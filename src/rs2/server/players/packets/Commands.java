package rs2.server.players.packets;

import java.util.Timer;

import rs2.server.players.Player;

/**
 * @author John (deathschaos9)
 * 
 */
public class Commands implements Packets {

	private Timer interfaceTimer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * rs2.server.players.packets.Packets#handlePacket(rs2.server.players.Player
	 * , int, int)
	 */
	@Override
	public void handlePacket(final Player p, int packetSize, int packetType) {
		final String[] command = p.getInStream().readString().split(" ");
		if (command[0].equals("tele")) {
			try {
				p.toX = Integer.valueOf(command[1]);
				p.toY = Integer.valueOf(command[2]);
				p.height = Integer.valueOf(command[3]);
			} catch (Exception e) {
				p.sendMessage("Invalid syntax, use ::tele x y h");
			}
		}
		if (command[0].equals("anim")) {
			try {
				p.animationRequest = Integer.valueOf(command[1]);
				p.appearanceUpdateRequired = true;
			} catch (Exception e) {
				p.sendMessage("Invalid syntax, use ::anim #");
			}
		}
		if (command[0].startsWith("interface")) {
			try {
				interfaceTimer = new java.util.Timer();
				interfaceTimer.schedule(new java.util.TimerTask() {
					int interfaceToOpen = Integer.parseInt(command[1]);
					@Override
					public void run() {
						p.showInterface(interfaceToOpen);	
						p.sendMessage("Opening interface: " + interfaceToOpen);
						interfaceToOpen++;
					}
					
				}, 0, 750);
			} catch (Exception e) {
				p.sendMessage("Invalid syntax, use ::interface #");
			}
		}
		if (command[0].equals("stop"))
			interfaceTimer.cancel();
		if (command[0].equals("update")) {
			p.requestUpdates();
		}
		if (command[0].equals("pos")) {
			p.sendMessage("Position: " + p.posX + ", " + p.posY + ", "
					+ p.height);
		}

	}

}
