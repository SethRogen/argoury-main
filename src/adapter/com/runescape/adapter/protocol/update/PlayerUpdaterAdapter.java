/**
 *
 */
package com.runescape.adapter.protocol.update;

import com.runescape.Constants;
import com.runescape.Static;
import com.runescape.logic.item.ItemDefinition;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.item.EquipmentDefinition.EquipmentType;
import com.runescape.logic.map.Directions.RunningDirection;
import com.runescape.logic.mask.*;
import com.runescape.logic.mask.Mask.MaskType;
import com.runescape.logic.npc.NPCDefinition;
import com.runescape.logic.player.Appearance;
import com.runescape.logic.player.GEI;
import com.runescape.logic.player.Levels;
import com.runescape.logic.player.Player;
import com.runescape.logic.utility.PlayerUpdater;
import com.runescape.network.Frame;
import com.runescape.network.FrameBuilder;
import com.runescape.network.Frame.FrameType;

import java.util.Iterator;
import java.util.Queue;

/**
 * @author Lazaro
 */
public final class PlayerUpdaterAdapter implements PlayerUpdater {
    private Player[] players = new Player[2048];
    private int[] playerLocations = new int[2048];

    @Override
    public Player[] getPlayers() {
        return players;
    }

    @Override
    public int[] getPlayerLocations() {
        return playerLocations;
    }

    private void doAppearance(Player player, FrameBuilder pb) {
        Frame appearanceBlock = player.getCachedAppearanceBlock();
        if (appearanceBlock == null) {
            player.getMasks().setAppearanceUpdate(true);
            player.setCachedAppearanceBlock(Static.world.getPlayerUpdater().doApperanceBlock(player));
            appearanceBlock = player.getCachedAppearanceBlock();
        }

        pb.writeS((byte) (appearanceBlock.getLength() & 0xFF));
        byte[] payload = appearanceBlock.getBytes();
        pb.writeBackwardsA(payload);
    }
    /* (non-Javadoc)
      * @see PlayerUpdater#doApperanceBlock(Player)
      */

