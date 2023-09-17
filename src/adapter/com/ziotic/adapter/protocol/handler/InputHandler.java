package com.ziotic.adapter.protocol.handler;

import com.ziotic.Static;
import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.handler.PlayerFrameHandler;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class InputHandler extends PlayerFrameHandler {
    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        switch (frame.getOpcode()) {
            case 54:
                handleTextInput(player, frame);
                break;
            case 51:
                handleAmountInput(player, frame);
                break;
        }
    }

    private void handleAmountInput(Player player, Frame frame) {
        if (!player.getAttributes().isSet("inputId")) {
            return;
        }

        int input = frame.readInt();
        int inputId = player.getAttributes().getInt("inputId");

        Static.callScript("inputs.handleInput", player, inputId, input);

        player.getAttributes().unSet("inputId");
    }

    private void handleTextInput(Player player, Frame frame) {
        if (!player.getAttributes().isSet("inputId")) {
            return;
        }

        String input = frame.readString();
        int inputId = player.getAttributes().getInt("inputId");

        Static.callScript("inputs.handleInput", player, inputId, input);

        player.getAttributes().unSet("inputId");
    }
}
