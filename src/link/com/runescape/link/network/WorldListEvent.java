package com.runescape.link.network;

import com.runescape.Static;
import com.runescape.engine.event.RecurringEvent;
import com.runescape.link.WorldServerSession;
import com.runescape.network.Frame;

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
