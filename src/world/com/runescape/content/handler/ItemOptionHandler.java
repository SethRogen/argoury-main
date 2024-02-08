package com.runescape.content.handler;

import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public interface ItemOptionHandler extends ActionHandler {
    public void handleItemOption1(Player player, PossesedItem item, int index);

    public void handleItemOption2(Player player, PossesedItem item, int index);

    public void handleItemOption3(Player player, PossesedItem item, int index);
}
