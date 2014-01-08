package rs2.server.players.packets;

import rs2.server.players.Player;

public class Chat implements Packets {

	@Override
	public void handlePacket(Player p, int packetSize, int packetType) {
		p.chatEffects = p.getInStream().readUnsignedByteS();
		p.chatColor = p.getInStream().readUnsignedByteS();
		p.chatTextSize = (byte) (packetSize - 2);
		p.getInStream().readBytes_reverseA(p.chatText, p.chatTextSize, 0);
		p.chatUpdateRequired = true;

	}
}
