package rs2.server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import rs2.server.net.SocketListener;
import rs2.server.players.PlayerHandler;
import rs2.server.util.Constants;
import rs2.server.util.PrintStreamLogger;
import rs2.server.util.Ticker;

/**
 * @author John (deathschaos9)
 * 
 */
public class Server {
	public static PlayerHandler playerHandler;
	public static Ticker mainTick;

	public static void main(String[] args) {
		loadWelcomeMessage();
		System.setOut(new PrintStreamLogger(System.out));
		init();
	}

	private static void init() {
		playerHandler = new PlayerHandler();
		mainTick = new Ticker(Constants.PRIMARY_TICK_TIME);
		new Thread(mainTick).start();
		new SocketListener(Constants.PORT);
	}

	private static void loadWelcomeMessage() {
		Scanner s = null;
		try {
			s = new Scanner(new FileReader("Data/welcome.dat"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (s.hasNext()) {
			System.out.println(s.nextLine());
		}

	}

	public static PlayerHandler getHandler() {
		return playerHandler;
	}

	private static long lastDebug = System.currentTimeMillis();
	private static int debugTime = 300000;

	public static void tick() {
		if (System.currentTimeMillis() - lastDebug > debugTime) {
			lastDebug = System.currentTimeMillis();
			double memoryUsage = (Runtime.getRuntime().totalMemory()
					- Runtime.getRuntime().freeMemory()) / 1024;
			String formattedMemoryUsage = null;
			if (memoryUsage >= 1000) {
				memoryUsage /= 1024;
				formattedMemoryUsage = String.valueOf(memoryUsage).substring(0, 5) + "Mb";
			} else {
				formattedMemoryUsage = memoryUsage + "Kb";
			}
			System.out.println("#pre#Players online: "
					+ PlayerHandler.getPlayerCount() + " Memory usage: "
					+ formattedMemoryUsage);
		}
	}

}