    @Override
    public Frame doApperanceBlock(Player player) {
        FrameBuilder appearanceBlock = new FrameBuilder(256);
        Appearance app = player.getAppearance();
        int hash = 0;
        // hash |= 0 << 6; // something to do with the chat bar
        // hash |= 0 << 3; // player size (example: if the player turns into an
        // npc and it is bigger than 1 tile)
        // hash |= 0x2; if the player has a display name
        // hash |= 0x4; //??
        hash |= player.getAppearance().getGender().intValue() & 0x1;
        appearanceBlock.writeByte(hash);
        appearanceBlock.writeByte(0 /* 1-mob status */).writeByte(app.getPKIcon()).writeByte(app.getPrayerIcon()).writeByte(0);
        if (!app.isNPC()) {
            for (int i = 0; i < 4; i++) {
                PossesedItem item = player.getEquipment().get(i);
                if (item != null) {
                    appearanceBlock.writeShort(32768 + ItemDefinition.getEquipmentId(item.getId()));
                } else {
                    appearanceBlock.writeByte(0);
                }
            }
            PossesedItem chestItem = player.getEquipment().get(Constants.Equipment.CHEST_SLOT);
            if (chestItem != null) {
                appearanceBlock.writeShort(32768 + ItemDefinition.getEquipmentId(chestItem.getId()));
            } else {
                appearanceBlock.writeShort(0x100 + app.getLook(2));
            }
            PossesedItem shieldItem = player.getEquipment().get(Constants.Equipment.SHIELD_SLOT);
            if (shieldItem != null) {
                appearanceBlock.writeShort(32768 + ItemDefinition.getEquipmentId(shieldItem.getId()));
            } else {
                appearanceBlock.writeByte(0);
            }
            if (chestItem != null && chestItem.getDefinition().getEquipmentDefinition().getEquipmentType() == EquipmentType.PLATEBODY) {
                appearanceBlock.writeByte(0);
            } else {
                appearanceBlock.writeShort(0x100 + app.getLook(3));
            }
            PossesedItem bottomItem = player.getEquipment().get(Constants.Equipment.BOTTOMS_SLOT);
            if (bottomItem != null) {
                appearanceBlock.writeShort(32768 + ItemDefinition.getEquipmentId(bottomItem.getId()));
            } else {
                appearanceBlock.writeShort(0x100 + app.getLook(5));
            }
            PossesedItem helmItem = player.getEquipment().get(Constants.Equipment.HELM_SLOT);
            if (helmItem != null && (helmItem.getDefinition().getEquipmentDefinition().getEquipmentType() == EquipmentType.FULL_HELM || helmItem.getDefinition().getEquipmentDefinition().getEquipmentType() == EquipmentType.FULL_MASK)) {
                appearanceBlock.writeByte(0);
            } else {
                appearanceBlock.writeShort(0x100 + app.getLook(0));
            }
            PossesedItem glovesItem = player.getEquipment().get(Constants.Equipment.GLOVES_SLOT);
            if (glovesItem != null) {
                appearanceBlock.writeShort(32768 + ItemDefinition.getEquipmentId(glovesItem.getId()));
            } else {
                appearanceBlock.writeShort(0x100 + app.getLook(4));
            }
            PossesedItem bootsItem = player.getEquipment().get(Constants.Equipment.BOOTS_SLOT);
            if (bootsItem != null) {
                appearanceBlock.writeShort(32768 + ItemDefinition.getEquipmentId(bootsItem.getId()));
            } else {
                appearanceBlock.writeShort(0x100 + app.getLook(6));
            }
            if (helmItem != null && helmItem.getDefinition().getEquipmentDefinition().getEquipmentType() == EquipmentType.FULL_MASK) {
                appearanceBlock.writeByte(0);
            } else {
                appearanceBlock.writeShort(0x100 + app.getLook(1)); // face
            }
        } else {
            appearanceBlock.writeShort(-1);
            appearanceBlock.writeShort(app.getNPCType());
            appearanceBlock.writeByte(0);
        }
        for (int i = 0; i < 5; i++) {
            appearanceBlock.writeByte(app.getColor(i));
        }
        PossesedItem weapon = player.getEquipment().get(Constants.Equipment.WEAPON_SLOT);
        appearanceBlock.writeShort(app.isNPC() ? NPCDefinition.forId(app.getNPCType()).renderId : (weapon != null ? weapon.getDefinition().renderId : 1426));
        appearanceBlock.writeString(player.getName());
        // extra string if the player has a display name
        appearanceBlock.write((byte) player.getLevels().getCombatLevel()); // combat
        appearanceBlock.writeByte(0); //I have no idea what this is?
        appearanceBlock.writeByte(-1); //this sets minigame levels
        // level
        appearanceBlock.writeShort(0);
        appearanceBlock.writeByte(0);
        return appearanceBlock.toFrame();
    }
    /* (non-Javadoc)
      * @see PlayerUpdater#getPlayerHashLoc(int)
      */

    private void masks(Player owner, Player player, FrameBuilder pb, boolean newPlayer) {
        if (player.getCachedMaskBlock() != null && !newPlayer) {
            pb.write(player.getCachedMaskBlock().getBytes());
        } else {
            pb.write(doMaskBlock(owner, player, newPlayer).getBytes());
        }
    }
    /* (non-Javadoc)
      * @see EntityUpdater#doMaskBlock(Entity)
      */

    @Override
    public Frame doMaskBlock(Player owner, Player player) {
        return doMaskBlock(owner, player, false);
    }

