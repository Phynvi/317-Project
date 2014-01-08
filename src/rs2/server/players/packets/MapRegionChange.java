/**
 * 
 */
package rs2.server.players.packets;

import rs2.server.players.Player;

/**
 * @author John
 *
 */
public class MapRegionChange implements Packets {

	/* (non-Javadoc)
	 * @see rs2.server.players.packets.Packets#handlePacket(rs2.server.players.Player, int, int)
	 */
	@Override
	public void handlePacket(Player p, int packetSize, int packetType) {
		switch (packetType) {
		case 121: // Map region finished loading
			break;
		case 210: // Player entered new map region
			break;
		}

	}

}
