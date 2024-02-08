package com.runescape.adapter.protocol.handler;

import com.runescape.Static;
import com.runescape.content.cc.ClanManager;
import com.runescape.logic.mask.Chat;
import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;
import com.runescape.utility.Text;

import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class ChatHandler extends PlayerFrameHandler {
    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        switch (frame.getOpcode()) {
            case 56:
                handleChat(player, frame);
                break;
            case 30:
                handleMessageSetting(player, frame);
                break;
        }

        player.preventFrameSpam(frame.getOpcode());
    }

    private void handleMessageSetting(Player player, Frame frame) {
        int type = frame.readUnsigned();

        player.getAttributes().set("msgType", type);
    }

    private void handleChat(Player player, Frame frame) {
        int color = frame.readUnsigned();
        int effect = frame.readUnsigned();
        int length = frame.readSmart();
        byte[] textBuffer = new byte[frame.getLength() - frame.getPosition()];
        frame.read(textBuffer);
        if (player.isMuted()) {
            Static.proto.sendMessage(player, "You are muted. To appeal your mute go to the Argoury forums.");
            return;
        }
        String text = Text.decompressHuffman(textBuffer, length);
        int msgType = player.getAttributes().getInt("msgType");
        if (msgType == 1 && player.getClan() != null) {
            ClanManager.sendMessage(player, Text.optimizeText(text));
        } else {
            Chat chat = new Chat(Text.optimizeText(text), color, effect);
            Player[] playerArray = Static.world.getLocalPlayers(player.getLocation());
            for (int i = 0; i < playerArray.length; i++) {
                if (playerArray[i] == null)
                    continue;
                Static.proto.sendPublicChat(playerArray[i], player, chat);
            }
        }
    }
}
