package com.runescape.link.network;

import com.runescape.engine.event.Event;
import com.runescape.engine.event.RecurringEvent;

public class RemovePlayerListSynchronizer extends RecurringEvent {
    public RemovePlayerListSynchronizer() {
        super(15000, Event.ExecutorType.SERVICE_WORKER);
    }

    @Override
    public void run() {

    }
}
