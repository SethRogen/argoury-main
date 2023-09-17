package com.ziotic.adapter.protocol.handler;

import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.handler.PlayerFrameHandler;
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
