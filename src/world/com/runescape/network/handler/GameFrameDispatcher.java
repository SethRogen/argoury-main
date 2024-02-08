package com.runescape.network.handler;

import com.runescape.logic.player.Player;
import com.runescape.network.Frame;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;

public class GameFrameDispatcher extends IoFilterAdapter {
    public static final GameFrameDispatcher INSTANCE = new GameFrameDispatcher();

    @Override
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message)  {
        if (message instanceof Frame) {
            Frame frame = (Frame) message;
            Player player = (Player) session.getAttribute("player");
            if (player != null && player.isConnected()) {
                player.queueFrame(frame);
                player.setLastPacketTime(System.currentTimeMillis());
            }
        }
    }
}
