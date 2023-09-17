package com.ziotic.adapter.protocol.update;

import java.util.Iterator;
import java.util.Queue;

import com.ziotic.Static;
import com.ziotic.logic.mask.Graphic;
import com.ziotic.logic.mask.Mask;
import com.ziotic.logic.mask.Splat;
import com.ziotic.logic.mask.SplatNode;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.utility.EntityUpdater;
import com.ziotic.network.Frame;
import com.ziotic.network.Frame.FrameType;
import com.ziotic.network.FrameBuilder;

/**
 * @author Lazaro
 */
public final class NPCUpdateAdapter implements EntityUpdater<NPC> {
    private void doAnimation(NPC npc, FrameBuilder fb) {
        int id = npc.getMasks().getAnimationId();
        int delay = npc.getMasks().getAnimationDelay();
        for (int i = 0; i < 4; i++) {
            fb.writeShortA(id);
        }
        fb.writeByteS(delay);
    }

    private void doForcedChat(NPC npc, FrameBuilder fb) {
        fb.writeString(npc.getMasks().getForcedChat().getText());
    }

    private void doFaceEntity(NPC npc, FrameBuilder fb) {
        fb.writeShortA(npc.getMasks().getFaceEntity());
    }

    private void doGraphics(NPC npc, FrameBuilder fb) {
        Graphic graphic = npc.getMasks().getGraphic();

        fb.writeShortA(graphic.getId()).writeLEInt(graphic.getSettings()).writeByte(graphic.getDirection());
    }

    private void doGraphics2(NPC npc, FrameBuilder fb) {
        Graphic graphic = npc.getMasks().getGraphic2();

        fb.writeLEShort(graphic.getId()).writeInt2(graphic.getSettings()).writeByteC(graphic.getDirection());
    }

    private void doGraphics3(NPC npc, FrameBuilder fb) {
        Graphic graphic = npc.getMasks().getGraphic2();

        fb.writeLEShort(graphic.getId()).writeInt2(graphic.getSettings()).writeByteC(graphic.getDirection());
    }

