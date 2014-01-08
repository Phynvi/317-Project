package rs2.server.players;

import java.io.IOException;
import java.net.Socket;

import rs2.server.io.Stream;
import rs2.server.util.Constants;

/**
 * @author John (deathschaos9)
 * 
 */
public class PlayerHandler {

	/**
	 * Room for up to 2000 players
	 */
	public static Player players[] = new Player[Constants.MAX_PLAYERS];

	/**
	 * @return returns number of players online.
	 */
	public static int getPlayerCount() {
		int count = 0;
		for (int i = 0; i < players.length; i++) {
			if ((players[i] != null) && !players[i].disconnected) {
				count++;
			}
		}
		return count;
	}

	public static int getPlayerID(String playerName) {
		for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
			if (players[i] != null) {
				if (players[i].username.equalsIgnoreCase(playerName))
					return i;
			}
		}
		return -1;
	}

	public PlayerHandler() {
		for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
			players[i] = null;
		}
	}

	public void updatePlayer(Player p, Stream outStream) {
		Stream in = new Stream(new byte[1024]);
		in.currentOffset = 0;
		p.getPlayerUpdater().updateMyMovement(p, outStream);
		boolean chatUpdate = p.chatUpdateRequired;
		p.chatUpdateRequired = false;
		p.getPlayerUpdater().appendUpdateBlock(p, in);
		p.chatUpdateRequired = chatUpdate;
		p.getOutStream().writeBits(8, p.playerListSize);
		int size = p.playerListSize;

		p.playerListSize = 0;
		for (int i = 0; i < size; i++) {
			if (!p.playerList[i].playerTeleported) {
				p.playerList[i].getPlayerUpdater().updateMovement(
						p.playerList[i], outStream);
				p.playerList[i].getPlayerUpdater().appendUpdateBlock(
						p.playerList[i], in);
				p.playerList[p.playerListSize++] = p.playerList[i];
			} else {
				int id = p.playerList[i].playerId;
				p.playerInListBitmap[id >> 3] &= ~(1 << (id & 7));
				p.getOutStream().writeBits(1, 1);
				p.getOutStream().writeBits(2, 3);
			}
		}

		for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
			if ((players[i] == null) || (!players[i].online)
					|| (players[i] == p))
				continue;
			if (!p.playerTeleported
					&& ((p.playerInListBitmap[players[i].playerId >> 3] & (1 << (players[i].playerId & 7))) != 0))
				continue;
			if (!p.withinDistance(players[i]))
				continue;
			addNewPlayer(p, players[i], outStream, in);
		}
		if (in.currentOffset > 0) {
			p.getOutStream().writeBits(11, 2047);
			p.getOutStream().finishBitAccess();
			p.getOutStream().writeBytes(in.buffer, in.currentOffset, 0);
		} else {
			p.getOutStream().finishBitAccess();
		}
		p.getOutStream().endFrameVarSizeWord();

	}

	public void addNewPlayer(Player me, Player other, Stream outStream,
			Stream in) {
		synchronized (this) {
			if (me.playerListSize >= 255) {
				return;
			}
			int id = other.playerId;
			me.playerInListBitmap[id >> 3] |= 1 << (id & 7);
			me.playerList[me.playerListSize++] = other;
			outStream.writeBits(11, id);
			outStream.writeBits(1, 1);
			boolean savedFlag = other.appearanceUpdateRequired;
			boolean savedUpdateRequired = other.updateRequired;
			other.appearanceUpdateRequired = true;
			other.updateRequired = true;
			other.getPlayerUpdater().appendUpdateBlock(other, in);
			other.appearanceUpdateRequired = savedFlag;
			other.updateRequired = savedUpdateRequired;
			outStream.writeBits(1, 1);
			int z = other.posY - me.posY;
			if (z < 0)
				z += 32;
			outStream.writeBits(5, z);
			z = other.posX - me.posX;
			if (z < 0)
				z += 32;
			outStream.writeBits(5, z);
		}
	}

	public void newPlayer(Socket s, String host) throws IOException {
		int slot = -1;
		for (int i = 1; i < players.length; i++) {
			if ((players[i] == null)) {
				slot = i;
				break;
			}
		}
		if (slot == -1)
			return;
		players[slot] = new Player(s, slot);
		System.out.println("#pre#Registering new player: " + slot);
		players[slot].handler = this;
		(new Thread(players[slot])).start();
		players[slot].localId = slot;
		players[slot].hostAddress = host;
		players[slot].lastPacket = System.currentTimeMillis();
	}

	public void tick() {
		for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
			if (players[i] == null)
				continue;
			if (!players[i].online)
				continue;
			if (players[i].disconnected) {
				players[i].destruct();
			}
			if (!players[i].initialized) {
				players[i].initialize();
				players[i].initialized = true;
			} else {
				players[i].update();
			}
			players[i].preProcessing();
			while(players[i].packetProcess());
			players[i].getNextPlayerMovement();
			players[i].postProcessing();
		}
	}
}
