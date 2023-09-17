package com.ziotic.logic.mask;

/**
 * @author Lazaro
 */
public class Graphic implements Mask {
    private int id;
    private int settings;
    private int direction;

    public Graphic(int id) {
        this(id, 0, 0, 0, 0);
    }

    public Graphic(int id, int delay) {
        this(id, delay, 0, 0, 0);
    }

    public Graphic(int id, int delay, int height) {
        this(id, delay, height, 0, 0);
    }

    public Graphic(int id, int delay, int height, int direction) {
        this(id, delay, height, direction, 0);
    }

    public Graphic(int id, int delay, int height, int direction, int direction2) {
        this.id = id;
        this.settings = (height << 16) | delay;
        this.direction = (direction2 << 3) | direction;
    }

    public int getId() {
        return id;
    }

    public int getSettings() {
        return settings;
    }

    public int getDirection() {
        return direction;
    }
}
