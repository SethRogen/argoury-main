/**
 *
 */
package com.runescape.content.trading;

import com.runescape.Constants;
import com.runescape.Static;
import com.runescape.logic.item.ItemContainer;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.player.Player;

import java.util.LinkedList;

/**
 * @author Maxime
 */
public class TradingManager {

    public static enum TradeStage {
        NONE,
        INITIATED_335,
        ACCEPTED_335,
        INITIATED_334,
        ACCEPTED_334
    }

    private Player otherPlayer = null;
    private LinkedList<Player> playerRequests = new LinkedList<Player>();

    private ItemContainer inventoryItems = null;
    private ItemContainer tradeScreenItems = null;
    private ItemContainer lentItem = null;

    private TradeStage stage = TradeStage.NONE;

    public TradingManager() {
    }

    public Player getOtherPlayer() {
        return otherPlayer;
    }

    public void setOtherPlayer(Player otherPlayer) {
        this.otherPlayer = otherPlayer;
    }

    public static void setOtherPlayerBoth(Player player, Player player2) {
        player.getTradingManager().setOtherPlayer(player2);
        player2.getTradingManager().setOtherPlayer(player);
    }

    /**
     * This method is called by the other player.
     */
    public static void requestTrade(Player player, Player player2) {
        if (!player.getTradingManager().playerRequests.contains(player2) && player2.isConnected() && player2.inGame()) {
            addRequest(player2);
        }
        if (bilateralRequest(player, player2)) {
            setTradeStagesInitiation335(player2, player);
            sendInterfaces(player2.getTradingManager().otherPlayer, player2);
            refreshBoth(player2, player2.getTradingManager().otherPlayer);
            Static.proto.sendMessage(player, "The other player accepted your trade offer.");
        } else
            Static.callScript("trading.sendRequest", player, player2);
    }

    public static void addRequest(Player player) {
        player.getTradingManager().playerRequests.add(player);
    }

    public static void removeRequest(Player player) {
        player.getTradingManager().playerRequests.remove(player);
    }

    public static boolean containsRequest(Player player) {
        return player.getTradingManager().playerRequests.contains(player);
    }

    public static void removeRequests(Player player, Player player2) {
        removeRequest(player2);
        removeRequest(player);
    }

    public static boolean bilateralRequest(Player player, Player player2) {
        if (containsRequest(player2) && containsRequest(player) && player2.isConnected() && player2.inGame() && player.isConnected() && player.inGame() && player.getTradingManager().stage == TradeStage.NONE && player2.getTradingManager().stage == TradeStage.NONE) {
            removeRequests(player, player2);
            setOtherPlayerBoth(player, player2);
            clearItemContainersBoth(player, player2);
            return true;
        }
        return false;
    }

    public static void setTradeStagesInitiation335(Player player, Player player2) {
        updateTradeStage(player, player2, TradeStage.INITIATED_335);
        updateTradeStage(player2, player, TradeStage.INITIATED_335);
    }

    public static TradeStage getTradeStage(Player player) {
        return player.getTradingManager().stage;
    }

    public static void setTradeStage(Player player, TradeStage stage) {
        player.getTradingManager().stage = stage;
    }

    public static boolean isTrading(Player player) {
        return player.getTradingManager().otherPlayer == null ? false : true;
    }

    public static void handleWalking(Player player, Player player2) {
        if (isTrading(player))
            if (player2 != null && player2 == player.getTradingManager().otherPlayer)
                if (isTrading(player2))
                    sendDecline(player, player.getTradingManager().otherPlayer, "You declined the trade.", "Other player declined the trade.");
    }

    public static void clearItemContainersBoth(Player player, Player player2) {
        clearItemContainers(player);
        clearItemContainers(player2);
    }

    public static void clearItemContainers(Player player) {
        player.getTradingManager().inventoryItems = new ItemContainer(player, 28);
        player.getTradingManager().tradeScreenItems = new ItemContainer(player, 28);
        player.getTradingManager().lentItem = new ItemContainer(player, 1);
        int i = 0;
        for (PossesedItem item : player.getInventory().array()) {
            if (item != null)
                player.getTradingManager().inventoryItems.add(item, i);
            i++;
        }
    }

