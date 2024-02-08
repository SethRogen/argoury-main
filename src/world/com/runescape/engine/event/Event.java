package com.runescape.engine.event;

/**
 * @author Lazaro
 */
public abstract class Event implements Runnable {
    public static enum ExecutorType {
        ASYNC_LOGIC, PARALLEL_LOGIC, SERVICE_WORKER
    }

    /**
     * Gets the delay between executing this task.
     *
     * @return The delay between executing this task.
     */
    public abstract int delay();

    /**
     * Gets the desired executor type for this task.
     *
     * @return The desired executor type for this task.
     */
    public abstract ExecutorType executor();

    /**
     * Gets whether or not this task is finished or not.
     *
     * @return Whether or not this task is finished or not.
     */
    public abstract boolean finished();
}
