package com.ziotic.logic.item;

import com.ziotic.logic.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lazaro
 */
public class ItemContainer {

    public static enum StackType {
        NORMAL, FORCE_STACK
    }

    private Player player;

    private PossesedItem[] items;
    private int capacity;
    private StackType stackType;

    private boolean listening = false;
    private List<ItemListener> listeners = new ArrayList<ItemListener>();

    public ItemContainer(Player player, int capacity) {
        this(player, capacity, StackType.NORMAL);
    }

    public ItemContainer(Player player, int capacity, StackType stackType) {
        this.player = player;
        this.capacity = capacity;
        this.stackType = stackType;
        items = new PossesedItem[capacity];
    }

    public void addListener(ItemListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(ItemListener listener) {
        listeners.remove(listener);
    }

    public Player getPlayer() {
        return player;
    }

    public PossesedItem[] array() {
        return items;
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public void event(ItemListener.ItemEventType type, int index) {
        if (listening) {
            for (ItemListener listener : listeners) {
                listener.event(this, type, index);
            }
        }
    }

    public void refresh() {
        event(ItemListener.ItemEventType.CHANGE, -1);
    }

    public void clear() {
        clear(true);
    }

    public void clear(boolean notifyListeners) {
        items = new PossesedItem[capacity];
        if (notifyListeners) {
            event(ItemListener.ItemEventType.CHANGE, -1);
        }
    }

    public boolean add(PossesedItem item) {
        return add(item.getId(), item.getAmount());
    }

    public boolean add(PossesedItem item, int preferredIndex) {
        return add(item.getId(), item.getAmount(), preferredIndex);
    }

    public boolean add(PossesedItem item, int preferredIndex, boolean notifyListeners) {
        return add(item.getId(), item.getAmount(), preferredIndex, notifyListeners);
    }

    public boolean add(int id) {
        return add(id, 1);
    }

    public boolean add(int id, int amount) {
        return add(id, amount, -1);
    }

    public boolean add(int id, int amount, int preferredIndex) {
        return add(id, amount, preferredIndex, true);
    }

    public boolean add(int id, int amount, int preferredIndex, boolean notifyListeners) {
        return add(id, amount, preferredIndex, notifyListeners, false);
    }

    public boolean add(int id, int amount, int preferredIndex, boolean notifyListeners, boolean enforceSlot) {
        if (!hasRoomFor(id, amount)) {
            if (notifyListeners) {
                event(ItemListener.ItemEventType.FULL, -1);
            }
            return false;
        }
        ItemDefinition def = ItemDefinition.forId(id);
        int amountToAdd = (def.isStackable() || stackType == StackType.FORCE_STACK) ? 1 : amount;
        for (int added = 0; added < amountToAdd; added++) {
            int curIndex = preferredIndex;
            PossesedItem item = preferredIndex != -1 ? items[preferredIndex] : null;
            if (preferredIndex == -1 || !(item == null || (item.getId() == id && (def.isStackable() || stackType == StackType.FORCE_STACK)))) {
                if (enforceSlot) {
                    return false;
                }
                curIndex = -1;
                for (int index = 0; index < items.length; index++) {
                    item = items[index];
                    if (item != null) {
                        if (item.getId() == id && (def.isStackable() || stackType == StackType.FORCE_STACK)) {
                            curIndex = index;
                            break;
                        }
                    }
                }
                if (curIndex == -1) {
                    for (int index = 0; index < items.length; index++) {
                        item = items[index];
                        if (item == null) {
                            curIndex = index;
                            break;
                        }
                    }
                }
            }
            if (curIndex == -1) {
                return false;
            }
            PossesedItem item2 = items[curIndex];
            if (item2 != null) {
                item2.setAmount(item2.getAmount() + amount);
            } else {
                item2 = items[curIndex] = new PossesedItem(id, (def.isStackable() || stackType == StackType.FORCE_STACK) ? amount : 1);
            }
            if (notifyListeners && amountToAdd == 1) {
                event(ItemListener.ItemEventType.CHANGE, curIndex);
            }
            preferredIndex = -1;
        }
        if (notifyListeners && amountToAdd > 1) {
            event(ItemListener.ItemEventType.CHANGE, -1);
        }
        return true;
    }

    public boolean contains(int id) {
        return contains(id, 1);
    }

    public boolean contains(int id, int amount) {
        int amountFound = 0;
        for (PossesedItem item : items) {
            if (item != null && item.getId() == id) {
                amountFound += item.getAmount();
                if (amountFound >= amount) {
                    return true;
                }
            }
        }
        return false;
    }

    public int amount(int id) {
        int amount = 0;
        for (PossesedItem item : items) {
            if (item != null && item.getId() == id) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    public int remaining() {
        return capacity - size();
    }

    public boolean hasRoomFor(int id, int amount) {
        ItemDefinition def = ItemDefinition.forId(id);
        if (def.isStackable() || stackType == StackType.FORCE_STACK) {
            return remaining() >= 1 || contains(id);
        } else {
            return remaining() >= amount;
        }
    }

    public boolean remove(PossesedItem item) {
        return remove(item.getId(), item.getAmount());
    }

    public boolean remove(PossesedItem item, int preferredIndex) {
        return remove(item.getId(), item.getAmount(), preferredIndex);
    }

    public boolean remove(PossesedItem item, int preferredIndex, boolean notifyListeners) {
        return remove(item.getId(), item.getAmount(), preferredIndex, notifyListeners);
    }

    public boolean remove(int id) {
        return remove(id, 1);
    }

    public boolean remove(int id, int amount) {
        return remove(id, amount, -1);
    }

    public boolean remove(int id, int amount, int preferredIndex) {
        return remove(id, amount, preferredIndex, true);
    }

    public boolean remove(int id, int amount, int preferredIndex, boolean notifyListeners) {
        ItemDefinition def = ItemDefinition.forId(id);
        if (def.isStackable() || stackType == StackType.FORCE_STACK) {
            int curIndex = preferredIndex;
            PossesedItem item = preferredIndex != -1 ? items[preferredIndex] : null;
            if (item == null || item.getId() != id || item.getAmount() < amount) {
                curIndex = -1;
                for (int index = 0; index < capacity; index++) {
                    item = items[index];
                    if (item != null && item.getId() == id && item.getAmount() >= amount) {
                        curIndex = index;
                        break;
                    }
                }
            }
            if (curIndex == -1) {
                return false;
            }
            item.setAmount(item.getAmount() - amount);
            if (item.getAmount() <= 0) {
                items[curIndex] = null;
            }
            if (notifyListeners) {
                event(ItemListener.ItemEventType.CHANGE, curIndex);
            }
            return true;
        } else {
            if (contains(id, amount)) {
                int removed = 0;
                PossesedItem item = preferredIndex != -1 ? items[preferredIndex] : null;
                if (item != null && item.getId() == id) {
                    items[preferredIndex] = null;
                    if (notifyListeners && amount == 1) {
                        event(ItemListener.ItemEventType.CHANGE, preferredIndex);
                    }
                    removed++;
                    if (removed == amount) {
                        if (notifyListeners && amount > 1) {
                            event(ItemListener.ItemEventType.CHANGE, -1);
                        }
                        return true;
                    }
                }
                for (int index = 0; index < capacity; index++) {
                    item = items[index];
                    if (item != null && item.getId() == id) {
                        items[index] = null;
                        if (notifyListeners && amount == 1) {
                            event(ItemListener.ItemEventType.CHANGE, index);
                        }
                        removed++;
                        if (removed == amount) {
                            if (notifyListeners && amount > 1) {
                                event(ItemListener.ItemEventType.CHANGE, -1);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public PossesedItem get(int index) {
        if (index < 0 || index > items.length) {
            return null;
        }
        return items[index];
    }

    public PossesedItem set(int id, int amount, int index) {
        PossesedItem item = id == -1 ? null : new PossesedItem(id, amount);
        items[index] = item;
        return item;
    }

    public void set(PossesedItem item, int index) {
        items[index] = item;
    }

    public int size() {
        int size = 0;
        for (PossesedItem item : items) {
            if (item != null) {
                size++;
            }
        }
        return size;
    }
    
    public void reorder() {
    	PossesedItem[] newItems = new PossesedItem[capacity];
    	int index = 0;
    	for (PossesedItem item : items)
    		if (item != null)
    			newItems[index++] = item;
    	this.items = newItems;
    }

    public int capacity() {
        return capacity;
    }
}