    public static void clearPlayerInventory(Player player) {
        for (PossesedItem item : player.getInventory().array()) {
            if (item != null)
                player.getInventory().remove(item);
        }
    }

    public ItemContainer getInventoryItems() {
        return this.inventoryItems;
    }

    public ItemContainer getTradeScreenItems() {
        return this.tradeScreenItems;
    }

    public ItemContainer getLentItem() {
        return this.lentItem;
    }

    public static void finishTrade(Player player, Player player2) {
        swapTradedItems(player, player2);
        swapTradedItems(player2, player);
        Static.callScript("trading.sendFinish", player);
        Static.callScript("trading.sendFinish", player2);
        player.getTradingManager().stage = TradeStage.NONE;
        player2.getTradingManager().stage = TradeStage.NONE;
        player.getTradingManager().otherPlayer = null;
        player2.getTradingManager().otherPlayer = null;
    }

    public static void swapTradedItems(Player player, Player player2) {
        clearPlayerInventory(player);
        int i = 0;
        for (PossesedItem item : player.getTradingManager().inventoryItems.array()) {
            if (item != null) {
                player.getInventory().add(item, i);
            }
            i++;
        }
        for (PossesedItem item : player2.getTradingManager().tradeScreenItems.array()) {
            if (item != null) {
                player.getInventory().add(item, -1);
            }
        }
        PossesedItem lending = player2.getTradingManager().lentItem.get(0);
        if (lending != null)
            player.getInventory().add(lending);
    }

    public static void sendInterfaces(Player player, Player player2) {
        if (player2.isConnected() && player2.inGame() && player.isConnected() && player.inGame()) {
            switch (player.getTradingManager().stage) {
                case INITIATED_335:
                    Static.callScript("trading.sendFirstInterface", player);
                    Static.callScript("trading.sendFirstInterface", player2);
                    break;
                case INITIATED_334:
                    Static.callScript("trading.sendSecondInterface", player);
                    Static.callScript("trading.sendSecondInterface", player2);
                    break;
            }
        }
    }

    public static void refreshBoth(Player player, Player player2) {
        refresh(player, player2);
        refresh(player2, player);
    }

    public static void refresh(Player player, Player player2) {
        Static.callScript("trading.sendRefresh", player, player2, player.getTradingManager().inventoryItems, player.getTradingManager().tradeScreenItems, player2.getTradingManager().tradeScreenItems, player.getTradingManager().lentItem, player2.getTradingManager().lentItem, player2.getTradingManager().inventoryItems);
    }

    public static void handleInventoryOptions(Player player, int opcode, int button) {
        switch (opcode) {
            case 1:
                offerItem(player, 1, button);
                break;
            case 2:
                offerItem(player, 5, button);
                break;
            case 3:
                offerItem(player, 10, button);
                break;
            case 4:
                offerItem(player, 0, button);
                break;
            case 5:
                player.getAttributes().set("offerXIndex", button);
                player.getAttributes().set("inputId", Constants.Inputs.TRADING_OFFER_X);
                Static.proto.requestAmountInput(player);
                break;
            case 6:
                Static.proto.sendMessage(player, "This item can not be valued until the Grand Exchange is up and running.");
                break;
            case 7:
                Static.proto.sendMessage(player, "Item lending is not yet implemented as a feature.");
                //offerItem(player, -1, button);
                break;
        }
    }

