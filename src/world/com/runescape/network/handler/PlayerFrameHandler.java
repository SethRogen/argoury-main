package com.runescape.network.handler;

import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.FrameHandler;

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
