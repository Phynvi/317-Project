package rs2.server.players;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * @author John
 *
 */
public class GameManager {
	
	/**
	 * Properties to load from character file
	 */
	static Properties loadProperties = new Properties();
 
	/**
	 * Properties to save to character file
	 */
	static Properties saveProperties = new Properties();
	
	/**
	 * File path to saved character files
	 */
	static String filePath = "Data/savedGames/";
	
	public static void saveGame(Player p) {
		String fileName = p.username.toLowerCase() + ".character";
		saveProperties.setProperty("username", p.username);
		saveProperties.setProperty("password", p.password);
		saveProperties.setProperty("lastIp", p.hostAddress);
		saveProperties.setProperty("rights", Integer.toString(p.rights));
		saveProperties.setProperty("coordX", Integer.toString(p.posX));
		saveProperties.setProperty("coordY", Integer.toString(p.posY));
		saveProperties.setProperty("height", Integer.toString(p.height));
		try {
			saveProperties.store(new FileOutputStream(filePath + fileName), null);
			System.out.println("#pre#Game saved for: " + p.username);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("#pre##err# Could not save game for: " + p.username);
		}
	}
	
	/**
	 * 
	 * @param p the player
	 * @param username the username attempting to log in
	 * @return login return code (1 = new character, 2 = valid login)
	 */
	public static int loadGame(Player p, String username) {
		String fileName = username.toLowerCase() + ".character";
		try {
			loadProperties.load(new FileInputStream(filePath + fileName));
		} catch (Exception e) {
			System.out.println("#pre#Character file not found: " + username);
			return 1;
		}
		p.username = loadProperties.getProperty("username");
		p.password = loadProperties.getProperty("password");
		p.rights = loadProperties.getProperty("rights") != null ? Integer.parseInt(loadProperties.getProperty("rights")) : 0;
		p.toX = loadProperties.getProperty("coordX") != null ? Integer.parseInt(loadProperties.getProperty("coordX")) : 3200;
		p.toY =loadProperties.getProperty("coordY") != null ? Integer.parseInt(loadProperties.getProperty("coordY")) : 3200;
		p.height =loadProperties.getProperty("height") != null ? Integer.parseInt(loadProperties.getProperty("height")) : 0;
		return 2;
		
	}
}
