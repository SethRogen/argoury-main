package com.runescape.content.handler;

import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.object.GameObject;
import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public interface ItemOnObjectHandler extends ActionHandler {
    public void handleItemOnObject(Player player, PossesedItem item, int itemIndex, GameObject obj);
}
