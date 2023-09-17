importPackage(com.ziotic.logic.npc.summoning)

function handleNPCOption2(player, npcId, npc) {
	if(npc instanceof Familiar) {
		if(npc != player.getFamiliar()) {
			player.sendMessage("This isn't your familiar.")
			return
		}
		var bob = player.getFamiliar().beastOfBurden();
		if (bob != null) {
			bob.open();
			return;
		}
	}
    switch(npcId) {
    default:
        logger().debug("Unhandled NPC option 2 [npcId=" + npcId + "]")
        break
    }
}

function handleNPCOption3(player, npcId, npc) {
    switch(npcId) {
    default:
        logger().debug("Unhandled NPC option 3 [npcId=" + npcId + "]")
        break
    }
}