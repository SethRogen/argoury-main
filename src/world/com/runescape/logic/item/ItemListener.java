package com.runescape.logic.item;

/**
 * @author Lazaro
 */
public interface ItemListener {
    public static enum ItemEventType {
        CHANGE, FULL
    }

    public void event(ItemContainer container, ItemEventType type, int index);
}