    private Frame doMaskBlock(Player owner, Player player, boolean newPlayer) {
        if (owner == null && player.getMasks().requiresUpdate(MaskType.HIT)) {
            return null;
        }

        FrameBuilder fb = new FrameBuilder(256);
        int mask = 0;
        if (player.getMasks().requiresUpdate(MaskType.FACE_DIRECTION) || (newPlayer && player.getMasks().getFaceDirection() != Mask.MASK_NULL)) {
            mask |= 0x8;
        }
        if (player.getMasks().requiresUpdate(MaskType.FORCED_CHAT)) {
            mask |= 0x800;
        }
        if (player.getMasks().requiresUpdate(MaskType.GRAPHICS2)) {
            mask |= 0x1000;
        }
        if (player.getMasks().requiresUpdate(MaskType.MOVEMENT_MODE) || newPlayer) {
            mask |= 0x20;
        }
        if (player.getMasks().requiresUpdate(MaskType.FACE_ENTITY) || (newPlayer && player.getMasks().getFaceEntity() != Mask.MASK_NULL)) {
            mask |= 0x1;
        }
        if (player.getMasks().requiresUpdate(MaskType.GRAPHICS3)) {
            mask |= 0x20000;
        }
        if (player.getMasks().requiresUpdate(MaskType.ANIMATION)) {
            mask |= 0x40;
        }
        if (player.getMasks().requiresUpdate(MaskType.APPEARANCE) || newPlayer) {
            mask |= 0x10;
        }
        if (player.getMasks().requiresUpdate(MaskType.MOVEMENT)) {
            mask |= 0x400;
        }
        if (player.getMasks().requiresUpdate(MaskType.FORCED_MOVEMENT_MODE)) {
            mask |= 0x8000;
        }
        if (player.getMasks().requiresUpdate(MaskType.HIT)) {
            mask |= 0x2;
        }
        if (player.getMasks().requiresUpdate(MaskType.GRAPHICS)) {
            mask |= 0x80;
        }

        if (mask >= 0x100) {
            mask |= 0x4;
            if (mask >= 0x10000) {
                mask |= 0x200;
                fb.writeByte(mask & 0xff).writeByte(mask >> 8 & 0xffff).writeByte(mask >> 16);
            } else {
                fb.writeByte(mask & 0xff).writeByte(mask >> 8);
            }
        } else {
            fb.writeByte(mask & 0xff);
        }

        if (player.getMasks().requiresUpdate(MaskType.FACE_DIRECTION) || (newPlayer && player.getMasks().getFaceDirection() != Mask.MASK_NULL)) {
            doFaceDirection(player, fb);
        }
        if (player.getMasks().requiresUpdate(MaskType.FORCED_CHAT)) {
            doForcedChat(player, fb);
        }
        if (player.getMasks().requiresUpdate(MaskType.GRAPHICS2)) {
            doGraphics2(player, fb);
        }
        if (player.getMasks().requiresUpdate(MaskType.MOVEMENT_MODE) || newPlayer) {
            doMovementMode(player, fb);
        }
        if (player.getMasks().requiresUpdate(MaskType.FACE_ENTITY) || (newPlayer && player.getMasks().getFaceEntity() != Mask.MASK_NULL)) {
            doFaceEntity(player, fb);
        }
        if (player.getMasks().requiresUpdate(MaskType.GRAPHICS3)) {
            doGraphics3(player, fb);
        }
        if (player.getMasks().requiresUpdate(MaskType.ANIMATION)) {
            doAnimation(player, fb);
        }
        if (player.getMasks().requiresUpdate(MaskType.APPEARANCE) || newPlayer) {
            doAppearance(player, fb);
        }
        if (player.getMasks().requiresUpdate(MaskType.MOVEMENT)) {
            doForcedMovement(player, fb);
        }
        if (player.getMasks().requiresUpdate(MaskType.FORCED_MOVEMENT_MODE)) {
            doForcedMovementMode(player, fb);
        }
        if (player.getMasks().requiresUpdate(MaskType.HIT)) {
            doHit(owner, player, fb);
        }
        if (player.getMasks().requiresUpdate(MaskType.GRAPHICS)) {
            doGraphics(player, fb);
        }

        return fb.toFrame();
    }

    private void doForcedMovement(Player player, FrameBuilder fb) {
        Movement movement = player.getMasks().getMovement();
        fb.writeByteS(movement.getX1());
        fb.writeByteA(movement.getY1());
        fb.writeByteC(movement.getX2());
        fb.writeByteA(movement.getY2());
        fb.writeShortA(movement.getSpeed1());
        fb.writeLEShortA(movement.getSpeed2());
        fb.writeByte(movement.getDirection());
    }

    private void doGraphics3(Player player, FrameBuilder fb) {
        Graphic graphic = player.getMasks().getGraphic3();
        fb.writeLEShortA(graphic.getId()).writeInt(graphic.getSettings()).writeByte(graphic.getDirection());
    }

    private void doForcedChat(Player player, FrameBuilder fb) {
        Chat chat = player.getMasks().getForcedChat();
        fb.writeString(chat.getText());
    }

