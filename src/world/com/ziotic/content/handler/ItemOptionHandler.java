package com.ziotic.content.handler;

import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.player.Player;

/**
 * @author Lazaro
 */
public interface ItemOptionHandler extends ActionHandler {
    public void handleItemOption1(Player player, PossesedItem item, int index);

    public void handleItemOption2(Player player, PossesedItem item, int index);

    public void handleItemOption3(Player player, PossesedItem item, int index);
}
