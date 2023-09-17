package com.ziotic.link.network;

import com.ziotic.engine.event.Event;
import com.ziotic.engine.event.RecurringEvent;

public class RemovePlayerListSynchronizer extends RecurringEvent {
    public RemovePlayerListSynchronizer() {
        super(15000, Event.ExecutorType.SERVICE_WORKER);
    }

    @Override
    public void run() {

    }
}
