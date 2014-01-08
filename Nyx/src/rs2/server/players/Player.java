package rs2.server.players;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import rs2.server.io.DecodeBuffer;
import rs2.server.io.EncodeBuffer;
import rs2.server.io.LoginProtocol;
import rs2.server.io.Stream;
import rs2.server.players.packets.PacketHandler;
import rs2.server.util.Constants;
import rs2.server.util.ISAACCipher;
import rs2.server.util.Misc;

/**
 * @author John (deathschaos9)
 * 
 */
public class Player implements Runnable {
	public String username = "";
	public String password = "";
	public String hostAddress;
	public int rights = 0;
	public int playerId = 0;
	public int localId;
	public OutputStream out;
	public InputStream in;
	public boolean disconnected;
	public Socket s = null;
	public PlayerHandler handler;
	public long lastPacket;
	public Player playerList[] = new Player[Constants.MAX_PLAYERS];
	public int playerListSize = 0;
	public boolean initialized;
	public int combatLevel = 3;
	public boolean online;

	/**
	 * Movement/Position variables
	 */
	public int curX, curY;
	public int toX, toY;
	public int posX, posY;
	public int height;
	public byte playerInListBitmap[] = new byte[(Constants.MAX_PLAYERS + 7) >> 3];
	public int primaryDirection = -1;
	public int secondaryDirection = -1;
	public boolean isMoving;
	public int mapRegionX;
	public int mapRegionY;
	public final int walkingQueueSize = 50;
	public int walkingQueueX[] = new int[walkingQueueSize];
	public int walkingQueueY[] = new int[walkingQueueSize];
	public int walkingQueueReadPtr = 0;
	public int wQueueWritePtr = 0;
	public boolean isRunning = false;
	public int newWalkCmdX[] = new int[walkingQueueSize];
	public int newWalkCmdY[] = new int[walkingQueueSize];
	public int newWalkCmdSteps = 0;
	public int travelBackX[] = new int[walkingQueueSize];
	public int travelBackY[] = new int[walkingQueueSize];
	public int numTravelBackSteps = 0;

	/**
	 * Update flags
	 */
	public boolean mapRegionChanged = false;
	public boolean updateRequired = true;
	public boolean playerTeleported = false;
	public boolean faceUpdateRequired;
	public int focusPointX = -1;
	public int focusPointY = -1;
	public boolean hitUpdateRequried;
	public boolean mask100update;
	public int animationRequest = -1;
	public boolean forcedChatUpdateRequired;
	public boolean appearanceUpdateRequired;
	public boolean clientMinimized = false;

	/**
	 * Appearance variables
	 */
	public int gender;
	public int headIcon;
	public int headIconPk;
	public int pArms;
	public int pBeard;
	public int pFeet;
	public int pFeetC;
	public int pGender;
	public int pHairC;
	public int pHands;
	public int pHead;
	public int pLegs;
	public int pLegsC;
	public int pSkinC;
	public int pTorso;
	public int pTorsoC;
	public int playerSE;
	public int playerSEA;
	public int playerSER;
	public int playerSEW;
	public int pEmote;

	/**
	 * Chat variables
	 */
	public boolean chatUpdateRequired;
	public byte chatText[] = new byte[4096];
	public byte chatTextSize = 0;
	public int chatColor = 0;
	public int chatEffects = 0;

	/**
	 * Instancing other classes
	 */
	public ISAACCipher inStreamDecryption;
	public ISAACCipher outStreamDecryption;
	private PlayerUpdating updater;
	private PlayerEquipment equipment;
	private Stream outStream;
	private Stream inStream;
	private DecodeBuffer decodeBuffer;
	private EncodeBuffer encodeBuffer;

	public PlayerUpdating getPlayerUpdater() {
		return updater == null ? updater = new PlayerUpdating(this) : updater;
	}

	public PlayerEquipment getEquipment() {
		return equipment == null ? equipment = new PlayerEquipment(this)
				: equipment;
	}

	public DecodeBuffer getDecodeBuffer() {
		return decodeBuffer;
	}

	public EncodeBuffer getEncodeBuffer() {
		return encodeBuffer;
	}

	public Stream getOutStream() {
		return outStream;
	}

	public Stream getInStream() {
		return inStream;
	}

