package com.ziotic.link.network;

import com.ziotic.Static;
import com.ziotic.network.Frame;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;

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
