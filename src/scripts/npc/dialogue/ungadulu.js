importPackage(com.runescape.content.magictemp)
importPackage(com.runescape.engine.tick)
importPackage(com.runescape.logic.map)
importPackage(com.runescape.logic.mask)
importPackage(com.runescape.logic.dialogue)

function dialogue() {
    var map = new java.util.HashMap()

    map.put(0, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { return [ "Can I help you?" ] },
        handle : function(convo) { convo.stage(1) }
    }))

    map.put(1, new OptionDialogue({
        title : function() { return "What would you like to say?" },
        text : function(convo) {
            return [ "I would like to go to Edgeville.",
		"Nevermind." ]
        },
        handle : function(convo, option) {
            switch(option) {
                case 0:
                    convo.stage(2)
                    break
                case 1:
                    convo.stage(3)
                    break
            }
        }
    }))

    map.put(2, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.PLAYER },
        text : function(convo) { return [ "I would like to go to Edgeville." ] },
        handle : function(convo) { convo.stage(4) }
    }))

    map.put(3, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.PLAYER },
        text : function(convo) { return [ "Nevermind, sorry." ] },
        handle : function(convo) { convo.end() }
    }))

    map.put(4, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { return [ "How did I not see that question comming?", 	
					"Alrighty then..." ] },
        handle : function(convo) {
		convo.end()

		teleportUsingNPC(convo.getPlayer(), convo.getNPC(), Tile.locate(3086, 3496, 0).randomize(3, 2))	
	}
    }))

    return map
}

function teleportUsingNPC(player, npc, loc) {
	npc.doAnimation(1818)
	npc.doGraphics(343)
	npc.faceEntity(player)
	player.faceEntity(npc)

	var tick = new RunnableTick(new java.lang.Runnable({
	    run : function() {
		    MagicTemp.teleport(player, loc, 4, 1816, -1, new Graphic(342), null, 0, false)

		    tick.stop()
	    }
	}), 3)
	player.registerTick(tick)
}