    private void doHit(Player owner, NPC npc, FrameBuilder fb) {
        Queue<SplatNode> splats = npc.getMasks().getSplatQueue();
        if (splats.isEmpty()) {
            fb.writeByteA(1);
            fb.writeSmart(32766);
            fb.writeSmart(0);            
            int lifePoints = npc.getHP();
            int maxLifePoints = npc.getMaxHP();
            if (lifePoints > maxLifePoints)
            	lifePoints = maxLifePoints;
            double d = (double) lifePoints / (double) maxLifePoints;
            int hpRatio = (int) ((Math.round(d * 100)) * 255 / 100); 
            fb.writeByteS(hpRatio);
 
        } else {
            fb.writeByteA(splats.size()); // count of hits

            int hp = npc.getHP();
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

                fb.writeSmart(0); // ??                
                int lifePoints = npc.getHP();
                int maxLifePoints = npc.getMaxHP();
                if (lifePoints > maxLifePoints)
                	lifePoints = maxLifePoints;
                double d = (double) lifePoints / (double) maxLifePoints;
                int hpRatio = (int) ((Math.round(d * 100)) * 255 / 100); 
                fb.writeByteS(hpRatio);
            }
        }
    }

    public Frame doMaskBlock(Player owner, NPC npc) {
        if (owner == null && npc.getMasks().requiresUpdate(Mask.MaskType.HIT)) {
            return null;
        }

        FrameBuilder fb = new FrameBuilder(256);
        int mask = 0;

        if (npc.getMasks().requiresUpdate(Mask.MaskType.HIT)) {
            mask |= 0x40;
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.GRAPHICS2)) {
            mask |= 0x400;
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.ANIMATION)) {
            mask |= 0x8;
        }
        if (npc.getMasks().getSwitchId() > -1) {
            mask |= 0x10;
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.GRAPHICS3)) {
            mask |= 0x10000;
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.GRAPHICS)) {
            mask |= 0x2;
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.FACE_ENTITY)) {
            mask |= 0x20;
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.FORCED_CHAT)) {
            mask |= 0x80;
        }

        if (npc.getMasks().requiresUpdate(Mask.MaskType.FACE_DIRECTION)) {
            // mask |= 0x80;
        }

        if (mask >= 0x100) {
            mask |= 0x4;
            if (mask >= 0x10000) {
                mask |= 0x8000;
                fb.writeByte(mask & 0xff).writeByte(mask >> 8 & 0xffff).writeByte(mask >> 16);
            } else {
                fb.writeByte(mask & 0xff).writeByte(mask >> 8);
            }
        } else {
            fb.writeByte(mask & 0xff);
        }

        if (npc.getMasks().requiresUpdate(Mask.MaskType.HIT)) {
            doHit(owner, npc, fb);
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.GRAPHICS2)) {
            doGraphics2(npc, fb);
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.ANIMATION)) {
            doAnimation(npc, fb);
        }
        if (npc.getMasks().getSwitchId() > -1) {
            doSwitch(npc, fb);
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.GRAPHICS2)) {
            doGraphics3(npc, fb);
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.GRAPHICS)) {
            doGraphics(npc, fb);
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.FACE_ENTITY)) {
            doFaceEntity(npc, fb);
        }
        if (npc.getMasks().requiresUpdate(Mask.MaskType.FORCED_CHAT)) {
            doForcedChat(npc, fb);
        }

        if (npc.getMasks().requiresUpdate(Mask.MaskType.FACE_DIRECTION)) {
            // doFaceDirection(npc, fb);
        }

        return fb.toFrame();
    }

    private void doSwitch(NPC npc, FrameBuilder fb) {
        fb.writeShort(npc.getMasks().getSwitchId());
    }

    private void doFaceDirection(NPC npc, FrameBuilder fb) {
        fb.writeShortA(npc.getMasks().getFaceDirection());
    }

    private void registerNPC(Player player, NPC npc, FrameBuilder fb) {
        int x = npc.getLocation().getX() - player.getLocation().getX();
        int y = npc.getLocation().getY() - player.getLocation().getY();
        if (x < 0)
            x += 32;
        if (y < 0)
            y += 32;
        fb.writeBits(15, npc.getIndex());
        fb.writeBits(1, npc.getMasks().requiresUpdate() ? 1 : 0);
        fb.writeBits(3, npc.getSpawn().direction.npcIntValue());
        fb.writeBits(2, npc.getLocation().getZ());
        fb.writeBits(14, npc.getId());
        fb.writeBits(1, 1);
        fb.writeBits(5, y);
        fb.writeBits(5, x);
        player.gei.localNPCs.add(npc);
    }

    public void update(Player player) {
        FrameBuilder fb = new FrameBuilder(117, FrameType.VAR_SHORT, 1024);
        fb.writeBits(8, player.gei.localNPCs.size());
        for (Iterator<NPC> it = player.gei.localNPCs.iterator(); it.hasNext(); ) {
            NPC npc = it.next();
            if (!npc.isVisible() || npc.isTeleporting() || !player.getLocation().withinRange(npc.getLocation())) {
                fb.writeBits(1, 1);
                fb.writeBits(2, 3);
                it.remove();
            } else {
                updateNPC(npc, fb);
            }
        }
        for (NPC npc : Static.world.getLocalNPCs(player.getLocation())) {
            if (player.gei.localNPCs.size() >= 255) {
                break;
            }
            if (player.gei.localNPCs.contains(npc) || !npc.isVisible() || player.isTeleporting()) {
                continue;
            }
            registerNPC(player, npc, fb);
        }
        fb.writeBits(15, 32767);
        for (NPC npc : player.gei.localNPCs) {
            if (npc.getMasks().requiresUpdate()) {
                mask(player, npc, fb);
            }
        }
        player.getSession().write(fb.toFrame());
    }

    private void mask(Player owner, NPC npc, FrameBuilder fb) {
        if (npc.getCachedMaskBlock() != null) {
            fb.write(npc.getCachedMaskBlock().getBytes());
        } else {
            fb.write(doMaskBlock(owner, npc).getBytes());
        }
    }

    private void updateNPC(NPC npc, FrameBuilder fb) {
        if (npc.getDirections().getDirection() == null) {
            if (npc.getMasks().requiresUpdate()) {
                fb.writeBits(1, 1);
                fb.writeBits(2, 0);
            } else {
                fb.writeBits(1, 0);
            }
        } else {
            fb.writeBits(1, 1);
            if (npc.getDirections().getSecondDirection() == null) {
                fb.writeBits(2, 1);
                fb.writeBits(3, npc.getDirections().getDirection().npcIntValue());
            } else {
                fb.writeBits(2, 1);
                fb.writeBits(1, 1);
                fb.writeBits(3, npc.getDirections().getDirection().npcIntValue());
                fb.writeBits(3, npc.getDirections().getSecondDirection().npcIntValue());
            }
            fb.writeBits(1, npc.getMasks().requiresUpdate() ? 1 : 0);
        }
    }
}
