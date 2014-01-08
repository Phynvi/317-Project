package rs2.server.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import rs2.server.io.Stream;
import rs2.server.players.GameManager;
import rs2.server.players.Player;
import rs2.server.players.PlayerHandler;
import rs2.server.util.ISAACCipher;

/**
 * @author John (deathschaos9)
 * 
 */
public class LoginProtocol {
	@SuppressWarnings("unused")
	private InputStream in;
	private OutputStream out;
	private Stream inStream;
	private Stream outStream;
	private int returnCode = 2;

	public LoginProtocol(Socket s, InputStream in, OutputStream out,
			Stream inStream, Stream outStream, int playerIndex)
			throws IOException {
		this.in = in;
		this.out = out;
		this.inStream = inStream;
		this.outStream = outStream;
		Player player = (Player) PlayerHandler.players[playerIndex];
		login(player);
	}

	private void login(Player player) throws IOException {
		try {
			player.fillInStream(2);

			/**
			 * Initial login request (Login opcode)
			 */
			int loginOpcode = inStream.readUnsignedByte();
			loginOpcode = 0;

			/**
			 * Name hash
			 */
			@SuppressWarnings("unused")
			int nameHash = inStream.readUnsignedByte();

			/**
			 * Write 8 bytes (1 long) to client.
			 */
			outStream.writeQWord(1);

			/**
			 * Write login opcode back to client
			 */
			outStream.writeByte(loginOpcode);

			/**
			 * Create and write ISAAC seed half (severSessionKey)
			 */
			long serverSessionKey = ((long) (java.lang.Math.random() * 99999999D) << 32)
					+ (long) (java.lang.Math.random() * 99999999D);
			outStream.writeQWord(serverSessionKey);

			/**
			 * Write buffer
			 */
			out.write(outStream.buffer, 0, outStream.currentOffset);
			outStream.currentOffset = 0;
			player.fillInStream(2);

			/**
			 * Connection opcode
			 */
			int opcode = inStream.readUnsignedByte();
			if (opcode != 16 && opcode != 18) {
				System.out.println("#pre##err#Unexpected opcode: " + opcode);
				return;
			}

			/**
			 * RSA encrypted block size
			 */
			int rsaBlockSize = inStream.readUnsignedByte();
			int rsaEncryptedBlockSize = rsaBlockSize - (36 + 1 + 1 + 2);
			player.fillInStream(rsaBlockSize);

			/**
			 * Magic ID (use is unknown)
			 */
			int magicId = inStream.readUnsignedByte();
			if (magicId != 255) {
				System.out.println("#pre##err#Expected magid ID of 255; read "
						+ magicId);
				return;
			}

			/**
			 * Client version
			 */
			int clientVersion = inStream.readUnsignedWord();
			if (clientVersion != 317) {
				System.out.println("#pre##err#Invalid client version: "
						+ clientVersion);
				return;
			}

			/**
			 * Memory version (unused)
			 */
			@SuppressWarnings("unused")
			int memoryVersion = inStream.readUnsignedByte();

			/**
			 * Read CRC keys (unused)
			 */
			int[] CRC_KEYS = new int[10];
			for (int i = 0; i < 9; i++) {
				CRC_KEYS[i] = inStream.readDWord();
			}

			/**
			 * Read RSA encoded block
			 */
			rsaEncryptedBlockSize--;
			int rsaEncodedBlock = inStream.readUnsignedByte();
			if (rsaEncryptedBlockSize != rsaEncodedBlock) {
				System.out.println("#pre##err#Encrypted data lenth ("
						+ rsaEncryptedBlockSize
						+ ") did not equal RSA encoded block ("
						+ rsaEncodedBlock + ")");
				return;
			}

			/**
			 * RSA opcode
			 */
			int rsaOpcode = inStream.readUnsignedByte();
			if (rsaOpcode != 10) {
				System.out
						.println("#pre##err#RSA opcode was expected to be 10; read "
								+ rsaOpcode);
				return;
			}

			/**
			 * Read server & client session key
			 */
			long clientSessionKey = inStream.readQWord();
			serverSessionKey = inStream.readQWord();

			/**
			 * Player username and password
			 */
			String username = inStream.readString();
			username.toLowerCase();
			username = username.substring(4); // For some reason, 4 characters
												// are added before the
												// username.
			username.trim();
			username = Character.toUpperCase(username.charAt(0))
					+ username.substring(1, username.length());

			String password = inStream.readString();
			int loginReturn = GameManager.loadGame(player, username);
			switch (loginReturn) {
			case 1:
				player.username = username;
				player.password = password;
				System.out.println("#pre#New character joined: "
						+ player.username);
				break;
			case 2:
				if (password.equals(player.password)) {
					System.out.println("#pre#Returning character: "
							+ player.username);
				} else {
					System.out.println("#pre##err# Invalid password: "
							+ player.username);
					returnCode = 3;
				}
				break;
			}

			/**
			 * ISAAC Seeds
			 */
			int seed[] = new int[4];

			seed[0] = (int) (clientSessionKey >> 32);
			seed[1] = (int) clientSessionKey;
			seed[2] = (int) (serverSessionKey >> 32);
			seed[3] = (int) serverSessionKey;

			/**
			 * Initialize ISAAC decryptor
			 */
			player.inStreamDecryption = new ISAACCipher(seed);
			for (int i = 0; i < 4; i++) {
				seed[i] += 50;
			}
			player.outStreamDecryption = new ISAACCipher(seed);
			outStream.packetEncryption = player.outStreamDecryption;

			/**
			 * Finalize login
			 */
			out.write(returnCode);
			out.write(player.rights);
			out.write(0);
			if (returnCode != 2)
				player.destruct();
			else
				player.online = true;
		} catch (IOException e) {
			System.err.println("#pre##err#An error occured during log in.");
			e.printStackTrace();
		}
	}
}
