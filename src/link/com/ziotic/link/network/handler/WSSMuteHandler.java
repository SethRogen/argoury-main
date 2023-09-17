package com.ziotic.link.network.handler;

import com.ziotic.Static;
import com.ziotic.link.WorldServerSession;
import com.ziotic.network.Frame;
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
