importPackage(com.runescape.logic.item)
importPackage(com.runescape.logic.player)
importPackage(com.runescape.logic.utility)

var interfaces = this

var NULL_SCREEN = new GameInterface(56)

var GRAPHIC_OPTIONS = new GameInterface(742)
var AUDIO_OPTIONS = new GameInterface(743)

var PRIVATE_CHAT_OPTIONS = new GameInterface(982, [214, 99], new NodeRunnable({
    run : function(player) {
        if(player.getDisplayMode() == DisplayMode.FIXED) {
            Static.proto.sendAccessMask(player, -1, -1, 548, 102, 0, 2)
        } else {
            Static.proto.sendAccessMask(player, -1, -1, 746, 48, 0, 2)
        }
    }
}), new NodeRunnable({
    run : function(player) {
        if(player.getDisplayMode() == DisplayMode.FIXED) {
            Static.proto.sendInterface(player, 261, 548, 214, true)
            Static.proto.sendAccessMask(player, -1, -1, 548, 102, 0, 2)
        } else {
            Static.proto.sendInterface(player, 261, 746, 99, true)
            Static.proto.sendAccessMask(player, -1, -1, 746, 48, 0, 2)
        }
        Static.proto.sendInterfaceScript(player, 1297)
    }
}))

var TRADE_SCREEN_INVENTORY = new GameInterface(336, [197, 84], new NodeRunnable({
	run : function(player) {
		Static.proto.sendAccessMask(player, 0, 27, 336, 0, 0, 54)
		player.getInventory().refresh()
	} 
})) 

var TRADE_SCREEN_335 = new GameInterface(335, null, new NodeRunnable({
	run : function(player) {
		Static.proto.sendConfig(player, 259, 0)
		Static.proto.sendInterface(player, interfaces.TRADE_SCREEN_INVENTORY)
		Static.proto.sendInterfaceScript(player, 695, 21954593, 90, 4, 7, 0, -1, 'Value<col=ff9040>', '', '', '', '', '', '', '', '')
		Static.proto.sendAccessMask(player, 0 , 27, 335, 33, 0, 2)
		Static.proto.sendInterfaceScript(player, 150, 21954590, 90, 4, 7, 0, -1, 'Remove<col=ff9040>', 'Remove-5<col=ff9040>', 'Remove-10<col=ff9040>',
				'Remove-All<col=ff9040>', 'Remove-X<col=ff9040>', 'Value<col=ff9040>', '', '', '')
		Static.proto.sendAccessMask(player, 0, 27, 335, 30, 0, 126)
		Static.proto.sendInterfaceScript(player, 150, 22020096, 93, 4, 7, 0, -1, 'Offer<col=ff9040>', 'Offer-5<col=ff9040>', 
    			'Offer-10<col=ff9040>', 'Offer-All<col=ff9040>', 'Offer-X<col=ff9040>', 'Value<col=ff9040>', 'Lend<col=ff9040>', '', '')
    	Static.proto.sendAccessMask(player, 0, 27, 336, 0, 0, 254)
    	Static.proto.sendAccessMask(player, -1, -1, 335, 56, 0, 2)
    	Static.proto.sendAccessMask(player, -1, -1, 335, 57, 0, 6)
		Static.proto.sendAccessMask(player, -1, -1, 335, 52, 0, 0)
		Static.proto.sendString(player, 335, 15, 'Trading with: ')
	}
}))

var TRADE_SCREEN_334 = new GameInterface(334, null, new NodeRunnable({
	run : function(player) {
	    // any packets needed here
		Static.proto.sendString(player, 335, 54, '')
	}
}))

var IKOD = new GameInterface(102, null, new NodeRunnable({
	run : function(player) {
		Static.proto.sendAccessMask(player, 0, 4, 102, 18, 0, 2)
		Static.proto.sendAccessMask(player, 0, 42, 102, 21, 0, 2)
		var manager = player.getItemsOnDeathManager()
		var ikod = manager.getIOD(player, ItemsOnDeathManager.ReturnType.INTERFACE)[0]
		Static.proto.sendInterfaceScript(player, 118, manager.getSafetyType().ordinal(), 
				manager.itemAmountKept(player), 
				ikod.array()[0].getId(), 
				ikod.array()[1].getId(), 
				ikod.array()[2].getId(), 
				ikod.array()[3].getId(), 
				manager.isSkulled() ? 1 : 0, 
				manager.hasBob() ? 1 : 0, 
				"You're marked with a <col=ff3333>skull<col=ff981f>.", 
				-1, 
				-1, 
				manager.useGravestone() ? 1 : 0)
	}
}))

function sendIKODInterface(player) {
	Static.proto.sendCloseInterface(player)
	Static.proto.sendInterface(player, IKOD)
}