	public Player(Socket clientSocket, int playerIndex) throws IOException {
		s = clientSocket;
		playerId = playerIndex;
		in = s.getInputStream();
		out = s.getOutputStream();
		// decodeBuffer = new DecodeBuffer(BUFFER_SIZE);
		// encodeBuffer = new EncodeBuffer(BUFFER_SIZE);
		inStream = new Stream(new byte[BUFFER_SIZE]);
		outStream = new Stream(new byte[BUFFER_SIZE]);
		pArms = 31;
		pBeard = 16;
		pFeet = 42;
		pFeetC = 3;
		pGender = 0;
		pHairC = 3;
		pHands = 33;
		pHead = 1;
		pLegs = 39;
		pLegsC = 2;
		pSkinC = 0;
		pTorso = 20;
		pTorsoC = 1;
		playerSE = 808;
		playerSEA = 806;
		playerSER = 824;
		playerSEW = 819;
		pEmote = 808;
		readPtr = writePtr = 0;
		buffer = new byte[BUFFER_SIZE];
		height = 0;
		toX = 3200;
		toY = 3200;
		posX = posY = -1;
		mapRegionX = mapRegionY = -1;
		curX = curY = 0;
		resetWalkingQueue();

	}

	public void createNewTileObject(int x, int y, int typeID, int orientation,
			int tileObjectType) {
		getOutStream().createFrame(85);
		getOutStream().writeByteC(y - (mapRegionY * 8));
		getOutStream().writeByteC(x - (mapRegionX * 8));
		getOutStream().createFrame(151);
		getOutStream().writeByteA(0);
		getOutStream().writeWordBigEndian(typeID);
		getOutStream().writeByteS((tileObjectType << 2) + (orientation & 3));
	}

