package com.runescape.link.network;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;

import com.runescape.Static;
import com.runescape.network.Frame;

/**
 * @author Lazaro
 */
public class LFrameDispatcher extends IoFilterAdapter {
    public static final LFrameDispatcher INSTANCE = new LFrameDispatcher();

    @Override
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message)  {
        if (message instanceof Frame) {
            Frame frame = (Frame) message;
            /**
             * Instead of dispatching the frame, we execute it directly in the
             * link server.
             *
             * This is because the frames being handled in this server do not
             * use much CPU and return quickly.
             */
            Static.frameManager.getHandler(frame.getOpcode()).handleFrame(session, frame);
        }
    }
}
