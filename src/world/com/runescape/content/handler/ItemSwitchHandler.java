package com.runescape.content.handler;

import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public interface ItemSwitchHandler extends ActionHandler {
    public void handleItemSwitch(Player player, int interfaceId1, int childId1, int interfaceId2, int childId2, int id1, int id2, int indexFrom, int indexTo);
}
