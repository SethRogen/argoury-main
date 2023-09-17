package com.ziotic.content.shop;

import com.ziotic.Static;
import com.ziotic.engine.event.RecurringEvent;
import com.ziotic.logic.item.ItemListener;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.player.DisplayMode;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.utility.GameInterface;
import com.ziotic.logic.utility.NodeRunnable;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Lazaro
 */
public class Shop extends RecurringEvent {
    private static final GameInterface SHOP_INTERFACE = new GameInterface(620, null, new NodeRunnable<Player>() {
        public void run(Player player) {
            if (player.getDisplayMode() == DisplayMode.FIXED) {
                Static.proto.sendInterface(player, 621, 548, 197, false); // inventory interface
                Static.proto.sendInterface(player, 449, 548, 198, false); // item info interface
            } else {
                Static.proto.sendInterface(player, 621, 746, 84, false); // inventory interface
                Static.proto.sendInterface(player, 449, 746, 85, false); // item info interface
            }
            Static.proto.sendAccessMask(player, -1, -1, 449, 21, 0, 0); // item info masks
            Static.proto.sendInterfaceScript(player, 149, 40697856, 93, 4, 7, 1, -1, "Value", "Sell 1", "Sell 5", "Sell 10", "Sell 50"); // inventory options
            Static.proto.sendAccessMask(player, 0, 27, 621, 0, 36, 1086); // inventory masks
            Static.proto.sendAccessMask(player, 0, 12, 620, 26, 0, 1150); // shop masks
            Static.proto.sendAccessMask(player, 0, player.getDisplayMode() == DisplayMode.FIXED ? 126 : 240, 620, 25, 0, 1150); // shop masks
            Static.proto.sendInterfaceVariable(player, 1241, 16750848);
            Static.proto.sendInterfaceVariable(player, 1242, 15439903);
            Static.proto.sendInterfaceVariable(player, 741, -1);
            Static.proto.sendInterfaceVariable(player, 743, -1);
        }
    }, new NodeRunnable<Player>() {
        public void run(Player player) {
            player.getCurrentShop().close(player);
        }
    }
    );
    private List<Integer> acceptedItems;
    private int id;
    private String name;
    private int[][] originalStock;
    private List<Player> players; // The players currently with this shop open
    private PossesedItem[] sampleStock;
    private PossesedItem[] stock;

    public Shop(int id, PossesedItem[] sampleStock, PossesedItem[] stock, int size, List<Integer> acceptedItems, String name) {
        super(30000, ExecutorType.PARALLEL_LOGIC);
        this.id = id;
        this.name = name;
        if (size > stock.length) {
            this.stock = new PossesedItem[size];
            for (int i = 0; i < stock.length; i++) {
                this.stock[i] = stock[i];
            }
        } else {
            this.stock = stock;
        }
        stock = this.stock;
        this.sampleStock = sampleStock;
        this.acceptedItems = acceptedItems;
        this.players = new LinkedList<Player>();
        originalStock = new int[stock.length][2];
        for (int i = 0; i < stock.length; i++) {
            PossesedItem item = stock[i];
            if (item != null) {
                originalStock[i][0] = item.getId();
                originalStock[i][1] = item.getAmount();
            } else {
                originalStock[i][0] = -1;
            }
        }
    }

    public void buy(Player player, int index, int amount, boolean sampleStock) {
        PossesedItem item = sampleStock ? this.sampleStock[index] : stock[index];
        if (item == null) {
            return;
        }
        if (item.getAmount() <= 0) {
            Static.proto.sendMessage(player, "There is no stock of that item at the moment.");
            return;
        } else if (player.getInventory().remaining() <= 0) {
            player.getInventory().event(ItemListener.ItemEventType.FULL, -1);
            return;
        } else if (item.getAmount() < amount) {
            Static.proto.sendMessage(player, "The shop has ran out of stock.");
            amount = item.getAmount();
        }
        long cost = ((long) item.getDefinition().shopPrice) * amount;
        if (cost > Integer.MAX_VALUE) { // Prevent a bit-overflow which causes
            // HUGE dupes (plus its impossible to
            // have more money than this)
            Static.proto.sendMessage(player, "You don't have enough coins.");
            return;
        }
        if ((!sampleStock && player.getInventory().contains(995, item.getDefinition().shopPrice * amount)) || sampleStock) {
            if (player.getInventory().add(new PossesedItem(item.getId(), amount))) {
                if (!sampleStock) { // TODO Make it only remove for the current
                    // player
                    player.getInventory().remove(995, item.getDefinition().shopPrice * amount);
                    item.setAmount(item.getAmount() - amount);
                    if (item.getAmount() == 0 && this.originalStock[index][0] == -1) {
                        this.stock[index] = null;
                    }
                    refresh();
                }
                player.getInventory().refresh();
            }
        } else {
            Static.proto.sendMessage(player, "You don't have enough coins.");
        }
    }

    public void close(Player player) {
        player.setCurrentShop(null);
        players.remove(player);
    }

    public void examine(Player player, int index, boolean sampleStock) {
        PossesedItem item = sampleStock ? this.sampleStock[index] : stock[index];
        if (item == null) {
            return;
        }
        Static.proto.sendMessage(player, item.getDefinition().examine);
    }

