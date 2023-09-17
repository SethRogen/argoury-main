package com.ziotic.link.network;

import com.ziotic.Static;
import com.ziotic.engine.event.RecurringEvent;
import com.ziotic.link.WorldServerSession;
import com.ziotic.network.Frame;
import org.apache.mina.core.session.IoSession;

public class WorldListEvent extends RecurringEvent {
    public WorldListEvent() {
        super(1000, ExecutorType.SERVICE_WORKER);
    }

    @Override
    public void run() {
        Frame frame = Static.proto.generateWorldList();
        for (WorldServerSession w : Static.currentLink().getLobbies()) {
            IoSession session = w.getSession();
            if (session != null) {
                session.write(frame);
            }
        }
        for (WorldServerSession w : Static.currentLink().getGames()) {
            if (w.getServerType() == 2) {
                IoSession session = w.getSession();
                if (session != null) {
                    session.write(frame);
                }
            }
        }
    }
}
