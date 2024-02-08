package com.runescape.link.network.handler;

import com.runescape.link.WorldServerSession;
import com.runescape.network.Frame;
import com.runescape.network.FrameHandler;

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
