package com.runescape.logic.item;

import com.runescape.Static;
import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public class InventoryListener implements ItemListener {
    public static final InventoryListener INSTANCE = new InventoryListener();

    @Override
    public void event(ItemContainer container, ItemEventType type, int index) {
        switch (type) {
            case FULL:
                Static.proto.sendMessage(container.getPlayer(), "There is not enough space in your inventory.");
                break;
            case CHANGE:
                if (index == -1) {
                    // A complete refresh is required
                    refresh(container.getPlayer(), container);
                } else {
                    // Only update a single slot to save bandwidth
                    refresh(container.getPlayer(), container, index);
                }
                break;
        }
    }

    private void refresh(Player player, ItemContainer container) {
        Static.proto.sendItems(player, 93, false, container.array());
    }

    private void refresh(Player player, ItemContainer container, int index) {
        refresh(player, container); // TODO For now, as we don't have the single item update packet
    }
}
