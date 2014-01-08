package rs2.server.players.packets;

import rs2.server.players.Player;

/**
 * @author John (deathschaos9)
 *
 */
public class UnusedPacket implements Packets {

	/* (non-Javadoc)
	 * @see rs2.server.players.packets.Packets#handlePacket(rs2.server.players.Player, int, int)
	 */
	@Override
	public void handlePacket(Player p, int packetSize, int packetType) {
		p.sendMessage("Unhandled packet: " + packetType + ", size: " + packetSize);

	}

}
