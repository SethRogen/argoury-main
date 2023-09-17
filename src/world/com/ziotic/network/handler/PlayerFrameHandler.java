package com.ziotic.network.handler;

import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.FrameHandler;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public abstract class PlayerFrameHandler implements FrameHandler {
    @Override
    public void handleFrame(IoSession session, Frame frame) {
        Player player = (Player) session.getAttribute("player");

        handleFrame(player, session, frame);
    }

    public abstract void handleFrame(Player player, IoSession session, Frame frame);
}
