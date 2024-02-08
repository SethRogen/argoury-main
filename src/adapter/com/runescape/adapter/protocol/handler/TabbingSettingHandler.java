package com.runescape.adapter.protocol.handler;

import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;

import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class TabbingSettingHandler extends PlayerFrameHandler {
    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        int publicChatSetting = frame.readUnsigned();
        int privateChatSetting = frame.readUnsigned();
        int tradeSetting = frame.readUnsigned();

        player.getFriends().setPrivateChatSetting(privateChatSetting);
    }
}
