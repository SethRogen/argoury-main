package com.ziotic.adapter.protocol.handler;

import com.ziotic.Static;
import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.handler.PlayerFrameHandler;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class WorldListHandler extends PlayerFrameHandler {
    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        player.setOnLogin(false);

        Static.proto.sendWorldList(session);
    }
}
