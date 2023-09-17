package com.ziotic.adapter.protocol.handler;

import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.handler.PlayerFrameHandler;
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
