package com.ziotic.network;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.ziotic.Static;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;

/**
 * @author Lazaro
 */
public class ConnectionHandler extends IoHandlerAdapter {
    private static Logger logger = Logging.log();

    @Override
    public void sessionCreated(IoSession session) {
        logger.info("Channel connected <" + session.getRemoteAddress().toString() + ">");
    }

    @Override
    public void sessionClosed(IoSession session) {
        final Player player = (Player) session.getAttribute("player");
        if (player != null) {
            Static.world.unregister(player);
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
            if(session.isConnected()) {            	
                session.close(true);
            }
        }
    }
}