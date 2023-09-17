package com.ziotic.network.handler;

import com.ziotic.network.Frame;
import com.ziotic.network.FrameHandler;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class SilentFrameHandler implements FrameHandler {
    @Override
    public void handleFrame(IoSession session, Frame frame) {
    }
}
