package com.runescape.network.handler;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import com.runescape.network.Frame;
import com.runescape.network.FrameHandler;
import com.runescape.utility.Logging;

/**
 * @author Lazaro
 */
public class DebugFrameHandler implements FrameHandler {
    private static Logger logger = Logging.log();

    public void handleFrame(IoSession session, Frame frame) {
        logger.debug("Unhandled frame [" + frame.toString() + "]");
    }
}
