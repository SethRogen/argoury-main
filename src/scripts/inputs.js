importPackage(com.ziotic)
importPackage(com.ziotic.content.cc)
importPackage(com.ziotic.logic.item)
importPackage(com.ziotic.logic.utility)

function handleInput(player, inputId, input) {
    switch(inputId) {
        case Constants.Inputs.CC_PREFIX:
            ClanManager.changeClanPrefix(player, input)
            break
        case Constants.Inputs.BANK_WITHDRAW_X:
			if(player.getCurrentInterface() == GameInterfaces.BANK_SCREEN) {
				BankManager.withdrawItem(player, player.getAttributes().getInt("withdrawXIndex"), input)
				player.getAttributes().set("withdrawX", input)
			}
            break
        case Constants.Inputs.BANK_DEPOSIT_X:
			if(player.getCurrentInterface() == GameInterfaces.BANK_SCREEN) {
				BankManager.depositItem(player, player.getAttributes().getInt("depositXIndex"), input)
				player.getAttributes().set("depositX", input)
			}
			break
        case Constants.Inputs.TRADING_OFFER_X:
        	player.getTradingManager().offerItem(player, input, player.getAttributes().getInt("offerXIndex"))
        	break
        case Constants.Inputs.TRADING_REMOVE_X:
        	player.getTradingManager().removeItem(player, input, player.getAttributes().getInt("removeXIndex"))
        	break
        default:
            logger().error("Unhandled input id : " + inputId)
            break  
    }
}