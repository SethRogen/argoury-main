package com.runescape.logic.item;

import java.util.HashMap;
import java.util.Map;

import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public class BankContainer extends ItemContainer {
    private Map<PossesedItem, Integer> tabMap = new HashMap<PossesedItem, Integer>();
    private short[] tabSizes = new short[9];

    public BankContainer(Player player) {
        super(player, BankManager.BANK_SIZE, ItemContainer.StackType.FORCE_STACK);
        addListener(BankListener.INSTANCE);
    }

    public int getTab(PossesedItem item) {
        Integer tab = tabMap.get(item);
        if (tab != null) {
            return tab;
        }
        return 0;
    }

    public void setTab(PossesedItem item, int tab) {
        if (tab != 0) {
            Integer oldTab = tabMap.put(item, tab);
            if (oldTab != null) {
                tabSizes[oldTab]--;
            } else {
                tabSizes[0]++;
            }
            tabSizes[tab]++;
        } else {
            unsetTab(item);
        }
    }

    public int unsetTab(PossesedItem item) {
        Integer tab = tabMap.remove(item);
        if (tab != null) {
            tabSizes[tab]--;
            tabSizes[0]--;
            return tab;
        }
        return 0;
    }

    public int getTabSize(int tab) {
        if (tab == 0) {
            return size() - tabSizes[0];
        }
        return tabSizes[tab];
    }

    public Map<PossesedItem, Integer> getTabMap() {
        return tabMap;
    }
}
