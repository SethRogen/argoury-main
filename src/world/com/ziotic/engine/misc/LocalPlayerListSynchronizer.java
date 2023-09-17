package com.ziotic.engine.misc;

import com.ziotic.Static;
import com.ziotic.engine.event.RecurringEvent;
import com.ziotic.link.WorldClientSession;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

public class LocalPlayerListSynchronizer extends RecurringEvent {
    private static final Logger log = Logging.log();

    public LocalPlayerListSynchronizer() {
        super(15000, ExecutorType.SERVICE_WORKER);
    }

    @Override
    public void run() {
        WorldClientSession session = Static.world.getWCSPool().acquire();
        try {
            session.synchronizeLocalPlayerList();
        } catch (Exception e) {
            log.error("Exception caught on player list synchronize!", e);
        }
        Static.world.getWCSPool().release(session);
    }
}