    private void doHit(Player owner, Player player, FrameBuilder fb) {
        Queue<SplatNode> splats = player.getMasks().getSplatQueue();
        if (splats.isEmpty()) {
            fb.writeByteC(1);
            fb.writeSmart(32766);
            fb.writeSmart(0);
            int lifePoints = player.getHP();
            int maxLifePoints = player.getLevels().getLevel(Levels.CONSTITUTION) * 10;
            if (lifePoints > maxLifePoints)
                lifePoints = maxLifePoints;
            double d = (double) lifePoints / (double) maxLifePoints;
            int hpRatio = (int) ((Math.round(d * 100)) * 255 / 100); 
            fb.writeByteA(hpRatio);
        } else {
            fb.writeByteC(splats.size()); // count of hits

            int hp = player.getHP();
            for (Iterator<SplatNode> it = splats.iterator(); it.hasNext(); ) {
                SplatNode splatNode = it.next();

                Splat splat = splatNode.getSplat1();
                Splat splat2 = splatNode.getSplat2();

                hp -= splat.getAmount();
                if (splat2 != null) {
                    hp -= splat2.getAmount();

                    fb.writeSmart(32767);

                    fb.writeSmart(splat.typePerspectiveOfSpectator(owner));
                    fb.writeSmart(splat.getAmount());

                    fb.writeSmart(splat2.typePerspectiveOfSpectator(owner));
                    fb.writeSmart(splat2.getAmount());
                } else {
                    fb.writeSmart(splat.typePerspectiveOfSpectator(owner));
                    fb.writeSmart(splat.getAmount());
                }

                fb.writeSmart(splat.getDelay()); // ??
                int lifePoints = player.getHP();
                int maxLifePoints = player.getLevels().getLevel(Levels.CONSTITUTION) * 10;
                if (lifePoints > maxLifePoints)
                    lifePoints = maxLifePoints;
                double d = (double) lifePoints / (double) maxLifePoints;
                int hpRatio = (int) ((Math.round(d * 100)) * 255 / 100); 
                fb.writeByteA(hpRatio);
            }
        }
    }

    private void doGraphics2(Player player, FrameBuilder fb) {
        Graphic graphic = player.getMasks().getGraphic2();
        fb.writeLEShortA(graphic.getId()).writeInt1(graphic.getSettings()).writeByteS(graphic.getDirection());
    }

    private void doGraphics(Player player, FrameBuilder fb) {
        Graphic graphic = player.getMasks().getGraphic();
        fb.writeLEShortA(graphic.getId()).writeInt1(graphic.getSettings()).writeByteS(graphic.getDirection());
    }

    private void doFaceDirection(Player player, FrameBuilder fb) {
        fb.writeLEShort(player.getMasks().getFaceDirection());
    }

    private void doFaceEntity(Player player, FrameBuilder fb) {
        fb.writeShortA(player.getMasks().getFaceEntity());
    }

    private void doAnimation(Player player, FrameBuilder fb) {
        int id = player.getMasks().getAnimationId();
        int delay = player.getMasks().getAnimationDelay();
        for (int i = 0; i < 4; i++) {
            fb.writeLEShort(id);
        }
        fb.writeByteA(delay);
    }

    private void doForcedMovementMode(Player player, FrameBuilder fb) {
        fb.writeByteC(player.getMasks().getForcedMovementMode());
    }

    private void doMovementMode(Player player, FrameBuilder fb) {
        fb.writeByteS(player.getMovementMode());
    }
    /* (non-Javadoc)
      * @see EntityUpdater#update(Player)
      */

