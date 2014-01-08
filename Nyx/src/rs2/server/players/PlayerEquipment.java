package rs2.server.players;

/**
 * @author John (deathschaos9)
 * 
 */
public class PlayerEquipment {
	Player p;
	
	public PlayerEquipment(Player player) {
		this.p = player;
	}
	public int[] playerEquipment = new int[14];
	public int[] playerEquipmentN = new int[14];
	public int playerHat = 0;
	public int playerCape = 1;
	public int playerAmulet = 2;
	public int playerWeapon = 3;
	public int playerChest = 4;
	public int playerShield = 5;
	public int playerLegs = 7;
	public int playerHands = 9;
	public int playerFeet = 10;
	public int playerRing = 12;
	public int playerArrows = 13;
}
