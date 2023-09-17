package com.ziotic.adapter.protocol.handler;

import com.ziotic.logic.dialogue.Conversation;
import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.handler.PlayerFrameHandler;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class DialogueHandler extends PlayerFrameHandler {
    private static final Logger logger = Logging.log();

    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        int interfaceSet = frame.readInt();
        int interfaceId = interfaceSet >> 16;
        int buttonId = interfaceSet & 0xffff;
        int buttonId2 = frame.readUnsignedLEShortA();
        if (buttonId2 == 65535) {
            buttonId2 = 0;
        }

        Conversation conversation = player.getCurrentConversation();
        if (conversation != null) {
            switch (interfaceId) {
                case 64:
                case 65:
                case 66:
                case 67:
                case 241:
                case 242:
                case 243:
                case 244:
                    conversation.handle(-1);
                    break;
                case 228:
                case 232:
                case 233:
                case 229:
                case 230:
                case 234:
                    conversation.handle(buttonId - 2);
                    break;
                default:
                    logger.debug("Unhandled interface [id=" + interfaceId + ", button=" + buttonId + ", button2=" + buttonId2 + "]");
                    break;
            }
        }
    }
}