    @Override
    public void update(Player player) {
        FrameBuilder fb = new FrameBuilder(113, FrameType.VAR_SHORT, 1024);
        /**
         * NSN0
         */
        fb.calculateBitPosition();
        int skip = -1;
        for (int localIndex = 0; localIndex < player.gei.localPlayerCount; localIndex++) {
            int worldIndex = player.gei.localPlayers[localIndex];
            if ((player.gei.playerData[worldIndex] & GEI.SKIPPED_LAST_CYCLE) == 0) {
                Player p2 = players[worldIndex];
                if (!updateLocalPlayer(player, p2, worldIndex, fb, skip)) {
                    player.gei.playerData[p2.getIndex()] |= GEI.SKIPPED_THIS_CYCLE;
                    skip++;
                } else {
                    skip = -1;
                }
            }
        }
        writeSkip(skip, fb);
        /**
         * NSN1
         */
        fb.calculateBitPosition();
        skip = -1;
        for (int localIndex = 0; localIndex < player.gei.localPlayerCount; localIndex++) {
            int worldIndex = player.gei.localPlayers[localIndex];
            if ((player.gei.playerData[worldIndex] & GEI.SKIPPED_LAST_CYCLE) != 0) {
                Player p2 = players[worldIndex];
                if (!updateLocalPlayer(player, p2, worldIndex, fb, skip)) {
                    player.gei.playerData[p2.getIndex()] |= GEI.SKIPPED_THIS_CYCLE;
                    skip++;
                } else {
                    skip = -1;
                }
            }
        }
        writeSkip(skip, fb);
        /**
         * NSN2
         */
        fb.calculateBitPosition();
        skip = -1;
        for (int privIndex = 0; privIndex < player.gei.nonLocalPlayerCount; privIndex++) {
            int worldIndex = player.gei.nonLocalPlayers[privIndex];
            if ((player.gei.playerData[worldIndex] & GEI.SKIPPED_LAST_CYCLE) != 0) {
                Player p2 = players[worldIndex];
                if (p2 == null || p2.isOnLogin()) {
                    player.gei.playerData[worldIndex] |= GEI.SKIPPED_THIS_CYCLE;
                    skip++;
                    continue;
                }
                if (player.getLocation().withinRange(p2.getLocation(), player.gei.playerViewportSize)) {
                    writeSkip(skip, fb);
                    skip = -1;
                    updateGlobalPlayer(player, p2, 0, fb, false);
                } else if (p2.isTeleporting() || p2.getTimeLoggedIn() == Static.world.getTime() || (player.isFirstCycle() && p2.getTimeLoggedIn() == player.getTimeLoggedIn())) {
                    writeSkip(skip, fb);
                    skip = -1;
                    updateGlobalPlayer(player, p2, 3, fb, false);
                } else if (p2.getMapRegionDirection() != null) {
                    writeSkip(skip, fb);
                    skip = -1;
                    updateGlobalPlayer(player, p2, 2, fb, false);
                } else if (p2.isHeightUpdate()) {
                    writeSkip(skip, fb);
                    skip = -1;
                    updateGlobalPlayer(player, p2, 1, fb, false);
                } else {
                    player.gei.playerData[worldIndex] |= GEI.SKIPPED_THIS_CYCLE;
                    skip++;
                }
            }
        }
        writeSkip(skip, fb);
        /**
         * NSN3
         */
        fb.calculateBitPosition();
        skip = -1;
        for (int privIndex = 0; privIndex < player.gei.nonLocalPlayerCount; privIndex++) {
            int worldIndex = player.gei.nonLocalPlayers[privIndex];
            if ((player.gei.playerData[worldIndex] & GEI.SKIPPED_LAST_CYCLE) == 0) {
                Player p2 = players[worldIndex];
                if (p2 == null || p2.isOnLogin()) {
                    player.gei.playerData[worldIndex] |= GEI.SKIPPED_THIS_CYCLE;
                    skip++;
                    continue;
                }
                if (player.getLocation().withinRange(p2.getLocation(), player.gei.playerViewportSize)) {
                    writeSkip(skip, fb);
                    skip = -1;
                    updateGlobalPlayer(player, p2, 0, fb, false);
                } else if (p2.isTeleporting() || p2.getTimeLoggedIn() == Static.world.getTime() || (player.isFirstCycle() && p2.getTimeLoggedIn() == player.getTimeLoggedIn())) {
                    writeSkip(skip, fb);
                    skip = -1;
                    updateGlobalPlayer(player, p2, 3, fb, false);
                } else if (p2.getMapRegionDirection() != null) {
                    writeSkip(skip, fb);
                    skip = -1;
                    updateGlobalPlayer(player, p2, 2, fb, false);
                } else if (p2.isHeightUpdate()) {
                    writeSkip(skip, fb);
                    skip = -1;
                    updateGlobalPlayer(player, p2, 1, fb, false);
                } else {
                    player.gei.playerData[worldIndex] |= GEI.SKIPPED_THIS_CYCLE;
                    skip++;
                }
            }
        }
        writeSkip(skip, fb);
        /**
         * Masks
         */
        fb.calculateBitPosition();
        for (int privIndex = 0; privIndex < player.gei.playerUpdatesRequiredCount; privIndex++) {
            int worldIndex = player.gei.playerUpdatesRequired[privIndex];
            Player p2 = players[worldIndex];
            boolean justEnteredViewport = (player.gei.playerData[worldIndex] & GEI.CROSSED_VIEWPORT_MASK) != 0;
            if (p2.getMasks().requiresUpdate() || justEnteredViewport) {
                masks(player, p2, fb, justEnteredViewport);
            }
        }
        player.getSession().write(fb.toFrame());

        player.setFirstCycle(false);
    }

