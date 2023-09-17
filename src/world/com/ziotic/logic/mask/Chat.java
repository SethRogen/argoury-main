package com.ziotic.logic.mask;

/**
 * @author Lazaro
 */
public class Chat implements Mask {
    private final int color, effect;
    private final String text;

    public Chat(String text, int color, int effect) {
        this.text = text;
        this.color = color;
        this.effect = effect;
    }

    public int getColor() {
        return color;
    }

    public int getEffect() {
        return effect;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
