package com.ziotic.logic.mask;

/**
 * @author Lazaro
 */
public interface Mask {
    public static enum MaskType {
        ANIMATION, APPEARANCE, FACE_DIRECTION, FACE_ENTITY, FORCED_CHAT, FORCED_MOVEMENT_MODE, GRAPHICS, GRAPHICS2, GRAPHICS3, HIT, MOVEMENT_MODE, MOVEMENT
    }

    public static final int MASK_NULL = -2;
    public static final int MASK_RESET = -1;
}
