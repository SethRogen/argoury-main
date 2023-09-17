package com.ziotic.logic.item;

import com.ziotic.Constants;
import com.ziotic.Static;
import com.ziotic.content.handler.*;
import com.ziotic.logic.dialogue.Conversation;
import com.ziotic.logic.item.ItemListener.ItemEventType;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.npc.NPCDefinition;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.utility.GameInterfaces;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

/**
 * @author Lazaro
 */
public class BankManager implements ActionHandler, ButtonHandler, ItemSwitchHandler, ObjectOptionHandler, NPCOptionHandler {
    private static final Logger logger = Logging.log();

    public static final int BANK_SIZE = 506;
    public static final int[] BANK_BOOTHS = new int[]{25808, 26972, 34752};
    public static final Integer[] BANKERS = new Integer[]{494, 495};

    public static void updateTabs(Player player) {
        BankContainer bank = player.getBank();
        Static.proto.sendConfig(player, 1246, bank.getTabSize(1) + (bank.getTabSize(2) * 1024) + (bank.getTabSize(3) * 1048576));
        Static.proto.sendConfig(player, 1247, bank.getTabSize(4) + (bank.getTabSize(5) * 1024) + (bank.getTabSize(6) * 1048576));
        Static.proto.sendConfig(player, 1248, bank.getTabSize(7) + (bank.getTabSize(8) * 1024) - 2013265920);
    }

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        system.registerButtonHandler(new int[]{762, 763}, this);
        system.registerItemSwitchHandler(new int[]{762}, this);
        system.registerObjectOptionHandler(BANK_BOOTHS, this);
        system.registerNPCOptionHandler(BANKERS, this);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3) {
        switch (interfaceId) {
            case 762:
                switch (b) {
                    case 93: // Items
                        PossesedItem item = player.getBank().get(b2);
                        if (item == null) {
                            return;
                        }
                        switch (opcode) {
                            case 1: // 1
                                withdrawItem(player, b2, 1);
                                break;
                            case 2: // 5
                                withdrawItem(player, b2, 5);
                                break;
                            case 3: // 10
                                withdrawItem(player, b2, 10);
                                break;
                            case 4: // 0
                                break;
                            case 5: // X
                                player.getAttributes().set("withdrawXIndex", b2);
                                player.getAttributes().set("inputId", Constants.Inputs.BANK_WITHDRAW_X);
                                Static.proto.requestAmountInput(player);
                                break;
                            case 6: // All
                                withdrawItem(player, b2, item.getAmount());
                                break;
                            case 7: // All but one
                                withdrawItem(player, b2, item.getAmount() - 1);
                                break;
                            case 10: // Examine
                                examineItem(player, b3);
                                break;
                            default:
                                logger.debug("Unhandled button [interface=" + interfaceId + ", button=" + b + ", button2=" + b2 + ", button3=" + b3 + ", opcode=" + opcode + "]");
                                break;
                        }
                        break;
                    case 60:
                    case 58:
                    case 56:
                    case 54:
                    case 52:
                    case 50:
                    case 48:
                    case 46:
                    case 44:
                        if (opcode == 2) {
                            collapseTab(player, tabId(b));
                            player.getBank().refresh();
                        }
                        break;
                    case 19:
                        player.getAttributes().set("withdrawingAsNote", !player.getAttributes().is("withdrawingAsNote"));
                        break;
                    case 33: // Deposit inventory
                        depositContainer(player, player.getInventory());
                        break;
                    case 35: // Deposit equipment
                        depositContainer(player, player.getEquipment());
                        break;
                    case 117:
                        player.clearCurrentInterfaces();
                        Static.proto.sendInterface(player, GameInterfaces.EQUIPMENT_SCREEN);
                        break;
                    default:
                        logger.debug("Unhandled button [interface=" + interfaceId + ", button=" + b + ", button2=" + b2 + ", button3=" + b3 + ", opcode=" + opcode + "]");
                        break;
                }
                break;
            case 763:
                PossesedItem item = player.getInventory().get(b2);
                if (item == null) {
                    return;
                }
                switch (opcode) {
                    case 1: // 1
                        depositItem(player, b2, 1);
                        break;
                    case 2: // 5
                        depositItem(player, b2, 5);
                        break;
                    case 3: // 10
                        depositItem(player, b2, 10);
                        break;
                    case 4: // 0
                        break;
                    case 5: // X
                        player.getAttributes().set("depositXIndex", b2);
                        player.getAttributes().set("inputId", Constants.Inputs.BANK_DEPOSIT_X);
                        Static.proto.requestAmountInput(player);
                        break;
                    case 6:
                        depositItem(player, b2, player.getInventory().amount(item.getId()));
                        break;
                    case 10:
                        examineItem(player, b3);
                        break;
                    default:
                        logger.debug("Unhandled button [interface=" + interfaceId + ", button=" + b + ", button2=" + b2 + ", button3=" + b3 + ", opcode=" + opcode + "]");
                        break;
                }
                break;
            default:
                logger.debug("Unhandled button [interface=" + interfaceId + ", button=" + b + ", button2=" + b2 + ", button3=" + b3 + ", opcode=" + opcode + "]");
                break;
        }
    }

    private static void depositContainer(Player player, ItemContainer container) {
        for (int i = 0; i < container.capacity(); i++) {
            PossesedItem item = container.get(i);
            if (item != null) {
                depositItem(player, i, item.getAmount(), container, false);
            }
        }
        player.getBank().refresh();
        container.refresh();
    }

    public static void withdrawItem(Player player, int index, int amount) {
        PossesedItem item = player.getBank().get(index);
        if (item == null) {
            return;
        }

        int id = item.getId();
        if (player.getAttributes().is("withdrawingAsNote")) {
            id = item.getDefinition().certID;
            if (id == -1) {
                id = item.getId();
                Static.proto.sendMessage(player, "This item cannot be withdrawn as a note.");
            }
        }
        ItemDefinition def = ItemDefinition.forId(id);

        if (amount > item.getAmount()) {
            amount = item.getAmount();
        }
        if (!def.isStackable() && amount > player.getInventory().remaining()) {
            amount = player.getInventory().remaining();
            player.getInventory().event(ItemEventType.FULL, -1);
        }

        if (player.getInventory().add(id, amount)) {
            if (amount == item.getAmount()) {
                int tab = player.getBank().unsetTab(item);
                if (player.getBank().getTabSize(tab) == 0) {
                    collapseTab(player, tab);
                }
            }
            player.getBank().remove(item.getId(), amount);
        }
    }

    private static void examineItem(Player player, int id) {
        ItemDefinition def = ItemDefinition.forId(id);
        if (def != null) {
            Static.proto.sendMessage(player, def.examine);
        }
    }

    public static boolean depositItem(Player player, int index, int amount) {
        return depositItem(player, index, amount, player.getInventory(), true);
    }

    private static boolean depositItem(Player player, int index, int amount, ItemContainer container, boolean notifyListeners) {
        PossesedItem item = container.get(index);
        if (item != null) {
            int id = item.getId();
            if (item.getDefinition().isNoted()) {
                id = item.getDefinition().certID;
            }

            int amount2 = container.amount(item.getId());
            if (amount > amount2) {
                amount = amount2;
            }

            if (player.getBank().add(id, amount, -1, notifyListeners)) {
                container.remove(item.getId(), amount, index, notifyListeners);
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleItemSwitch(Player player, int interfaceId1, int childId1, int interfaceId2, int childId2, int id1, int id2, int indexFrom, int indexTo) {
        if (childId2 == 93) {
            PossesedItem item1 = player.getBank().get(indexFrom);
            PossesedItem item2 = player.getBank().get(indexTo);
            if (item1 == null) {
                return;
            }
            player.getBank().set(item1, indexTo);
            player.getBank().set(item2, indexFrom);
            player.getBank().refresh();
        } else {
            moveToTab(player, indexFrom, tabId(childId2));
        }
    }

    private static void moveToTab(Player player, int index, int tab) {
        PossesedItem item = player.getBank().get(index);
        if (item == null) {
            return;
        }

        int oldTab = player.getBank().getTab(item);
        if (oldTab == tab) {
            return;
        }
        if (player.getBank().getTabSize(oldTab) == 1) {
            if (oldTab == 0) {
                Static.proto.sendMessage(player, "You must have at least one item left in this tab before making another.");
            } else {
                player.getBank().setTab(item, tab);
                collapseTab(player, oldTab);
            }
        } else {
            player.getBank().setTab(item, tab);
        }

        player.getBank().refresh();
    }

    private static void collapseTab(Player player, int tab) {
        for (PossesedItem item : player.getBank().array()) {
            if (item != null) {
                int t = player.getBank().getTab(item);
                if (t > tab) {
                    player.getBank().setTab(item, t - 1 > 0 ? t - 1 : 0);
                } else if (t == tab) {
                    player.getBank().setTab(item, 0);
                }
            }
        }
    }

    private static int tabId(int childId) {
        switch (childId) {
            case 60:
                return 1;
            case 58:
                return 2;
            case 56:
                return 3;
            case 54:
                return 4;
            case 52:
                return 5;
            case 50:
                return 6;
            case 48:
                return 7;
            case 46:
                return 8;
        }
        return 0;
    }

    @Override
    public void handleObjectOption1(Player player, GameObject obj) {
        NPC banker = obj.getAttributes().get("banker");
        if (banker == null || !banker.isValid() || banker.isDestroyed()) {
            do {
                Tile loc = obj.getLocation().translate(1, 0, 0);
                if (loc.containsNPCs()) {
                    banker = loc.getNPCs().get(0);
                    break;
                }
                loc = obj.getLocation().translate(0, 1, 0);
                if (loc.containsNPCs()) {
                    banker = loc.getNPCs().get(0);
                    break;
                }
                loc = obj.getLocation().translate(-1, 0, 0);
                if (loc.containsNPCs()) {
                    banker = loc.getNPCs().get(0);
                    break;
                }
                loc = obj.getLocation().translate(0, -1, 0);
                if (loc.containsNPCs()) {
                    banker = loc.getNPCs().get(0);
                    break;
                }
            } while (false);
            if (banker == null) {
                return;
            } else {
                obj.getAttributes().set("banker", banker);
            }
        }

        NPCDefinition def = banker.getDefinition();
        if (def.dialogue != null) {
            new Conversation(def.dialogue, player, banker).init();
        }
    }

    @Override
    public void handleObjectOption2(Player player, GameObject obj) {
        Static.proto.sendInterface(player, GameInterfaces.BANK_SCREEN);
    }

    @Override
    public void handleObjectOption3(Player player, GameObject obj) {
    }

    @Override
    public void handleNPCOption2(Player player, NPC npc) {
        Static.proto.sendInterface(player, GameInterfaces.BANK_SCREEN);
    }

    @Override
    public void handleNPCOption1(Player player, NPC npc) {
        // TODO Auto-generated method stub

    }
}
