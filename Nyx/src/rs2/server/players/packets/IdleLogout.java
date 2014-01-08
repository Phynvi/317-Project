package rs2.server.players.packets;

import rs2.server.players.Player;

public class IdleLogout implements Packets {

	@Override
	public void handlePacket(Player p, int packetSize, int packetType) {
		p.logout();

	}

}
