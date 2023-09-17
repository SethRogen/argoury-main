package com.ziotic.link.network.handler;

import com.ziotic.Static;
import com.ziotic.link.WorldServerSession;
import com.ziotic.network.Frame;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class WSSFriendHandler extends WSSFrameHandler {
    @Override
    public void handleFrame(WorldServerSession world, IoSession session, Frame frame) {
        String sender = frame.readString();
        int rights = frame.readUnsigned();
        String recipient = frame.readString();
        String message = frame.readString();

        Static.currentLink().sendPM(sender, rights, recipient, message);
    }
}
