package rs2.server.players.packets;

import rs2.server.players.Player;

public class Walking implements Packets {

	@Override
	public void handlePacket(Player p, int packetSize, int packetType) {
		switch (packetType) {
		case 98:
		case 164:
		case 248:
			if (packetType == 248) {
				p.packetSize -= 14;
			}
			p.newWalkCmdSteps = (p.packetSize - 5) / 2;
			if (++p.newWalkCmdSteps > p.walkingQueueSize) {
				p.newWalkCmdSteps = 0;
				return;
			}

			p.newWalkCmdX[0] = p.newWalkCmdY[0] = 0;

			int firstStepX = p.getInStream().readSignedWordBigEndianA()
					- p.mapRegionX * 8;
			for (int i = 1; i < p.newWalkCmdSteps; i++) {
				p.newWalkCmdX[i] = p.getInStream().readSignedByte();
				p.newWalkCmdY[i] = p.getInStream().readSignedByte();
			}

			int firstStepY = p.getInStream().readSignedWordBigEndian()
					- p.mapRegionY * 8;
			@SuppressWarnings("unused")
			boolean isRunning = p.getInStream().readSignedByteC() == 1;
			for (int i1 = 0; i1 < p.newWalkCmdSteps; i1++) {
				p.newWalkCmdX[i1] += firstStepX;
				p.newWalkCmdY[i1] += firstStepY;
			}
			break;
		}

	}

}
