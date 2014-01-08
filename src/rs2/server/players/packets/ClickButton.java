package rs2.server.players.packets;

import rs2.server.players.Player;

public class ClickButton implements Packets {

	@Override
	public void handlePacket(Player p, int packetSize, int packetType) {
		int buttonId = getButtonId(p.getInStream().buffer, 0, packetSize);
		switch (buttonId) {
		case 152:
		case 153:
			p.isRunning = p.isRunning ? false : true;
			break;
		case 9154:
			p.logout();
			break;
		default:
			p.sendMessage("Button: " + buttonId);
			break;
		}

	}

	private int getButtonId(byte[] buffer, int offset, int packetSize) {
		int button = 0;
		int multiplier = 1000;
		for (int i = 0; i < packetSize; i++) {
			int num = (buffer[offset + i] & 0xFF) * multiplier;
			button += num;
			if (multiplier > 1)
				multiplier = multiplier / 1000;
		}
		return button;
	}

}