	@Override
	public void run() {
		try {
			new LoginProtocol(s, in, out, inStream, getOutStream(), playerId);
			flushOutStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		buffer = new byte[BUFFER_SIZE];
		readPtr = 0;
		writePtr = 0;

		int position, offset;

		while (!disconnected && online) {
			synchronized (this) {
				if (writePtr == readPtr) {
					try {
						wait();
					} catch (java.lang.InterruptedException _ex) {
					}
				}
				offset = readPtr;
				if (writePtr >= readPtr) {
					position = writePtr - readPtr;
				} else {
					position = BUFFER_SIZE - readPtr;
				}
			}
			if (position > 0) {
				try {
					out.write(buffer, offset, position);
					readPtr = (readPtr + position) % BUFFER_SIZE;
					if (writePtr == readPtr) {
						out.flush();
					}
				} catch (Exception e) {
					destruct();
				}
			}
		}
	}

	public void destruct() {
		playerListSize = 0;
		for (int i = 0; i < 255; i++)
			playerList[i] = null;
		toX = toY = 0;
		getNextPlayerMovement();
		getPlayerUpdater().updateMyMovement(this, getOutStream());
		try {
			s.close();
			PlayerHandler.players[playerId] = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		in = null;
		out = null;
		System.out.println("#pre#Player: " + playerId + ", " + username
				+ " disconnected.");
	}

	public int readPtr, writePtr;
	public final int BUFFER_SIZE = 2042;
	public byte buffer[];

	public void flushOutStream() {
		if (disconnected || getOutStream().currentOffset == 0) {
			return;
		}
		synchronized (this) {
			int maxWritePtr = (readPtr + BUFFER_SIZE - 2) % BUFFER_SIZE;
			for (int i = 0; i < getOutStream().currentOffset; i++) {
				buffer[writePtr] = getOutStream().buffer[i];
				writePtr = (writePtr + 1) % BUFFER_SIZE;
				if (writePtr == maxWritePtr) {
					disconnected = true;
					destruct();
					return;
				}
			}
			getOutStream().currentOffset = 0;
			notify();
		}
	}

	public void fillInStream(int i) throws java.io.IOException {
		inStream.currentOffset = 0;
		in.read(inStream.buffer, 0, i);
	}

	public int getPlayerRights() {
		if (username.equals("Deathschaos9") || username.equals("Mopar"))
			return 2;
		return 0;
	}

	public void resetWalkingQueue() {
		walkingQueueReadPtr = wQueueWritePtr = 0;
		for (int i = 0; i < walkingQueueSize; i++) {
			walkingQueueX[i] = curX + 20;
			walkingQueueY[i] = curY;
		}
	}

	public void addToWalkingQueue(int x, int y) {
		int next = (wQueueWritePtr + 1) % walkingQueueSize;
		if (next == wQueueWritePtr)
			return;
		walkingQueueX[wQueueWritePtr] = x;
		walkingQueueY[wQueueWritePtr] = y;
		wQueueWritePtr = next;
	}

	public void getNextPlayerMovement() {
		mapRegionChanged = false;
		playerTeleported = false;
		primaryDirection = secondaryDirection = -1;

		if (toX != -1 && toY != -1) {
			mapRegionChanged = true;
			if (mapRegionX != -1 && mapRegionY != -1) {
				int relX = toX - mapRegionX * 8, relY = toY - mapRegionY * 8;
				if (relX >= 2 * 8 && relX < 11 * 8 && relY >= 2 * 8
						&& relY < 11 * 8)
					mapRegionChanged = false;
			}
			if (mapRegionChanged) {
				mapRegionX = (toX >> 3) - 6;
				mapRegionY = (toY >> 3) - 6;
			}
			curX = toX - 8 * mapRegionX;
			curY = toY - 8 * mapRegionY;
			posX = toX;
			posY = toY;
			resetWalkingQueue();

			toX = toY = -1;
			playerTeleported = true;
		} else {
			primaryDirection = getNextWalkingDirection();
			if (primaryDirection == -1)
				return;
			if (isRunning)
				secondaryDirection = getNextWalkingDirection();
			else
				secondaryDirection = -1;
			int deltaX = 0, deltaY = 0;
			if (curX < 2 * 8) {
				deltaX = 4 * 8;
				mapRegionX -= 4;
				mapRegionChanged = true;
			} else if (curX >= 11 * 8) {
				deltaX = -4 * 8;
				mapRegionX += 4;
				mapRegionChanged = true;
			}
			if (curY < 2 * 8) {
				deltaY = 4 * 8;
				mapRegionY -= 4;
				mapRegionChanged = true;
			} else if (curY >= 11 * 8) {
				deltaY = -4 * 8;
				mapRegionY += 4;
				mapRegionChanged = true;
			}

			if (mapRegionChanged) {
				curX += deltaX;
				curY += deltaY;
				for (int i = 0; i < walkingQueueSize; i++) {
					walkingQueueX[i] += deltaX;
					walkingQueueY[i] += deltaY;
				}
			}

		}
	}

	public void preProcessing() {
		newWalkCmdSteps = 0;
	}

	public void postProcessing() {
		if (newWalkCmdSteps > 0) {
			int firstX = newWalkCmdX[0], firstY = newWalkCmdY[0];
			int lastDir = 0;
			boolean foundVertex = false;
			numTravelBackSteps = 0;
			int ptr = walkingQueueReadPtr;
			int direction = Misc.direction(curX, curY, firstX, firstY);
			if (direction != -1 && (direction & 1) != 0) {
				do {
					lastDir = direction;
					if (--ptr < 0)
						ptr = walkingQueueSize - 1;

					travelBackX[numTravelBackSteps] = walkingQueueX[ptr];
					travelBackY[numTravelBackSteps++] = walkingQueueY[ptr];
					direction = Misc.direction(walkingQueueX[ptr],
							walkingQueueY[ptr], firstX, firstY);
					if (lastDir != direction) {
						foundVertex = true;
						break;
					}

				} while (ptr != wQueueWritePtr);
			} else
				foundVertex = true;

			if (!foundVertex) {
				System.out
						.println("#pre##err#Couldn't find connection vertex! "
								+ firstX + ", " + firstY + ", " + direction);
			} else {
				wQueueWritePtr = walkingQueueReadPtr;

				addToWalkingQueue(curX, curY);

				if (direction != -1 && (direction & 1) != 0) {

					for (int i = 0; i < numTravelBackSteps - 1; i++) {
						addToWalkingQueue(travelBackX[i], travelBackY[i]);
					}
					int wayPointX2 = travelBackX[numTravelBackSteps - 1], wayPointY2 = travelBackY[numTravelBackSteps - 1];
					int wayPointX1, wayPointY1;
					if (numTravelBackSteps == 1) {
						wayPointX1 = curX;
						wayPointY1 = curY;
					} else {
						wayPointX1 = travelBackX[numTravelBackSteps - 2];
						wayPointY1 = travelBackY[numTravelBackSteps - 2];
					}

					direction = Misc.direction(wayPointX1, wayPointY1,
							wayPointX2, wayPointY2);
					if (direction == -1 || (direction & 1) != 0) {
						System.out
								.println("#pre##err#The walking queue is corrupt! wp1=("
										+ wayPointX1
										+ ", "
										+ wayPointY1
										+ "), "
										+ "wp2=("
										+ wayPointX2
										+ ", "
										+ wayPointY2 + ")");
					} else {
						direction >>= 1;
						foundVertex = false;
						int x = wayPointX1, y = wayPointY1;
						while (x != wayPointX2 || y != wayPointY2) {
							x += Misc.directionDeltaX[direction];
							y += Misc.directionDeltaY[direction];
							if ((Misc.direction(x, y, firstX, firstY) & 1) == 0) {
								foundVertex = true;
								break;
							}
						}
						if (!foundVertex) {
							System.err
									.println("#pre##err#Unable to determine connection vertex!"
											+ "  wp1=("
											+ wayPointX1
											+ ", "
											+ wayPointY1
											+ "), wp2=("
											+ wayPointX2
											+ ", "
											+ wayPointY2
											+ "), "
											+ "first=("
											+ firstX
											+ ", "
											+ firstY + ")");
						} else
							addToWalkingQueue(wayPointX1, wayPointY1);
					}
				} else {
					for (int i = 0; i < numTravelBackSteps; i++) {
						addToWalkingQueue(travelBackX[i], travelBackY[i]);
					}
				}

				for (int i = 0; i < newWalkCmdSteps; i++) {
					addToWalkingQueue(newWalkCmdX[i], newWalkCmdY[i]);
				}

			}
		}
	}

	public int getNextWalkingDirection() {
		if (walkingQueueReadPtr == wQueueWritePtr)
			return -1;
		int dir;
		do {
			dir = Misc.direction(curX, curY,
					walkingQueueX[walkingQueueReadPtr],
					walkingQueueY[walkingQueueReadPtr]);
			if (dir == -1) {
				walkingQueueReadPtr = (walkingQueueReadPtr + 1)
						% walkingQueueSize;
			} else if ((dir & 1) != 0) {
				System.out
						.println("#pre##err#Invalid waypoint in walking queue!");
				resetWalkingQueue();
				return -1;
			}
		} while ((dir == -1) && (walkingQueueReadPtr != wQueueWritePtr));
		if (dir == -1)
			return -1;
		dir >>= 1;
		curX += Misc.directionDeltaX[dir];
		curY += Misc.directionDeltaY[dir];
		posX += Misc.directionDeltaX[dir];
		posY += Misc.directionDeltaY[dir];
		return dir;
	}

	public boolean withinDistance(Player player) {
		if (height != player.height)
			return false;
		int deltaX = player.posX - posX;
		int deltaY = player.posY - posY;
		return deltaX <= 15 && deltaX >= -16 && deltaY <= 15 && deltaY >= -16;
	}

	public void setSidebarInterfaces() {
		setSidebarInterface(0, 2423); // attack tab
		setSidebarInterface(1, 3917); // skills tab
		setSidebarInterface(2, 638); // quest tab
		setSidebarInterface(3, 3213); // backpack tab
		setSidebarInterface(4, 1644); // items wearing tab
		setSidebarInterface(5, 5608); // pray tab
		setSidebarInterface(6, 1151); // magic tab
		setSidebarInterface(7, 6299); // clan chat
		setSidebarInterface(8, 5065); // friend
		setSidebarInterface(9, 5715); // ignore
		setSidebarInterface(10, 2449); // logout tab
		setSidebarInterface(11, 4445); // wrench tab
		setSidebarInterface(12, 147); // run tab
	}

	private void setSidebarInterface(int i, int j) {
		getOutStream().createFrame(71);
		getOutStream().writeWord(j);
		getOutStream().writeByteA(i);

	}

	private void setChatOptions(int publicChat, int privateChat, int tradeChat) {
		getOutStream().createFrame(206);
		getOutStream().writeByte(publicChat);
		getOutStream().writeByte(privateChat);
		getOutStream().writeByte(tradeChat);
	}

	public void setInterfaceWalkable(int i) {
		getOutStream().createFrame(208);
		getOutStream().writeWordBigEndian_dup(i);
		flushOutStream();
	}

	public void showInterface(int i) {
		getOutStream().createFrame(97);
		getOutStream().writeWord(i);
		flushOutStream();
	}

	public void sendFrame171(int i) {
		getOutStream().createFrame(171);
		getOutStream().writeByte(0);
		getOutStream().writeWord(i);
		flushOutStream();
	}
	
	/**
	 * Send packet id 126 to client
	 * @param data the string to send
	 * @param frame the interface child frame
	 */
	public void sendFrame126(String data, int frame) {
		getOutStream().createFrameVarSizeWord(126);
		getOutStream().writeString(data);
		getOutStream().writeWordA(frame);
		getOutStream().endFrameVarSizeWord();
		flushOutStream();
	}

	public void initialize() {
		setChatOptions(0, 0, 0);
		setClientConfig(43, 2 - 1);
		setClientConfig(166, 3);
		getOutStream().createFrame(249);
		getOutStream().writeByteA(1);
		getOutStream().writeWordBigEndianA(playerId);
		getOutStream().createFrame(107);
		setSidebarInterfaces();
		getOutStream().createFrameVarSize(104);
		getOutStream().writeByteC(4);
		getOutStream().writeByteA(0);
		getOutStream().writeString("Follow");
		getOutStream().endFrameVarSize();
		getOutStream().createFrameVarSize(104);
		getOutStream().writeByteC(5);
		getOutStream().writeByteA(0);
		getOutStream().writeString("Trade with");
		getOutStream().endFrameVarSize();
		requestUpdates();
		flushOutStream();
		sendWelcomeMessage();

	}

	private void sendWelcomeMessage() {
		sendMessage("Welcome to Nyx.");
	}

	public void sendMessage(String s) {

		outStream.createFrameVarSize(253);
		outStream.writeString(s);
		outStream.endFrameVarSize();

	}

	private void setClientConfig(int i, int j) {
		outStream.createFrame(36);
		outStream.writeWordBigEndian(i);
		outStream.writeByte(j);
	}

	public void update() {
		synchronized (this) {
			handler.updatePlayer(this, getOutStream());
			flushOutStream();
		}
	}

	public void clearUpdateFlags() {
		focusPointX = focusPointY = -1;
		updateRequired = false;
		chatUpdateRequired = false;
		appearanceUpdateRequired = false;
		hitUpdateRequried = false;
		animationRequest = -1;
		mask100update = false;

	}

	public int packetType = -1;
	public int packetSize;
	public long[] latency = new long[256];

	public boolean packetProcess() {
		if (disconnected) {
			return false;
		}
		try {

			if (in == null)
				return false;
			int available = in.available();
			if (available == 0)
				return false;

			if (packetType == -1) {
				try {
					packetType = in.read() & 0xff;
					if (inStreamDecryption != null)
						packetType = packetType
								- inStreamDecryption.getNextKey() & 0xff;
					packetSize = Constants.PACKET_SIZES[packetType];
					available--;
				} catch (SocketException e) {
					System.out.println("#pre##fatal#" + username
							+ e.getLocalizedMessage());
					destruct();
				}
			}
			if (packetSize == -1) {
				if (available > 0) {
					packetSize = in.read() & 0xff;
					available--;
				} else
					return false;
			}
			if (available < packetSize)
				return false;
			fillInStream(packetSize);
			if (packetType != 0 && packetType != -1) {
				latency[packetType] = System.currentTimeMillis();
				PacketHandler.handlePacket(this, packetSize, packetType);
			}
			packetType = -1;
		} catch (Exception e) {
			System.out
					.println("#pre##err#Exception encountered while parsing incoming packets from "
							+ username + ".");
			e.printStackTrace();
		}
		return true;
	}

	public void requestUpdates() {
		updateRequired = true;
		appearanceUpdateRequired = true;
		update();
	}

	public void logout() {
		outStream.createFrame(109);
		GameManager.saveGame(this);
		disconnected = true;
	}
}
