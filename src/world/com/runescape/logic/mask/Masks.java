package com.runescape.logic.mask;

import java.util.ArrayDeque;
import java.util.Queue;

import com.runescape.logic.Entity;
import com.runescape.logic.map.Tile;

/**
 * @author Lazaro
 */
public final class Masks {
    private Entity entity;

    private int animationId = Mask.MASK_NULL;
    private int animationDelay = 0;
    private boolean appearance = false;
    private int faceDirection = Mask.MASK_NULL;
    private int faceEntity = Mask.MASK_NULL;
    private boolean initialDirectionSent = true;
    private Chat forcedChat = null;
    private boolean movementMode = false;
    private int forcedMovementMode = 0;
    private Graphic graphic, graphic2, graphic3;
    private Movement movement = null;

    private Queue<Graphic> graphicsQueue = new ArrayDeque<Graphic>();

    private Queue<SplatNode> splatQueue = new ArrayDeque<SplatNode>();
    private boolean updateHPBar = false;

    private int switchId = -1;

    public Masks(Entity entity) {
        this.entity = entity;
    }

    public void processQueuedMasks() {
        if (graphic == null)
            graphic = graphicsQueue.poll();
        if (graphic2 == null)
            graphic2 = graphicsQueue.poll();
        if (graphic3 == null)
            graphic3 = graphicsQueue.poll();
    }

    public void submitSplat(SplatNode splat) {
        splatQueue.offer(splat);
    }

    public void submitGraphics(Graphic graphic) {
        graphicsQueue.offer(graphic);
    }

    public void clearSplats() {
        splatQueue.clear();
    }

    public void clearGraphics() {
        graphicsQueue.clear();
    }

    public Queue<SplatNode> getSplatQueue() {
        return splatQueue;
    }

    public boolean shouldUpdateHPBar() {
        return updateHPBar;
    }

    public void setUpdateHPBar(boolean updateHPBar) {
        this.updateHPBar = updateHPBar;
    }

    public int getAnimationDelay() {
        return animationDelay;
    }

    public int getAnimationId() {
        return animationId;
    }

    public int getFaceDirection() {
        return faceDirection;
    }

    public int getFaceEntity() {
        return faceEntity;
    }

    public Chat getForcedChat() {
        return forcedChat;
    }

    public int getForcedMovementMode() {
        return forcedMovementMode;
    }

    public boolean requiresUpdate() {
        return switchId > -1 || animationId != Mask.MASK_NULL || appearance || forcedChat != null || !initialDirectionSent || graphic != null || graphic2 != null || graphic3 != null || splatQueue.size() > 0 || updateHPBar || movementMode || forcedMovementMode != 0 || movement != null;
    }

    public boolean requiresUpdate(Mask.MaskType mask) {
        switch (mask) {
            case ANIMATION:
                return animationId != Mask.MASK_NULL;
            case APPEARANCE:
                return appearance;
            case FORCED_CHAT:
                return forcedChat != null;
            case FACE_ENTITY:
                return faceEntity != Mask.MASK_NULL && !initialDirectionSent;
            case FACE_DIRECTION:
                return faceDirection != Mask.MASK_NULL && !initialDirectionSent;
            case GRAPHICS:
                return graphic != null;
            case GRAPHICS2:
                return graphic2 != null;
            case GRAPHICS3:
                return graphic3 != null;
            case HIT:
                return splatQueue.size() > 0 || updateHPBar;
            case MOVEMENT_MODE:
                return movementMode;
            case FORCED_MOVEMENT_MODE:
                return forcedMovementMode != 0;
            case MOVEMENT:
                return movement != null;
        }
        return false;
    }

    public void reset() {
        animationId = Mask.MASK_NULL;
        animationDelay = 0;
        appearance = false;
        forcedChat = null;
        graphic = null;
        graphic2 = null;
        graphic3 = null;
        movement = null;
        movementMode = false;
        forcedMovementMode = 0;
        switchId = -1;
        initialDirectionSent = true;
        splatQueue.clear();
        updateHPBar = false;
    }

    public void resetDirection() {
        setFaceEntity(Mask.MASK_RESET);
    }

    public void setAnimation(int id) {
        setAnimation(id, 0);
    }

    public void setAnimation(int id, int delay) {
        this.animationId = id;
        this.animationDelay = delay;
    }

    public void setAppearanceUpdate(boolean appearance) {
        this.appearance = appearance;
    }

    public void setFaceDirection(Tile loc) {
        setFaceDirection(((int) (Math.atan2(entity.getLocation().getX() - loc.getX(), entity.getLocation().getY() - loc.getY()) * 2607.5945876176133)) & 0x3fff);
    }

    public void setFaceDirection(int faceDirection) {
        faceEntity = Mask.MASK_NULL;
        initialDirectionSent = false;
        this.faceDirection = faceDirection;
    }

    public void setForceChat(String chat) {
        forcedChat = new Chat(chat, 0, 255);
    }

    public void setFaceEntity(int faceEntity) {
        faceDirection = Mask.MASK_NULL;
        initialDirectionSent = false;
        this.faceEntity = faceEntity;
    }

    public void setForcedChat(Chat forcedChat) {
        this.forcedChat = forcedChat;
    }

    public void setForcedMovementMode(int forcedMovementMode) {
        this.forcedMovementMode = forcedMovementMode;
    }

    public void setMovementModeUpdate(boolean movementMode) {
        this.movementMode = movementMode;
    }

    public Graphic getGraphic2() {
        return graphic2;
    }

    public Graphic getGraphic() {
        return graphic;
    }

    public Graphic getGraphic3() {
        return graphic3;
    }

    public Movement getMovement() {
        return movement;
    }

    public void setMovement(Movement movement) {
        this.movement = movement;
    }

    public void preventDirection() {
        this.faceDirection = Mask.MASK_NULL;
        this.faceEntity = Mask.MASK_NULL;
        this.initialDirectionSent = true;
    }

    public void setSwitchId(int switchId) {
        this.switchId = switchId;
    }

    public int getSwitchId() {
        return switchId;
    }
}