    private void updateGlobalPlayer(Player owner, Player player, int stage, FrameBuilder fb, boolean repeated) {
        if (!repeated) {
            fb.writeBits(1, 1);
        }
        fb.writeBits(2, stage);
        switch (stage) {
            case 0:
                if (player.isTeleporting() || player.getTimeLoggedIn() == Static.world.getTime() || (owner.isFirstCycle() && player.getTimeLoggedIn() == owner.getTimeLoggedIn())) {
                    fb.writeBits(1, 1);
                    updateGlobalPlayer(owner, player, 3, fb, true);
                } else if (player.getMapRegionDirection() != null) {
                    fb.writeBits(1, 1);
                    updateGlobalPlayer(owner, player, 2, fb, true);
                } else if (player.isHeightUpdate()) {
                    fb.writeBits(1, 1);
                    updateGlobalPlayer(owner, player, 1, fb, true);
                } else {
                    fb.writeBits(1, 0);
                }
                int x = player.getLocation().getX() - (player.getLocation().getRegionX() << 6);
                int y = player.getLocation().getY() - (player.getLocation().getRegionY() << 6);
                fb.writeBits(6, x);
                fb.writeBits(6, y);
                fb.writeBits(1, 1); // requires update, TODO: we could disable this
                // and make the client use it's cached
                // appearance block if the player hasn't changed
                // appearance yet
                owner.gei.playerData[player.getIndex()] |= GEI.IN_VIEWPORT_MASK | GEI.CROSSED_VIEWPORT_MASK | GEI.SKIPPED_THIS_CYCLE;
                owner.gei.playerUpdatesRequired[owner.gei.playerUpdatesRequiredCount++] = (short) player.getIndex();
                break;
            case 1:
                int hashLoc = owner.gei.playerLocations[player.getIndex()];
                int z = (hashLoc >> 16) & 0x3;
                z = player.getLocation().getZ() - z & 0x3;
                fb.writeBits(2, z);
                owner.gei.updateLocation(player);
                break;
            case 2:
                hashLoc = owner.gei.playerLocations[player.getIndex()];
                z = (hashLoc >> 16) & 0x3;
                z = player.getLocation().getZ() - z & 0x3;
                fb.writeBits(5, z << 3 | (player.getMapRegionDirection().intValue() & 0x7));
                owner.gei.updateLocation(player);
                break;
            case 3:
                hashLoc = owner.gei.playerLocations[player.getIndex()];
                z = (hashLoc >> 16) & 0x3;
                x = (hashLoc >> 8) & 0xff;
                y = hashLoc & 0xff;
                x = player.getLocation().getRegionX() - x & 0xff;
                y = player.getLocation().getRegionY() - y & 0xff;
                z = player.getLocation().getZ() - z & 0x3;
                fb.writeBits(18, z << 16 | x << 8 | y);
                owner.gei.updateLocation(player);
                break;
        }
    }

