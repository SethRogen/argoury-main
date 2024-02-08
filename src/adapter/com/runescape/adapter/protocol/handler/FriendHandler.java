package com.runescape.adapter.protocol.handler;

import com.runescape.Static;
import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;
import com.runescape.utility.Text;

import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class FriendHandler extends PlayerFrameHandler {
    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        switch (frame.getOpcode()) {
            case 9:
                String name = frame.readString();
                player.getFriends().addFriend(name);
                break;
            case 15:
                name = frame.readString();
                player.getFriends().removeFriend(name);
                break;
            case 76:
                if (player.isMuted()) {
                    if (player.inGame())
                        Static.proto.sendMessage(player, "You are muted. To appeal your mute go to the Argoury forums.");
                    break;
                }
                String recipient = frame.readString();
                int length = frame.readSmart();
                byte[] textBuffer = new byte[frame.getLength() - frame.getPosition()];
                frame.read(textBuffer);
                String message = Text.optimizeText(Text.decompressHuffman(textBuffer, length));
                player.getFriends().sendPM(recipient, message);
                break;
            case 55:
                name = frame.readString();
                frame.readUnsigned();
                player.getFriends().addIgnore(name);
                break;
            case 85:
                name = frame.readString();
                player.getFriends().removeIgnore(name);
                break;
        }
    }
}
