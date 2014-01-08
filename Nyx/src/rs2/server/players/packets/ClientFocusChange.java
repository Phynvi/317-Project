/**
 * 
 */
package rs2.server.players.packets;

import rs2.server.players.Player;

/**
 * @author John
 *
 */
public class ClientFocusChange implements Packets {

	/* (non-Javadoc)
	 * @see rs2.server.players.packets.Packets#handlePacket(rs2.server.players.Player, int, int)
	 */
	@Override
	public void handlePacket(Player p, int packetSize, int packetType) {
		// Client has been minimized or brought back to the foreground
		p.clientMinimized = p.clientMinimized ? false : true;
		p.sendMessage(p.clientMinimized ? "You have minimized your client..." : "You have brought your client back to the foreground...");

	}

}
