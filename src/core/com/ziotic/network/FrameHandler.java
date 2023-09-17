package com.ziotic.network;

import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public interface FrameHandler {
    public void handleFrame(IoSession session, Frame frame);
}
