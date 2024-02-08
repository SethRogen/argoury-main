package com.runescape.engine.tick;

/**
 * @author Lazaro
 */
public class RunnableTick extends Tick {
    private Runnable runnable;

    public RunnableTick(Runnable runnable) {
        this(null, runnable);
    }

    public RunnableTick(Runnable runnable, int delay) {
        this(null, runnable, delay);
    }

    public RunnableTick(String identifier, Runnable runnable) {
        super(identifier);
        this.runnable = runnable;
    }

    public RunnableTick(String identifier, Runnable runnable, int delay) {
        super(identifier, delay);
        this.runnable = runnable;
    }

    @Override
    public boolean execute() {
        runnable.run();
        return running();
    }
}
