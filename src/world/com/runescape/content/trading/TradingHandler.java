/**
 *
 */
package com.runescape.content.trading;

import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ButtonHandler;
import com.runescape.logic.player.Player;

/**
 * @author Maxime
 */
public class TradingHandler implements ButtonHandler {

    /**
     * @see com.runescape.content.handler.ActionHandler#load(com.runescape.content.handler.ActionHandlerSystem)
     */
    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        // TODO Auto-generated method stub
        system.registerButtonHandler(new int[]{334, 335, 336}, this);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    /**
     * @see com.runescape.content.handler.ButtonHandler#handleButton(com.runescape.logic.player.Player, int, int, int, int, int)
     */
    @Override
    public void handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3) {
        switch (interfaceId) {
            case 334:
                TradingManager.handleTradeScreenOptions(player, opcode, interfaceId, b, b2, b3);
                break;
            case 335:
                TradingManager.handleTradeScreenOptions(player, opcode, interfaceId, b, b2, b3);
                break;
            case 336:
                TradingManager.handleInventoryOptions(player, opcode, b2);
                break;
        }
    }
}
