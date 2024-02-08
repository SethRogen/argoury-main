package com.runescape.adapter.protocol.handler;

import com.runescape.Static;
import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;

import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class ButtonHandler extends PlayerFrameHandler {
    private int opcode;

    public ButtonHandler(int opcode) {
        this.opcode = opcode;
    }

    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        int interfaceSet = frame.readInt();
        int interfaceId = interfaceSet >> 16;
        int b = interfaceSet & 0xff;
        int b2 = frame.readUnsignedShortA();
        int b3 = frame.readUnsignedShortA();
        if (b2 == 65535) {
            b2 = 0;
        }
        if (b3 == 65535) {
            b3 = 0;
        }
        if (!Static.ahs.handleButton(player, opcode, interfaceId, b, b2, b3)) {
            Static.callScript("buttons.handleButton", player, opcode, interfaceId, b, b2, b3);
        }
        // player.preventFrameSpam(frame.getOpcode());
    }
}
