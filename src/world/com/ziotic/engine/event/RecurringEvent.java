package com.ziotic.engine.event;

import com.ziotic.Static;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Lazaro
 */
public abstract class RecurringEvent extends Event {
    private int initialDelay;
    private int recurringDelay;
    private ExecutorType executor;
    private boolean running = true;
    protected ScheduledFuture<?> schedule = null;

    public RecurringEvent(int delay) {
        this(delay, delay);
    }

    public RecurringEvent(int initialDelay, int recurringDelay) {
        this(initialDelay, recurringDelay, ExecutorType.ASYNC_LOGIC);
    }

    public RecurringEvent(int delay, ExecutorType executor) {
        this(delay, delay, executor);
    }

    public RecurringEvent(int initialDelay, int recurringDelay, ExecutorType executor) {
        this.initialDelay = initialDelay;
        this.recurringDelay = recurringDelay;
        this.executor = executor;
    }

    public int initialDelay() {
        return initialDelay;
    }

    @Override
    public int delay() {
        return recurringDelay;
    }

    @Override
    public ExecutorType executor() {
        return executor;
    }

    @Override
    public boolean finished() {
        return !running;
    }

    public void changeDelay(int delay) {
        this.recurringDelay = delay;
        if (schedule != null) {
            schedule.cancel(false);
        }
        Static.engine.scheduleRecurringEvent(this);
    }

    public void stop() {
        running = false;
        if (schedule != null) {
            schedule.cancel(false);
        } else {
            Static.engine.submit(new DelayedEvent(30) {
                @Override
                public void run() {
                    stop();
                }
            });
        }
    }

    public void setSchedule(ScheduledFuture<?> schedule) {
        this.schedule = schedule;
    }
}