    private String flipString(String string) {
        StringBuilder sb = new StringBuilder();
        char[] stringChars = string.toCharArray();
        for (int i = stringChars.length - 1; i >= 0; i--) {
            sb.append(stringChars[i]);
        }
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public void info(Player player, int index, boolean sampleStock) {
        PossesedItem item = sampleStock ? this.sampleStock[index] : stock[index];
        if (item == null) {
            return;
        }
        if (!sampleStock) {
            Static.proto.sendMessage(player, item.getDefinition().name + ": currently costs " + parseInteger(item.getDefinition().shopPrice) + " coins.");
        } else {
            Static.proto.sendMessage(player, item.getDefinition().name + ": this is a free sample!");
        }
    }

    public void open(Player player) {
        Static.proto.sendConfig(player, 118, 3); // item container id 3
        Static.proto.sendConfig(player, 1496, 553); // item container id 553
        Static.proto.sendConfig(player, 532, 995); // ??
        Static.proto.sendItems(player, 3, false, stock); // main stock
        Static.proto.sendItems(player, 553, false, sampleStock); // free sample
        // items
        Static.proto.sendInterface(player, SHOP_INTERFACE);
        Static.proto.sendInterfaceVariable(player, 199, -1);
        for (int i = 946; i < 986; i++) {
            Static.proto.sendInterfaceVariable(player, i, 0); // item price
            // configs
        }
        Static.proto.sendInterfaceVariable(player, 1241, 16750848);
        Static.proto.sendInterfaceVariable(player, 1242, 15439903);
        Static.proto.sendInterfaceVariable(player, 741, -1);
        Static.proto.sendInterfaceVariable(player, 743, -1);
        Static.proto.sendString(player, 620, 20, name);
        players.add(player);
        player.setCurrentShop(this);
    }

    private String parseInteger(int i) {
        StringBuilder sb = new StringBuilder();
        int sinceComma = 0;
        char[] intChars = ((Integer) i).toString().toCharArray();
        for (int x = intChars.length - 1; x >= 0; x--) {
            sb.append(intChars[x]);
            sinceComma++;
            if (sinceComma == 3 && x != 0) {
                sb.append(",");
                sinceComma = 0;
            }
        }
        return flipString(sb.toString());
    }

    @Override
    public void run() {
        boolean updated = false;
        for (int i = 0; i < stock.length; i++) {
            PossesedItem item = stock[i];
            if (item != null) {
                if (item.getAmount() < originalStock[i][1]) {
                    item.setAmount(item.getAmount() + 1);
                    updated = true;
                } else if (item.getAmount() > originalStock[i][1]) {
                    item.setAmount(item.getAmount() - 1);
                    if (originalStock[i][0] == -1 && (item.getAmount() == 0 || item.getDefinition().isStackable())) {
                        stock[i] = null;
                    }
                    updated = true;
                }
            }
        }
        if (updated) {
            refresh();
        }
    }

    public void sell(Player player, int index, int amount) {
        PossesedItem item = player.getInventory().get(index);
        if (item == null) {
            return;
        }
        if (/*!item.getDefinition().isTradeable()*/false) {
            Static.proto.sendMessage(player, "You cannot sell this item."); // TODO
            // Find
            // out
            // what
            // RS
            // says
            return;
        }
        if (acceptedItems != null && !acceptedItems.contains(item.getId())) {
            return; // RuneScape probably says something here
        }
        for (PossesedItem shopItem : stock) {
            if (shopItem != null && shopItem.getId() == item.getId()) {
                int invAmount = player.getInventory().amount(item.getId());
                if (invAmount < amount) {
                    amount = invAmount;
                }
                player.getInventory().remove(item.getId(), amount);
                long value = item.getDefinition().shopPrice;
                if (value < 0) {
                    value = item.getDefinition().shopPrice;
                }
                value *= amount;
                if (value > Integer.MAX_VALUE) {
                    Static.proto.sendMessage(player, "The shop does not have enough money for this amount of items.");
                    player.getInventory().add(item.getId(), amount);
                    shopItem.setAmount(shopItem.getAmount() - amount);
                    player.getInventory().refresh();
                    return;
                }
                player.getInventory().add(995, (int) value);
                player.getInventory().refresh();
                refresh();
                return;
            }
        }
        for (int i = 0; i < stock.length; i++) {
            if (stock[i] == null) {
                int invAmount = player.getInventory().amount(item.getId());
                if (invAmount < amount) {
                    amount = invAmount;
                }
                player.getInventory().remove(item.getId(), amount);
                stock[i] = new PossesedItem(item.getId(), amount);
                long value = item.getDefinition().shopPrice;
                if (value < 0) {
                    value = item.getDefinition().shopPrice;
                }
                value *= amount;
                if (value > Integer.MAX_VALUE) {
                    Static.proto.sendMessage(player, "The shop does not have enough money for this amount of items.");
                    player.getInventory().add(item.getId(), amount);
                    stock[i] = null;
                    player.getInventory().refresh();
                    return;
                }
                player.getInventory().add(995, (int) value);
                player.getInventory().refresh();
                refresh();
                return;
            }
        }
        // Not enough space in the shop
        Static.proto.sendMessage(player, "There is not enough space in the shop for this item."); // TODO
        // Find
        // correct
        // RS
        // message
    }

    public void refresh() {
        for (Player player : players) {
            Static.proto.sendItems(player, 3, false, stock); // main stock
        }
    }
}
