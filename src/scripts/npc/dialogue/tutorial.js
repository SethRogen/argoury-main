importPackage(com.ziotic)
importPackage(com.ziotic.logic.dialogue)
importPackage(com.ziotic.map)

invoke("interfaces")

function dialogue() {
    var map = new java.util.HashMap()

    map.put(0, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { 
		if(convo.getPlayer().isNoob()) {
		    convo.getPlayer().getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_WALK)
		}

		return [ "Welcome to Runescape, " + convo.getPlayer().getName() + "!" ] 
	},
        handle : function(convo) { convo.stage(1)  }
    }))

    map.put(1, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.PLAYER },
        text : function(convo) { return [ "How did you know my name?" ] },
        handle : function(convo) { convo.stage(2) }
    }))

    map.put(2, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { return [ "I know everything my dear." ] },
        handle : function(convo) { convo.stage(3) }
    }))

    map.put(3, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { return [ "Anyways, I'm here to tell you a little",
                                        "about Runescape and how to get started." ] },
        handle : function(convo) { convo.stage(4) }
    }))


    map.put(4, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { return [ "The first thing you need to know is that",
					                    "at the moment, Runescape is in a stage of development",
                                        "known as the 'alpha' stage." ] },
        handle : function(convo) { convo.stage(5) }
    }))

    map.put(5, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { return [ "This is a very early stage of development.",
					                    "In the coming future, Runescape will be released as",
					                    "'beta', and then 'final' stages!" ] },
        handle : function(convo) { convo.stage(6) }
    }))

    map.put(6, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { return [ "If you want to head to the wilderness, talk with Ungadulu.",
					                    "He is a little south-west from here. You can't miss him." ] },
        handle : function(convo) { convo.stage(7) }
    }))

    map.put(7, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { return [ "If you want to enter in a command, use the",
					                    "reverse apostrophe (') key to open up the command",
					                    "console. It is usually located under the ESC key." ] },
        handle : function(convo) { convo.stage(8) }
    }))

    map.put(8, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { return [ "From there type any command you want. Check the",
					                    "forums for a list of commands. Also for a list of",
					                    "items, go to wwww.Runescape.com!" ] },
        handle : function(convo) { convo.stage(9) }
    }))

    map.put(9, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { return [ "That is all! If you need more help with Runescape,",
					                    "ask a moderator for more specific advice." ] },
        handle : function(convo) { convo.stage(10) }
    }))


    map.put(10, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.PLAYER },
        text : function(convo) { return [ "Awesome! Thank you!" ] },
        handle : function(convo) { 
		if(convo.getPlayer().isNoob()) {
		    convo.getPlayer().getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_ANY)
		    convo.getPlayer().setNoob(false)
		}
		convo.end() 
	}
    }))

    return map
}