    private void updateLocalPlayer(Player owner, Player player, int index, int stage, boolean remove, FrameBuilder fb) {
        fb.writeBits(1, 1);
        fb.writeBits(1, remove ? 0 : (player.getMasks().requiresUpdate() ? 1 : 0));
        fb.writeBits(2, stage);
        switch (stage) {
            case 0:
                if (remove) {
                    if (player != null) {
                        if (player.isTeleporting()) {
                            fb.writeBits(1, 1);
                            updateGlobalPlayer(owner, player, 3, fb, true);
                        } else if (player.getMapRegionDirection() != null) {
                            fb.writeBits(1, 1);
                            updateGlobalPlayer(owner, player, 2, fb, true);
                        } else if (player.isHeightUpdate()) {
                            fb.writeBits(1, 1);
                            updateGlobalPlayer(owner, player, 1, fb, true);
                        } else {
                            fb.writeBits(1, 0);
                        }
                    } else {
                        fb.writeBits(1, 0);
                    }
                    owner.gei.playerData[index] &= ~GEI.IN_VIEWPORT_MASK;
                    owner.gei.playerData[index] |= GEI.CROSSED_VIEWPORT_MASK;
                }
                break;
            case 1:
                fb.writeBits(3, player.getDirections().getDirection().intValue());
                owner.gei.updateLocation(player);
                break;
            case 2:
                fb.writeBits(4, player.getDirections().getDirection().intValue());
                owner.gei.updateLocation(player);
                break;
            case 3:
                fb.writeBits(1, owner != player ? 0 : 1);
                if (owner != player) {
                    int x = player.getLocation().getX() - player.getPreviousLocation().getX() & 0x1f;
                    int y = player.getLocation().getY() - player.getPreviousLocation().getY() & 0x1f;
                    int z = player.getLocation().getZ() - player.getPreviousLocation().getZ() & 0x3;
                    fb.writeBits(12, z << 10 | x << 5 | y);
                } else {
                    int x = player.getLocation().getX() - player.getPreviousLocation().getX() & 0x3fff;
                    int y = player.getLocation().getY() - player.getPreviousLocation().getY() & 0x3fff;
                    int z = player.getLocation().getZ() - player.getPreviousLocation().getZ() & 0x3;
                    fb.writeBits(30, z << 28 | x << 14 | y);
                }
                owner.gei.updateLocation(player);
                break;
        }
    }

    /**
     * For updating a local player.
     * <p/>
     * Used int NSN0 and NSN1.
     *
     * @param owner  The player we are updating.
     * @param player A local player.
     * @param fb     The message factory to write with.
     * @return If the block was written.
     */
    private boolean updateLocalPlayer(Player owner, Player player, int index, FrameBuilder fb, int skip) {
        if (player == null || (owner != player && (!player.isValid() || player.isDestroyed() || (owner.isTeleporting() && player.isTeleporting()) || !owner.getLocation().withinRange(player.getLocation(), owner.gei.playerViewportSize)))) {
            writeSkip(skip, fb);
            updateLocalPlayer(owner, player, index, 0, true, fb);
        } else {
            if (player.isTeleporting() && (!player.isForcedTeleporting() || owner == player)) {
                writeSkip(skip, fb);
                updateLocalPlayer(owner, player, index, 3, false, fb);
            } else if (player.getDirections().getDirection() != null) {
                writeSkip(skip, fb);
                if (player.getDirections().getDirection() instanceof RunningDirection) {
                    updateLocalPlayer(owner, player, index, 2, false, fb);
                } else {
                    updateLocalPlayer(owner, player, index, 1, false, fb);
                }
            } else {
                if (player.getMasks().requiresUpdate()) {
                    writeSkip(skip, fb);
                    updateLocalPlayer(owner, player, index, 0, false, fb);
                } else {
                    return false;
                }
            }
            if (player.getMasks().requiresUpdate()) {
                owner.gei.playerUpdatesRequired[owner.gei.playerUpdatesRequiredCount++] = (short) player.getIndex();
            }
        }
        return true;
    }

    /**
     * Writes an amount of loops to skip.
     *
     * @param skip The amount of loops to skip.
     * @param fb   The message factory to write with.
     */
    private void writeSkip(int skip, FrameBuilder fb) {
        if (skip > -1) {
            int type = 0;
            if (skip != 0) {
                if (skip < 32) {
                    type = 1;
                } else if (skip < 256) {
                    type = 2;
                } else if (skip < 2048) {
                    type = 3;
                } else {
                    throw new IllegalArgumentException("Skip count cannot be over 2047!");
                }
            }
            fb.writeBits(1, 0);
            fb.writeBits(2, type);
            switch (type) {
                case 1:
                    fb.writeBits(5, skip);
                    break;
                case 2:
                    fb.writeBits(8, skip);
                    break;
                case 3:
                    fb.writeBits(11, skip);
                    break;
            }
        }
    }
}
