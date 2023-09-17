package com.ziotic.content.handler;

import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.Player;

/**
 * @author Lazaro
 */
public interface ItemOnObjectHandler extends ActionHandler {
    public void handleItemOnObject(Player player, PossesedItem item, int itemIndex, GameObject obj);
}
