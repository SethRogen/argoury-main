importPackage(com.runescape)
importPackage(com.runescape.content.trading)
importPackage(com.runescape.logic.player)
importPackage(com.runescape.logic.utility)
importPackage(com.runescape.logic.item)

invoke("interfaces")

function sendFirstInterface(player) {	
	Static.proto.sendCloseInterface(player)
	Static.proto.sendInterface(player, interfaces.TRADE_SCREEN_335)
	Static.proto.sendConfig(player, 259, 0)
	Static.proto.sendString(player, 335, 15, 'Trading with: ' + player.getTradingManager().getOtherPlayer().getName())
	Static.proto.sendString(player, 335, 37, '')
	Static.proto.sendString(player, 335, 43, 'No limits!')  
	Static.proto.sendString(player, 335, 44, 'No limits!')
	Static.proto.sendString(player, 335, 22, player.getTradingManager().getOtherPlayer().getName())
}

function sendSecondInterface(player) {
	Static.proto.sendCloseInterface(player)
	Static.proto.sendInterface(player, interfaces.TRADE_SCREEN_334)
	Static.proto.sendString(player, 334, 34, "Are you sure you want to make this trade?")
	Static.proto.sendString(player, 334, 54, player.getTradingManager().getOtherPlayer().getName())
}

function sendRequest(player, requesting) {
	Static.proto.sendMessage(player, 'wishes to trade with you', requesting.getName(), 100)
	Static.proto.sendMessage(requesting, "Sending trade offer...")
}

function sendDecline(player, string) {
	Static.proto.sendCloseInterface(player);
	InventoryListener.INSTANCE.event(player.getInventory(), ItemListener.ItemEventType.CHANGE, -1)
	Static.proto.sendMessage(player, string)
}

function sendFinish(player) {
	Static.proto.sendCloseInterface(player)
	InventoryListener.INSTANCE.event(player.getInventory(), ItemListener.ItemEventType.CHANGE, -1)
}

/**
 * @param player
 * @param player2
 * @param container1 Inventory
 * @param container2 This player items on screen
 * @param container3 Other player's items on screen
 * @param container4 This player lent item
 * @param container5 Other player's lent item
 * @param container6 Other player's inventory
 */
function sendRefresh(player, player2, container1, container2, container3, container4, container5, container6) {
	switch (TradingManager.getTradeStage(player)) {
	case TradingManager.TradeStage.INITIATED_335:
	case TradingManager.TradeStage.ACCEPTED_335:
		Static.proto.sendItems(player, 93, false, container1.array())
		Static.proto.sendItems(player, 90, false, container2.array())
		Static.proto.sendItems(player, 90, true, container3.array())
		Static.proto.sendItems(player, 541, false, container4.array())
		Static.proto.sendItems(player, 541, true, container5.array())
		Static.proto.sendString(player, 335, 21, ' has ' + container6.remaining() + ' free inventory slots')
		if (TradingManager.getTradeStage(player) == TradingManager.TradeStage.INITIATED_335 
				|| TradingManager.getTradeStage(player) == TradingManager.TradeStage.ACCEPTED_335) {
			if (TradingManager.getTradeStage(player2) == TradingManager.TradeStage.ACCEPTED_335) {
				Static.proto.sendString(player, 335, 37, 'Other player has accepted...')
				Static.proto.sendString(player2, 335, 37, 'Waiting for other player...')
			} else  {
				Static.proto.sendString(player, 335, 37, '')
				Static.proto.sendString(player2, 335, 37, '')
			}
		} else {
			Static.proto.sendString(player, 335, 37, 'Waiting for other player...')
			Static.proto.sendString(player2, 335, 37, 'Other player has accepted...')
		}
		break
	case TradingManager.TradeStage.INITIATED_334:
	case TradingManager.TradeStage.ACCEPTED_334:
		if (TradingManager.getTradeStage(player) == TradingManager.TradeStage.ACCEPTED_334) { //TradingManager.getTradeStage(player) == TradingManager.TradeStage.INITIATED_334 || 
			if (TradingManager.getTradeStage(player2) == TradingManager.TradeStage.ACCEPTED_334) {
				Static.proto.sendString(player, 334, 35, 'Other player has accepted...')
			} else {
				Static.proto.sendString(player, 334, 35, 'Waiting for other player...')
				Static.proto.sendString(player2, 334, 35, 'Other player has accepted')
			}
		}
		break
	}
}

function sendTradeModified(player, positionBool, slot) {
	if (positionBool) {
		Static.proto.sendInterfaceScript(player, 143, 21954594, 4, 7, slot)
	} else {
		Static.proto.sendInterfaceScript(player, 143, 21954591, 4, 7, slot)
	}
}

function sendLendingModified(player, positionBool) {
	if (positionBool) {
    	Static.proto.sendInterfaceScript(player, 714, 21954608)
	} else {
		Static.proto.sendInterfaceScript(player, 714, 21954607)
	}
	Static.proto.sendInterfaceScript(player, 146, -1)
}
