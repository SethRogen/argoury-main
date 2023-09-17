package com.ziotic.logic.item;

import com.ziotic.Static;
import com.ziotic.engine.event.Event;
import com.ziotic.engine.event.RecurringEvent;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Lazaro
 */
public class GroundItemManager {
    private List<GroundItem> groundItems = new ArrayList<GroundItem>();

    public void add(int id, int amount, Tile loc, String owner, boolean spawned) {
        for (GroundItem item : loc.getItems()) {
            if ((item.isPublic() || (owner != null && owner.equals(item.getOwner()))) && !item.isSpawned() && id == item.getId() && item.getDefinition().isStackable()) {
                item.setAmount(item.getAmount() + amount);
                item.resetTimeModified();
                item.setModified(true);
                refresh(item);
                item.setModified(false);
                return;
            }
        }
        final GroundItem item = new GroundItem(id, amount, loc, owner, spawned);
        groundItems.add(item);
        refresh(item);
        Static.engine.scheduleRecurringEvent(new RecurringEvent(60000, Event.ExecutorType.PARALLEL_LOGIC) {
            public void run() {
                int timeSinceModified = (int) (System.currentTimeMillis() - item.getTimeModified());
                if (timeSinceModified >= 60000) {
                    if (item.exists()) {
                        if (!item.isPublic()) {
                            item.setPublic(true);
                            item.resetTimeModified();
                            refresh(item, true);
                            super.changeDelay(60000);
                            return;
                        } else {
                            if (item.isSpawned()) {
                                item.setExists(false);
                                refresh(item);
                                super.changeDelay(60000);
                            } else {
                                remove(item);
                                super.stop();
                            }
                        }
                    } else if (item.isSpawned()) {
                        item.setExists(true);
                        refresh(item);
                        super.changeDelay(60000);
                    }
                } else {
                    super.changeDelay(60000 - timeSinceModified);
                }
            }
        });
    }

    public GroundItem get(int id, Tile loc) {
        for (GroundItem item : loc.getItems()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    private void refresh(GroundItem item) {
        refresh(item, false);
    }

    private void refresh(GroundItem item, boolean excludeOwner) {
        for (Player player : Static.world.getLocalPlayers(item.getLocation(), 48)) {//31
            if (excludeOwner && player.getProtocolName().equals(item.getOwner())) {
                continue;
            }
            if (item.exists()) {
                if (player.getLocalGroundItems().contains(item)) {
                    if (!item.isModified()) {
                        continue;
                    }
                } else if (item.isPublic() || player.getProtocolName().equals(item.getOwner())) {
                    player.getLocalGroundItems().add(item);
                }
            }
            refresh(player, item);
        }
    }

    public void refresh(Player player) {
        for (Iterator<GroundItem> it = player.getLocalGroundItems().iterator(); it.hasNext(); ) {
            GroundItem item = it.next();
            if (!item.exists() || player.getLocation().differentMap(item.getLocation())) {
                //Static.proto.sendDestroyGroundItem(player, item);
                it.remove();
            }
        }
        for (GroundItem item : Static.world.getLocalItems(player.getLocation())) {
            if (item != null) {
                if (item.exists()) {
                    if (player.getLocalGroundItems().contains(item)) {
                        continue;
                    } else if (item.isPublic() || player.getProtocolName().equals(item.getOwner())) {
                        player.getLocalGroundItems().add(item);
                    }
                    refresh(player, item);
                }
            }
        }
    }

    private void refresh(Player player, GroundItem item) {
        if (!item.exists()) {
            Static.proto.sendDestroyGroundItem(player, item);
        } else if (item.isPublic() || player.getProtocolName().equals(item.getOwner())) {
            if (item.isModified()) {
                Static.proto.sendDestroyGroundItem(player, item);
            }
            Static.proto.sendCreateGroundItem(player, item);
        }
    }

    public boolean remove(GroundItem item) {
        if (ItemDefinition.forId(item.getId()).isStackable()) {
            item.setExists(false);
            refresh(item);
            item.setLocation(null);
            groundItems.remove(item);
        } else {
            item.setAmount(item.getAmount() - 1);
            if (item.getAmount() >= 1) {
                item.setModified(true);
                refresh(item);
                item.setModified(false);
            } else {
                item.setExists(false);
                refresh(item);
                item.setLocation(null);
                groundItems.remove(item);
            }
        }
        return true;
    }
}