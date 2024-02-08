package com.runescape.logic.item;

import com.runescape.Static;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ButtonHandler;
import com.runescape.logic.player.Player;

public class InventoryHandler implements ButtonHandler {

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        system.registerButtonHandler(new int[]{149}, this);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleButton(final Player player, int opcode, int interfaceId, int b, final int b2, final int b3) {
        switch (opcode) {
            case 8: // drop
                PossesedItem item = player.getInventory().get(b2);
                if (item != null) {
                    int id = item.getId();
                    int amt = item.getAmount();
                    if (id == b3) {
                        if (player.getInventory().remove(id, amt, b2)) {
                            Static.world.getGroundItemManager().add(id, amt, player.getLocation(), player.getProtocolName(), false);
                        }
                    }
                }
                break;
            case 2: // wield
                final PossesedItem w = player.getInventory().get(b2);
                player.addSpecificProcess(new Runnable() {
                    public void run() {
                        Static.callScript("equipment.wieldEquipment", player, b3, b2);
                        player.getCombat().wieldEquipment(w);
                    }
                });
                break;
            case 10: // examine
                item = player.getInventory().get(b2);
                if (item != null) {
                    Static.proto.sendMessage(player, item.getDefinition().examine);
                }
                break;
            default:
                Static.callScript("buttons.unhandledButton", player, opcode, interfaceId, b, b2, b3);
                break;
        }
    }
}
