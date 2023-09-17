package com.ziotic.network.handler;

import com.ziotic.network.Frame;
import com.ziotic.network.FrameHandler;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class DebugFrameHandler implements FrameHandler {
    private static Logger logger = Logging.log();

    public void handleFrame(IoSession session, Frame frame) {
        logger.debug("Unhandled frame [" + frame.toString() + "]");
    }
}
