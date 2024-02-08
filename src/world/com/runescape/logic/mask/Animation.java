package com.runescape.logic.mask;

public class Animation {
    private int id;
    private int delay;

    public Animation(int id) {
        this(id, 0);
    }

    public Animation(int id, int delay) {
        this.id = id;
        this.delay = delay;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the delay
     */
    public int getDelay() {
        return delay;
    }


}
