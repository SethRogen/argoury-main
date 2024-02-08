package com.runescape.logic.item;

import com.runescape.Static;
import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public class EquipmentListener implements ItemListener {
    public static final EquipmentListener INSTANCE = new EquipmentListener();

    @Override
    public void event(ItemContainer container, ItemEventType type, int index) {
        switch (type) {
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
        Static.proto.sendItems(player, 94, false, container.array());
        player.calculateBonuses();
        player.updateCurrentInterface();
    }

    private void refresh(Player player, ItemContainer container, int index) {
        refresh(player, container); // TODO For now, as we don't have the single item update packet
    }
}