    public static void offerItem(Player player, int amount, int button) {
        PossesedItem i = player.getTradingManager().inventoryItems.get(button);
        if (i != null) {
            switch (amount) {
                case -1:
                    if (i.getDefinition().lendedParentId != -1 && i.getDefinition().lendedParentId2 == -1 && player.getTradingManager().lentItem.remaining() > 0) {
                        player.getTradingManager().inventoryItems.remove(i, button);
                        player.getTradingManager().lentItem.add(i);
                        setTradeStagesInitiation335(player, player.getTradingManager().otherPlayer);
                    } else
                        Static.proto.sendMessage(player, "You can not lend this item.");
                    break;
                default:
                    if (i.getDefinition().lendedParentId2 == -1) {
                        if (amount == 0) {
                            if (i.getDefinition().isStackable())
                                amount = i.getAmount();
                            else
                                amount = player.getTradingManager().inventoryItems.amount(i.getId());
                        }
                        if (player.getTradingManager().inventoryItems.amount(i.getId()) < amount) {
                            amount = player.getTradingManager().inventoryItems.amount(i.getId());
                            Static.proto.sendMessage(player, "You don't have enough items to do this.");
                        }
                        PossesedItem item = new PossesedItem(i.getId(), amount);
                        if (player.getTradingManager().inventoryItems.contains(item.getId(), item.getAmount())) {
                            player.getTradingManager().tradeScreenItems.add(item, -1);
                            player.getTradingManager().inventoryItems.remove(item, button);
                            setTradeStagesInitiation335(player, player.getTradingManager().otherPlayer);
                        }
                    } else
                        Static.proto.sendMessage(player, "You can not trade a lent item.");
            }
            refreshBoth(player, player.getTradingManager().otherPlayer);
        }
    }

    public static void handleTradeScreenOptions(Player player, int opcode, int interfaceId, int button, int button2, int button3) {
        switch (opcode) {
            case 1:
                switch (interfaceId) {
                    case 335:
                        switch (button) {
                            case 16:
                                updateTradeStage(player, player.getTradingManager().otherPlayer, TradeStage.ACCEPTED_335);
                                break;
                            case 12:
                            case 18:
                                sendDecline(player, player.getTradingManager().otherPlayer, "You declined the trade.", "Other player declined the trade.");
                                break;
                            case 56:
                                Static.proto.sendMessage(player, "This feature is not implemented yet.");
                                removeItem(player, -1, button2);
                                break;
                            case 30:
                                removeItem(player, 1, button2);
                                break;
                        }
                        break;
                    case 334:
                        switch (button) {
                            case 21:
                                updateTradeStage(player, player.getTradingManager().otherPlayer, TradeStage.ACCEPTED_334);
                                break;
                            case 6:
                            case 22:
                                sendDecline(player, player.getTradingManager().otherPlayer, "You declined the trade.", "Other player declined the trade.");
                                break;
                        }
                        break;
                }
                break;
            case 51:
                switch (button) {
                    case 57:
                        Static.proto.sendMessage(player, "This feature is not implemented yet.");
                        break;
                    case 30:
                        removeItem(player, 5, button2);
                        break;
                }
                break;
            case 2:
                removeItem(player, 5, button2);
                break;
            case 3:
                removeItem(player, 10, button2);
                break;
            case 4: //remove 0
                break;
            case 5:
                player.getAttributes().set("removeXIndex", button2);
                player.getAttributes().set("inputId", Constants.Inputs.TRADING_REMOVE_X);
                Static.proto.requestAmountInput(player);
                break;
            case 6:
                Static.proto.sendMessage(player, "This item can not be valued until the Grand Exchange is up and running.");
                break;
        }
    }

