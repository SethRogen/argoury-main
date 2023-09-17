package com.ziotic.link.network.handler;

import com.ziotic.link.WorldServerSession;
import com.ziotic.network.Frame;
import com.ziotic.network.FrameHandler;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public abstract class WSSFrameHandler implements FrameHandler {
    @Override
    public void handleFrame(IoSession session, Frame frame) {
        WorldServerSession world = (WorldServerSession) session.getAttribute("world");

        handleFrame(world, session, frame);
    }

    public abstract void handleFrame(WorldServerSession world, IoSession session, Frame frame);
}
