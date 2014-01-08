package rs2.server.players.packets;

import rs2.server.players.Player;

/**
 * @author John (deathschaos9)
 * 
 */
public class PacketHandler {
	static Packets packets[] = new Packets[255];

	static {
		Packets unusedPacket = new UnusedPacket();
		Packets walking = new Walking();
		Packets mapRegionChange = new MapRegionChange();
		for (int i = 0; i < packets.length; i++) {
			packets[i] = unusedPacket;
		}
		packets[3] = new ClientFocusChange();
		packets[4] = new Chat();
		packets[77] = new AntiBotCameraRotate();
		packets[98] = walking;
		packets[103] = new Commands();
		packets[121] = mapRegionChange;
		packets[185] = new ClickButton();
		packets[164] = walking;
		packets[202] = new IdleLogout();
		packets[210] = mapRegionChange;
		packets[218] = new ReportPlayer();
		packets[241] = new ClickInGame();
		packets[248] = walking;
	}

	/**
	 * @param player
	 *            the player
	 * @param packetSize
	 *            the size of the packet
	 * @param packetType
	 *            the packet type
	 */
	public static void handlePacket(Player player, int packetSize,
			int packetType) {
		if (packetType == -1)
			return;
		Packets packet = packets[packetType];
		packet.handlePacket(player, packetSize, packetType);

	}
}