    public static void removeItem(Player player, int amount, int button) {
        switch (amount) {
            case -1:
                PossesedItem lending = player.getTradingManager().lentItem.get(0);
                if (lending.getDefinition().lendedParentId != -1 && lending.getDefinition().lendedParentId2 == -1) {
                    player.getTradingManager().lentItem.remove(lending);
                    player.getTradingManager().inventoryItems.add(lending);
                    sendLendingModified(player, player.getTradingManager().otherPlayer);
                    setTradeStagesInitiation335(player, player.getTradingManager().otherPlayer);
                } else
                    Static.proto.sendMessage(player, "A lending bug occured, please report and decline trade.");
                break;
            default:
                PossesedItem i = player.getTradingManager().tradeScreenItems.get(button);
                if (i != null) {
                    if (amount == 0) {
                        if (i.getDefinition().isStackable())
                            amount = i.getAmount();
                        else
                            amount = player.getTradingManager().tradeScreenItems.amount(i.getId());
                    }
                    if (player.getTradingManager().tradeScreenItems.amount(i.getId()) < amount) {
                        amount = player.getTradingManager().tradeScreenItems.amount(i.getId());
                        Static.proto.sendMessage(player, "You don't have enough items on the trading screen to do this.");
                    }
                    PossesedItem item = new PossesedItem(i.getId(), amount);
                    if (player.getTradingManager().tradeScreenItems.contains(item.getId(), item.getAmount())) {
                        LinkedList<Integer> modifiedSlots = new LinkedList<Integer>();
                        int index = 0;
                        for (PossesedItem it : player.getTradingManager().tradeScreenItems.array()) {
                            if (it != null) {
                                if (it.getId() == item.getId())
                                    modifiedSlots.add(index);
                            }
                            index++;
                        }
                        player.getTradingManager().inventoryItems.add(item, -1);
                        player.getTradingManager().tradeScreenItems.remove(item, button);
                        sendTradeModified(player, player.getTradingManager().otherPlayer, modifiedSlots);
                        setTradeStagesInitiation335(player, player.getTradingManager().otherPlayer);
                    }
                }
        }
        refreshBoth(player, player.getTradingManager().otherPlayer);
    }

    public static void sendTradeModified(Player player, Player player2, LinkedList<Integer> modifiedSlots) {
        for (int i : modifiedSlots) {
            Static.callScript("trading.sendTradeModified", player, false, i);
            Static.callScript("trading.sendTradeModified", player2, true, i);
        }
    }

    public static void sendLendingModified(Player player, Player player2) {
        Static.callScript("trading.sendLendingModified", player, false);
        Static.callScript("trading.sendLendingModified", player2, true);
    }

    public static void sendDecline(Player player, Player player2, String message, String message2) {
        Static.callScript("trading.sendDecline", player, message);
        Static.callScript("trading.sendDecline", player2, message2);
        player.getTradingManager().stage = TradeStage.NONE;
        player2.getTradingManager().stage = TradeStage.NONE;
        player.getTradingManager().otherPlayer = null;
        player2.getTradingManager().otherPlayer = null;
    }

    public static void handleDisconnection(Player player, Player player2) {
        if (isTrading(player))
            if (player2 != null && player2 == player.getTradingManager().otherPlayer)
                if (isTrading(player2))
                    sendDecline(player, player.getTradingManager().otherPlayer, "You declined the trade.", "Other player declined the trade.");
    }

    public static void updateTradeStage(Player player, Player player2, TradeStage stage) {
        player.getTradingManager().stage = stage;
        if (player.getTradingManager().stage == TradeStage.NONE && player2.getTradingManager().stage == TradeStage.NONE) {
            setTradeStage(player, TradeStage.INITIATED_335);
            setTradeStage(player2, TradeStage.INITIATED_335);
        } else if (player.getTradingManager().stage == TradeStage.ACCEPTED_335 && player2.getTradingManager().stage == TradeStage.ACCEPTED_335) {
            // TODO Take into calculation lending items!
            boolean noSpace1 = player.getTradingManager().tradeScreenItems.size() + player2.getTradingManager().inventoryItems.size() < 29;
            boolean noSpace2 = player2.getTradingManager().tradeScreenItems.size() + player.getTradingManager().inventoryItems.size() < 29;
            if (noSpace1 && noSpace2) {
                setTradeStage(player, TradeStage.INITIATED_334);
                setTradeStage(player, TradeStage.INITIATED_334);
                sendInterfaces(player, player2);
            } else
                sendDecline(player, player2, !noSpace1 ? "Other player has not enough inventory space to make this trade." : "You don't have enough inventory space to make this trade.", !noSpace1 ? "You don't have enough inventory space to make this trade." : "Other player has not inventory space to make this trade.");
        } else if (player.getTradingManager().stage == TradeStage.ACCEPTED_334 && player2.getTradingManager().stage == TradeStage.ACCEPTED_334)
            finishTrade(player, player2);
        refreshBoth(player, player2);
    }
}
