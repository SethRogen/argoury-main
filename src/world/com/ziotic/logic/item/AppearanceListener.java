package com.ziotic.logic.item;

/**
 * @author Lazaro
 */
public class AppearanceListener implements ItemListener {
    public static final AppearanceListener INSTANCE = new AppearanceListener();

    @Override
    public void event(ItemContainer container, ItemEventType type, int index) {
        if (type == ItemListener.ItemEventType.CHANGE) {
            container.getPlayer().getAppearance().refresh();
        }
    }
}
