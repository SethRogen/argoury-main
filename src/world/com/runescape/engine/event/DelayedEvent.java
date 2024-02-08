package com.runescape.engine.event;

/**
 * @author Lazaro
 */
public abstract class DelayedEvent extends Event {
    private int delay;
    private ExecutorType type;

    public DelayedEvent(int delay) {
        this(delay, ExecutorType.ASYNC_LOGIC);
    }

    public DelayedEvent(int delay, ExecutorType type) {
        this.delay = delay;
        this.type = type;
    }

    @Override
    public ExecutorType executor() {
        return type;
    }

    @Override
    public int delay() {
        return delay;
    }

    @Override
    public boolean finished() {
        return true;
    }
}
