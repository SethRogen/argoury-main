package com.runescape.link.network.handler;

import com.runescape.Static;
import com.runescape.link.WorldServerSession;
import com.runescape.network.Frame;

import org.apache.mina.core.session.IoSession;

public class WSSMuteHandler extends WSSFrameHandler {

    @Override
    public void handleFrame(WorldServerSession world, IoSession session,
                            Frame frame) {
        String moderator = frame.readString();
        String user = frame.readString();
        boolean mute = frame.read() == 1;
        Static.currentLink().handlePlayerMuting(moderator, mute, user);
    }

}
