package com.runescape.content.handler;

import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public interface ButtonHandler extends ActionHandler {
    public void handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3);
}
