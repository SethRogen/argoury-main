importPackage(com.ziotic)
importPackage(com.ziotic.logic.item)
importPackage(com.ziotic.logic.utility)

invoke("interfaces")

function handleButton(player, opcode, interfaceId, b, b2, b3) {
    switch(interfaceId) {
    case 670:
        wieldEquipment(player, b3, b2)
        break
    case 982:
        if((b >= 45 && b <= 62) || b == 37) {
            if(b == 37) {
                if(player.getFriends().getPrivateChatColor() == 0) {
                    player.getFriends().setPrivateChatColor(1)
                } else {
                    player.getFriends().setPrivateChatColor(0)
                }
            } else {
                player.getFriends().setPrivateChatColor(b - 44)
            }
        } else {
            switch(b) {
            case 5:
                Static.proto.sendCloseInterface(player)
                break
            default:
                unhandledButton(player, opcode, interfaceId, b, b2, b3)
                break
            }
        }
        break
    case 261:
        switch(b) {
        case 14:
            Static.proto.sendInterface(player, GRAPHIC_OPTIONS)
            break
        case 16:
            Static.proto.sendInterface(player, AUDIO_OPTIONS)
            break
        case 5:
            Static.proto.sendInterface(player, PRIVATE_CHAT_OPTIONS)
            break
        default:
            unhandledButton(player, opcode, interfaceId, b, b2, b3)
            break
        }
        break
    case 750:
        switch(opcode) {
        case 1:
            toggleRun(player)
            break
        default:
            unhandledButton(player, opcode, interfaceId, b, b2, b3)
            break
        }
        break
    case 182:
        switch(b) {
        case 5:
        	if (player.getCombat().canLogout())
        		Static.proto.sendExitToLobby(player)
        	else
        		Static.proto.sendMessage(player, "You can\'t log out until 10 seconds after the end of combat.")
            break
        case 10:
        	if (player.getCombat().canLogout())
        		Static.proto.sendExitToLogin(player)
        	else 
        		Static.proto.sendMessage(player, "You can\'t log out until 10 seconds after the end of combat.")
            break
        default:
            unhandledButton(player, opcode, interfaceId, b, b2, b3)
            break
        }
        break
    case 746:
    	switch (b) {
    	case 225:
    		switch (opcode) {
    		case 8:
        		player.getLevels().setXPGained(0)
        		player.updateXPCounter()
    			break;
    			default:
    				unhandledButton(player, opcode, interfaceId, b, b2, b3)
    				break
    		}
    		break;
    		default:
    			unhandledButton(player, opcode, interfaceId, b, b2, b3)
    			break
    	}
    default:
        unhandledButton(player, opcode, interfaceId, b, b2, b3)
        break
    }
}

function toggleRun(player) {
    player.setRunning(!player.isRunning())
    Static.proto.sendConfig(player, 173, player.isRunning() ? 1 : 0)
}

function unhandledButton(player, opcode, interfaceId, button, button2, button3) {
    logger().debug("Unhandled button [interface=" + interfaceId + ", button=" + button + ", button2=" + button2 + ", button3=" + button3 + ", opcode=" + opcode + "]")
}