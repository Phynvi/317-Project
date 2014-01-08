package rs2.server.players.packets;

import rs2.server.players.Player;

/**
 * @author John (deathschaos9)
 * 
 */
public interface Packets {
	public void handlePacket(Player p, int packetSize, int packetType);
}
