package com.runescape.content.handler;

import com.runescape.logic.item.Item;
import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public interface ItemOnItemHandler extends ActionHandler {
    public void handleItemOnItem(Player player, Item item1, int index1, Item item2, int index2);
}
