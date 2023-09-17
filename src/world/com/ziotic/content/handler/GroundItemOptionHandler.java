package com.ziotic.content.handler;

import com.ziotic.logic.item.GroundItem;
import com.ziotic.logic.player.Player;

/**
 * @author Lazaro
 */
public interface GroundItemOptionHandler extends ActionHandler {
    void handleGroundItemOption(Player player, GroundItem item);
}
