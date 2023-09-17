importPackage(com.ziotic.logic.utility)
importPackage(com.ziotic.logic.dialogue)

function dialogue() {
    var map = new java.util.HashMap()

    map.put(0, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) { return [ "Good day. How may I help you?" ] },
        handle : function(convo) { convo.stage(1) }
    }))
    map.put(1, new OptionDialogue({
        title : function() { return "What would you like to say?" },
        text : function(convo) {
            return [ "I'd like to access my bank account, please.",
                     "I'd like to check my PIN settings.",
                     "I'd like to see my collection box.",
                     "What is this place?" ]
        },
        handle : function(convo, option) {
            switch(option) {
                case 0:
                    convo.end()
                    Static.proto.sendInterface(convo.getPlayer(), GameInterfaces.BANK_SCREEN)
                    break
                case 1: // TODO PIN settings
                    // convo.end()
                    convo.stage(9);
                    break
                case 2: // TODO Collection box
                    // convo.end()
                    convo.stage(10);
                    break
                case 3:
                    convo.stage(2)
                    break
            }
        }
    }))
     map.put(2, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.PLAYER },
        text : function(convo) { return [ "What is this place?" ] },
        handle : function(convo) { convo.stage(3) }
    }))
    map.put(3, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) {
            return [ "This is a branch of the Bank of Runescape. We have",
                     "branches in many towns." ]
        },
        handle : function(convo) { convo.stage(4) }
    }))
    map.put(4, new OptionDialogue({
        title : function() { return "What would you like to say?" },
        text : function(convo) {
            return [ "And what do you do?",
                     "Didn't you used to be called the Bank of Varrock?" ]
        },
        handle : function(convo, option) {
            switch(option) {
                case 0:
                    convo.stage(5)
                    break
                case 1:
                    convo.stage(7)
                    break
            }
        }
    }))
    map.put(5, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.PLAYER },
        text : function(convo) { return [ "And what do you do?" ] },
        handle : function(convo) { convo.stage(6) }
    }))
    map.put(6, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) {
            return [ "We will look after your items and money for you.",
                     "Leave your valuables with us if you want to keep them",
                     "safe." ]
        },
        handle : function(convo) { convo.end() }
    }))
    map.put(7, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.PLAYER },
        text : function(convo) { return [ "Didn't you used to be called the Bank of Varrock?" ] },
        handle : function(convo) { convo.stage(8) }
    }))
    map.put(8, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) {
            return [ "Yes we did, but people kept on coming into our",
                     "branches outside of Varrock and telling us that our",
                     "signs were wrong. They acted as if we didn't know",
                     "what town we were in or something." ]
        },
        handle : function(convo) { convo.end() }
    }))

    map.put(9, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) {
            return [ "Currently you cannot view or modify any PIN settings.",
                     "We're sorry for this."]
        },
        handle : function(convo) { convo.end() }
    }))
    map.put(10, new StatementDialogue({
        pov : function() { return StatementDialogue.POV.NPC },
        text : function(convo) {
            return [ "Currently you cannot view your collection box.",
                     "We're sorry for this."]
        },
        handle : function(convo) { convo.end() }
    }))

    return map
}