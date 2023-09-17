package com.ziotic.link.network;

import com.ziotic.adapter.protocol.HandshakeCodec;
import com.ziotic.link.WorldServerSession;
import com.ziotic.network.StandardFrameEncoder;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.io.IOException;

/**
 * @author Lazaro
 */
public class LConnectionHandler extends IoHandlerAdapter {
    private static Logger logger = Logging.log();

    @Override
    public void sessionCreated(IoSession session) {
        logger.info("Channel connected <" + session.getRemoteAddress().toString() + ">");
    }

    @Override
    public void sessionClosed(IoSession session) {
        WorldServerSession world = (WorldServerSession) session.getAttribute("world");
        if (world != null) {
            world.removeSession(session);
            if (!world.isOnline()) {
                world.destroy();
            }
        }

        logger.info("Channel disconnected <" + session.getRemoteAddress().toString() + ">");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) {
        session.close(false);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        if (!(cause instanceof IOException)) {
            logger.error("Exception caught in networking!", cause);
        } else {
            session.close(false);
        }
    }
}
