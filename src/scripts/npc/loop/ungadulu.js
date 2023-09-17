function loop(npc) {
	switch(random(0, 3)) {
		case 0:
			npc.doForceChat("Talk with me if you want to go to the wilderness!")
			break;
		case 1:
			npc.doForceChat("Teleporting people to Edgeville for free!")
			break;
		case 2:
			npc.doForceChat("Get your free ride to the wilderness while you can!")
			break;
		case 3:
			npc.doForceChat("Want to kill some players? Ask me to teleport you!")
			break;
	}
	

	return random(3, 10)
}