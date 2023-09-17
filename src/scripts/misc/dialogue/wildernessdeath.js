importPackage(com.ziotic.logic.map)
importPackage(com.ziotic.logic.dialogue)

function dialogue() {
    var map = new java.util.HashMap()

    map.put(0, new OptionDialogue({
        title : function() { return "What would you like to do?" },
        text : function(convo) {
            return [ "Stay here.",
                     "Go back to the wilderness entrance." ]
        },
        handle : function(convo, option) {
            switch(option) {
                case 1:
                    convo.getPlayer().setTeleportDestination(Tile.locate(3130, 3516, 0).randomize(3, 2))
                    break
            }
            convo.end()
        }
    }))

    return map
}