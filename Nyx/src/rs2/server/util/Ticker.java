package rs2.server.util;

import rs2.server.Server;

/**
 * @author John (deathschaos9)
 * 
 */
public class Ticker extends Thread {
	private static int tickTime;

	public Ticker(int time) {
		tickTime = time;
	}

	public static void tick() {
		try {
			do {
				Server.tick();
				Server.getHandler().tick();
				try {
					Thread.sleep(tickTime);
				} catch (java.lang.Exception _ex) {
				}
			} while (true);
		} catch (java.lang.Exception _ex) {
		}

	}

	@Override
	public void run() {
		tick();
	}
}
