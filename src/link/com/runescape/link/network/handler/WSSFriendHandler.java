package com.runescape.link.network.handler;

import com.runescape.Static;
import com.runescape.link.WorldServerSession;
import com.runescape.network.Frame;

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
