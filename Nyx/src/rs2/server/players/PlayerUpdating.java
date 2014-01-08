package rs2.server.players;

import rs2.server.io.Stream;
import rs2.server.util.Misc;

/**
 * @author John (deathschaos9)
 * 
 */
public class PlayerUpdating {
	Player p;
	protected static Stream updateBlock = new Stream(new byte[100]);

	PlayerUpdating(Player player) {
		this.p = player;
	}

	public void sendMapRegion() {
		p.getOutStream().createFrame(73);
		p.getOutStream().writeWordA(getMapRegionX());
		p.getOutStream().writeWord(getMapRegionY());
	}

	public int getMapRegionX() {
		return p.mapRegionX + 6;
	}

	public int getMapRegionY() {
		return p.mapRegionY + 6;
	}

	public int isUpdateRequired(Player player) {
		return (player.updateRequired || player.chatUpdateRequired) ? 1 : 0;
	}

	public void updateMovement(Player player, Stream outStream) {
		synchronized (player) {
			if (player.primaryDirection == -1) {
				if (player.updateRequired || player.chatUpdateRequired) {
					outStream.writeBits(1, 1);
					outStream.writeBits(2, 0);
				} else
					outStream.writeBits(1, 0);
			} else if (player.secondaryDirection == -1) {
				outStream.writeBits(1, 1);
				outStream.writeBits(2, 1);
				outStream.writeBits(3,
						Misc.xlateDirection[player.primaryDirection]);
				outStream.writeBits(1, isUpdateRequired(player));
			} else {
				outStream.writeBits(1, 1);
				outStream.writeBits(2, 2);
				outStream.writeBits(3,
						Misc.xlateDirection[player.primaryDirection]);
				outStream.writeBits(3,
						Misc.xlateDirection[player.secondaryDirection]);
				outStream.writeBits(1, isUpdateRequired(player));
			}
		}
	}

	void updateMyMovement(Player player, Stream outStream) {
		synchronized (player) {
			if (player.mapRegionChanged) {
				sendMapRegion();
			}
			if (player.playerTeleported) {
				outStream.createFrameVarSizeWord(81);
				outStream.initBitAccess();
				outStream.writeBits(1, 1);
				outStream.writeBits(2, 3);
				outStream.writeBits(2, player.height);
				outStream.writeBits(1, 1);
				outStream.writeBits(1, (player.updateRequired) ? 1 : 0);
				outStream.writeBits(7, player.curY);
				outStream.writeBits(7, player.curX);
				return;
			}
			if (player.primaryDirection == -1) {
				outStream.createFrameVarSizeWord(81);
				outStream.initBitAccess();
				player.isMoving = false;
				if (player.updateRequired) {
					outStream.writeBits(1, 1);
					outStream.writeBits(2, 0);
				} else {
					outStream.writeBits(1, 0);
				}
			} else {
				outStream.createFrameVarSizeWord(81);
				outStream.initBitAccess();
				outStream.writeBits(1, 1);

				if (player.secondaryDirection == -1) {
					player.isMoving = true;
					outStream.writeBits(2, 1);
					outStream.writeBits(3,
							Misc.xlateDirection[player.primaryDirection]);
					if (player.updateRequired)
						outStream.writeBits(1, 1);
					else
						outStream.writeBits(1, 0);
				} else {
					player.isMoving = true;
					outStream.writeBits(2, 2);
					outStream.writeBits(3,
							Misc.xlateDirection[player.primaryDirection]);
					outStream.writeBits(3,
							Misc.xlateDirection[player.secondaryDirection]);
					if (player.updateRequired)
						outStream.writeBits(1, 1);
					else
						outStream.writeBits(1, 0);
				}
			}
		}
	}

	public void appendUpdateBlock(Player player, Stream in) {
		synchronized (this) {
			if (!player.updateRequired && !player.chatUpdateRequired)
				return;
			int updateMask = 0;
			if (player.mask100update) {
				updateMask |= 0x100;
			}
			if (player.animationRequest != -1) {
				updateMask |= 8;
			}
			if (player.forcedChatUpdateRequired) {
				updateMask |= 4;
			}
			if (player.chatUpdateRequired) {
				updateMask |= 0x80;
			}
			if (player.appearanceUpdateRequired) {
				updateMask |= 0x10;
			}
			if (player.faceUpdateRequired) {
				updateMask |= 1;
			}
			if (player.focusPointX != -1) {
				updateMask |= 2;
			}
			if (player.hitUpdateRequried) {
				updateMask |= 0x20;
			}

			if (updateMask >= 0x100) {
				updateMask |= 0x40;
				in.writeByte(updateMask & 0xFF);
				in.writeByte(updateMask >> 8);
			} else {
				in.writeByte(updateMask);
			}

			if (player.mask100update) {
				appendMask100Update(in);
			}
			if (player.animationRequest != -1) {
				appendAnimationRequest(in);
			}
			if (player.forcedChatUpdateRequired) {
				appendForcedChat(in);
			}
			if (player.chatUpdateRequired) {
				appendPlayerChatText(player, in);
			}
			if (player.faceUpdateRequired) {
				appendFaceUpdate(in);
			}
			if (player.appearanceUpdateRequired) {
				appendPlayerAppearance(in);
			}
			if (player.focusPointX != -1) {
				appendSetFocusDestination(in);
			}
			if (player.hitUpdateRequried) {
				appendHitUpdate(in);
			}
		}
		player.clearUpdateFlags();
	}

