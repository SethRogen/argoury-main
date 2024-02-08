package com.runescape.adapter.protocol.handler;

import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;

import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class PingHandler extends PlayerFrameHandler {
    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        if (System.currentTimeMillis() - player.getLastPing() >= 5000) {
            player.sendPing();
        }
    }
}
