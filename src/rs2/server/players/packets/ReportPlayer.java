/**
 * 
 */
package rs2.server.players.packets;

import rs2.server.players.Player;
import rs2.server.util.Misc;

/**
 * @author John
 * 
 */
public class ReportPlayer implements Packets {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * rs2.server.players.packets.Packets#handlePacket(rs2.server.players.Player
	 * , int, int)
	 */
	@Override
	public void handlePacket(Player p, int packetSize, int packetType) {
		String additionalMessage;
		String reported = Misc.longToPlayerName(p.getInStream().readQWord());
		int ruleBroken = p.getInStream().readUnsignedByte() + 1;
		if (p.getInStream().readUnsignedByte() == 1)
			additionalMessage = ", 48hr mute active.";
		else
			additionalMessage = ".";

		p.sendMessage("You have reported: " + reported
				+ " for breaking rule #" + ruleBroken + additionalMessage);

	}

}