	private void appendHitUpdate(Stream in) {
		// TODO Auto-generated method stub

	}

	private void appendSetFocusDestination(Stream in) {
		// TODO Auto-generated method stub

	}

	private void appendPlayerAppearance(Stream in) {
		updateBlock.currentOffset = 0;
		updateBlock.writeByte(p.gender);
		updateBlock.writeByte(p.headIcon);
		if (p.getEquipment().playerEquipment[p.getEquipment().playerHat] > 1) {
			updateBlock.writeWord(0x200 + p.getEquipment().playerEquipment[p
					.getEquipment().playerHat]);
		} else {
			updateBlock.writeByte(0);
		}
		if (p.getEquipment().playerEquipment[p.getEquipment().playerCape] > 1) {
			updateBlock.writeWord(0x200 + p.getEquipment().playerEquipment[p
					.getEquipment().playerCape]);
		} else {
			updateBlock.writeByte(0);
		}
		if (p.getEquipment().playerEquipment[p.getEquipment().playerAmulet] > 1) {
			updateBlock.writeWord(0x200 + p.getEquipment().playerEquipment[p
					.getEquipment().playerAmulet]);
		} else {
			updateBlock.writeByte(0);
		}
		if (p.getEquipment().playerEquipment[p.getEquipment().playerWeapon] > 1) {
			updateBlock.writeWord(0x200 + p.getEquipment().playerEquipment[p
					.getEquipment().playerWeapon]);
		} else {
			updateBlock.writeByte(0);
		}
		if (p.getEquipment().playerEquipment[p.getEquipment().playerChest] > 1) {
			updateBlock.writeWord(0x200 + p.getEquipment().playerEquipment[p
					.getEquipment().playerChest]);
		} else {
			updateBlock.writeWord(256 + p.pTorso);
		}
		if (p.getEquipment().playerEquipment[p.getEquipment().playerShield] > 1) {
			updateBlock.writeWord(0x200 + p.getEquipment().playerEquipment[p
					.getEquipment().playerShield]);
		} else {
			updateBlock.writeByte(0);
		}
		updateBlock.writeWord(256 + p.pArms);
		updateBlock.writeWord(256 + p.pLegs);
		updateBlock.writeWord(256 + p.pHead);
		updateBlock.writeWord(256 + p.pHands);
		updateBlock.writeWord(256 + p.pFeet);
		updateBlock.writeWord(256 + p.pBeard);

		updateBlock.writeByte(p.pHairC);
		updateBlock.writeByte(p.pTorsoC);
		updateBlock.writeByte(p.pLegsC);
		updateBlock.writeByte(p.pFeetC);
		updateBlock.writeByte(p.pSkinC);
		updateBlock.writeWord(p.pEmote);
		updateBlock.writeWord(823);
		updateBlock.writeWord(p.playerSEW);
		updateBlock.writeWord(820);
		updateBlock.writeWord(821);
		updateBlock.writeWord(822);
		updateBlock.writeWord(p.playerSER);
		updateBlock.writeQWord(Misc.playerNameToInt64(p.username));
		updateBlock.writeByte(p.combatLevel);
		updateBlock.writeWord(0);
		in.writeByteC(updateBlock.currentOffset);
		in.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);

	}

	private void appendFaceUpdate(Stream in) {
		// TODO Auto-generated method stub

	}

	private void appendPlayerChatText(Player player, Stream in) {
		in.writeWordBigEndian(((player.chatColor & 0xFF) << 8)
				+ (player.chatEffects & 0xFF));
		in.writeByte(player.rights);
		in.writeByteC(player.chatTextSize);
		in.writeBytes_reverse(player.chatText, player.chatTextSize, 0);

	}

	private void appendForcedChat(Stream in) {
		// TODO Auto-generated method stub

	}

	private void appendAnimationRequest(Stream in) {
		in.writeWordBigEndian(p.animationRequest);
		in.writeByte(0);

	}

	private void appendMask100Update(Stream in) {
		// TODO Auto-generated method stub

	}
}
