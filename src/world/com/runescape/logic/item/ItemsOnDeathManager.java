package com.runescape.logic.item;

import com.runescape.Static;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ButtonHandler;
import com.runescape.logic.player.Player;

import java.util.ArrayList;

/**
 * @author Maxime Meire
 */
public class ItemsOnDeathManager implements ButtonHandler {

    public static enum ReturnType {
        INTERFACE,
        DEATH
    }

    public static enum SafetyType {
        NONE,
        DEFAULT,
        HOUSE,
        CASTLE_WARS,
        TROUBLE_BREWING,
        BARBARIAN_ASSAULT
    }

    private SafetyType safetyType = SafetyType.NONE;
    private boolean hasBob = false;
    private boolean isSkulled = false;
    private boolean itemProtect = false;
    private boolean useGravestone = false;

    private boolean showsInterface = false;

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        system.registerButtonHandler(new int[]{149, 387, 102}, this);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3) {
        switch (interfaceId) {
            case 102:
                switch (b) {
                    case 13:
                        player.getItemsOnDeathManager().showsInterface = false;
                        break;
                }
                break;
            case 149:
                switch (opcode) {
                    case 12:
                        if (player.getItemsOnDeathManager().showsInterface)
                            Static.callScript("interfaces.sendIKODInterface", player);
                        break;
                }
                break;
            case 387:
                switch (b) {
                    case 45:
                        player.getItemsOnDeathManager().showsInterface = true;
                        Static.callScript("interfaces.sendIKODInterface", player);
                        break;
                }
                break;
        }
    }

    public static void handleWalking(Player player) {
        player.getItemsOnDeathManager().showsInterface = false;
    }

    /**
     * This method serves two purposes. One calculating the items kept on death interface item container,
     * and the other calculates both items kept on death and lost item containers.
     * The reason for this is that the items kept on death interface has a different format
     * for showing the items than items lost on death in the inventory.
     * </p>
     * To get the interface items kept on death use ReturnType INTERFACE.</p>
     * To get the items kept on death and dropped on death use ReturnType DEATH.</p>
     *
     * @param player The player to calculate for.
     * @param type   The type of calculation, this determines what the method will be returning.
     * @return Return types: </p>
     *         <b>INTERFACE:</b>
     *         Returns with this format: Object[] { items kept on death on interface }</p>
     *         <b>DEATH:</b>
     *         Returns with this format: Object[] { items kept on death, items dropped on death }</p>
     *         Both return types have elements of the type ItemContainer.
     */
    public static Object[] getIOD(Player player, ReturnType type) {
        if (player != null && player.inGame() && player.isConnected()) {
            ArrayList<PossesedItem> sortedList = new ArrayList<PossesedItem>(40);
            for (PossesedItem i : player.getInventory().array()) {
                if (i != null) {
                    PossesedItem item = new PossesedItem(i.getId(), i.getAmount());
                    pushSorted(player, item, sortedList);
                }
            }
            for (PossesedItem i : player.getEquipment().array()) {
                if (i != null) {
                    PossesedItem item = new PossesedItem(i.getId(), i.getAmount());
                    pushSorted(player, item, sortedList);
                }
            }
            ItemContainer ikodInterface = new ItemContainer(player, 4);
            ItemContainer idod = new ItemContainer(player, 40);
            ItemContainer ikod = new ItemContainer(player, 4);
            int length = player.getItemsOnDeathManager().isSkulled() ? (player.getItemsOnDeathManager().itemProtect() ? 1 : 0) : (player.getItemsOnDeathManager().itemProtect() ? 4 : 3);
            for (int i = 0; i < 4; i++) {
                ikodInterface.array()[i] = new PossesedItem(-1, 0);
            }
            int splitLength = length;
            if (sortedList.size() < 4) {
                splitLength = sortedList.size();
            }
            for (int i = 0; i < splitLength; i++) {
                PossesedItem item = sortedList.get(i);
                if (item != null) {
                    if (item.getDefinition().isStackable() && item.getAmount() > 1) {
                        int remove = item.getAmount() - 1;
                        item.setAmount(1);
                        sortedList.add(i + 1, new PossesedItem(item.getId(), remove));
                        if (splitLength + 1 <= length) {
                            splitLength += 1;
                        } else
                            break;
                    }
                }
            }
            for (int i = 0; i < splitLength; i++) {
                ikodInterface.array()[i] = sortedList.get(i);
                ikod.array()[i] = sortedList.get(i);
            }
            switch (type) {
                case INTERFACE:
                    return new Object[]{ikodInterface};
                case DEATH:
                    for (int i = length; i < sortedList.size(); i++) {
                        idod.add(sortedList.get(i));
                    }
                    idod.add(526);
                    return new Object[]{ikod, idod};
                default:
                    return null;
            }
        }
        return null;
    }

    public static void pushSorted(Player player, PossesedItem i, ArrayList<PossesedItem> sortedList) {
        int pushIndex = 0;
        int index = 0;
        if (sortedList.size() != 0) {
            for (@SuppressWarnings("unused") PossesedItem item : sortedList) {
                int value = ((Integer) i.getDefinition().highAlch).compareTo(sortedList.get(index).getDefinition().highAlch);
                if (value >= 0)
                    break;
                else
                    pushIndex++;
                index++;
            }
            sortedList.add(pushIndex, i);
        } else {
            sortedList.add(pushIndex, i);
        }
    }

    public static int itemAmountKept(Player player) {
        return player.getItemsOnDeathManager().isSkulled() ? (player.getItemsOnDeathManager().itemProtect() ? 1 : 0) : (player.getItemsOnDeathManager().itemProtect() ? 4 : 3);
    }

    /**
     * @param hasBob the hasBob to set
     */
    public void hasBob(boolean hasBob) {
        this.hasBob = hasBob;
    }

    /**
     * @return the hasBob
     */
    public boolean hasBob() {
        return hasBob;
    }

    /**
     * @param isSkulled the isSkulled to set
     */
    public void isSkulled(boolean isSkulled) {
        this.isSkulled = isSkulled;
    }

    /**
     * @return the isSkulled
     */
    public boolean isSkulled() {
        return isSkulled;
    }

    /**
     * @param itemProtect the itemProtect to set
     */
    public void itemProtect(boolean itemProtect) {
        this.itemProtect = itemProtect;
    }

    /**
     * @return the itemProtect
     */
    public boolean itemProtect() {
        return itemProtect;
    }

    /**
     * @param useGravestone the useGravestone to set
     */
    public void useGravestone(boolean useGravestone) {
        this.useGravestone = useGravestone;
    }

    /**
     * @return the useGravestone
     */
    public boolean useGravestone() {
        return useGravestone;
    }

    /**
     * @param safetyType the safetyType to set
     */
    public void setSafetyType(SafetyType safetyType) {
        this.safetyType = safetyType;
    }

    /**
     * @return the safetyType
     */
    public SafetyType getSafetyType() {
        return safetyType;
    }
}
