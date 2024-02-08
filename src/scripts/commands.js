importPackage(com.runescape)
importPackage(com.runescape.content.tutorial)
importPackage(com.runescape.content.combat)
importPackage(com.runescape.content.shop)
importPackage(com.runescape.content.misc)
importPackage(com.runescape.engine.tick)
importPackage(com.runescape.logic.item)
importPackage(com.runescape.logic.map)
importPackage(com.runescape.logic.mask)
importPackage(com.runescape.logic.npc)
importPackage(com.runescape.logic.object)
importPackage(com.runescape.logic.utility)
importPackage(com.runescape.utility.script)
importPackage(com.runescape.content.prayer)

invoke("interfaces")

function checkCombat(player) {
    if(player.isInPVP() && player.getCombat().inCombat()) {
        Static.proto.sendMessage(player, "You must be outside this PVP zone and not engaged in combat to use this command!")
        return false
    }
    return true
}

function checkInPVP(player) {
	if (Combat.isInPVPZone(player)) {
		Static.proto.sendMessage(player, "You must be outside this PVP zone to use this command!")
		return true
	}
	return false
}

function checkMod(player) {
    if(!player.isModerator()) {
        Static.proto.sendMessage(player, "You must be a moderator in order to use this command!")
        return false
    }
    return true
}

function checkAdmin(player) {
    if(!player.isAdministrator()) {
        Static.proto.sendMessage(player, "You must be an administrator in order to use this command!")
        return false
    }
    return true
}

function checkMember(player) {
    if(!player.isMember() && !player.isModerator()) {
        player.onMembersOnlyFeature()
        return false
    }
    return true
}

function handleCommand(player, commandQuery, cmd, args, string) {
    if(player.getPathProcessor().getMoveSpeed() == PathProcessor.MOVE_SPEED_WALK) {
        return
    }
	
	if(cmd.equals("lolbitch")) {
		Static.proto.sendConfig(player, 1175, 10);
		return;
	}
	
    if(cmd.equals("item") && !checkInPVP(player)) {
        var id = java.lang.Integer.parseInt(args[0])
        var amount = 1
        if(args.length > 1) {
            amount = java.lang.Integer.parseInt(args[1])
        }
        if (ItemDefinition.forId(id) != null) {
        	if (!ItemDefinition.forId(id).isStackable()) {
        		if(amount > player.getInventory().remaining()) {
            		amount = player.getInventory().remaining()
            	}
        	}
        }
        player.getInventory().add(id, amount)
    } else if(cmd.equals("empty")) {
        player.getInventory().clear()
    } else if(cmd.equals("check") && checkAdmin(player)) {
	for(var i = 0; i < 100; i++)
	    Static.proto.sendMessage(player,  ("its time:" + i), player.getClanOwner(), i)
    } else if(cmd.equals("inter") && checkAdmin(player)) {
        Static.proto.sendCloseInterface(player)
        var id = java.lang.Integer.parseInt(args[0])
        if(args.length == 1) {
            Static.proto.sendInterface(player, new GameInterface(id))
        } else {
            var loc = java.lang.Integer.parseInt(args[1])
            Static.proto.sendInterface(player, new GameInterface(id, [loc, loc], null))
        }
    } else if(cmd.equals("groto")) {
	    player.setTeleportDestination(Tile.locate(2970, 9672, 0))
    } else if(cmd.equals("tele") && checkAdmin(player)) {
        var x = java.lang.Integer.parseInt(args[0])
        var y = java.lang.Integer.parseInt(args[1])
        var z = player.getZ()
        if(args.length > 2) {
            z = java.lang.Integer.parseInt(args[2])
        }
        player.setTeleportDestination(Tile.locate(x, y, z))
    } else if(cmd.equals("master") && !checkInPVP(player)) {
        for(var i = 0; i < 25; i++)
            player.getLevels().addXP(i, 200000000)
    } else if(cmd.equals("reload") && checkAdmin(player)) {
        Static.js = new JavaScriptManager()
    } else if(cmd.equals("loc")) {
        Static.proto.sendMessage(player, "Your current location is: " + player.getLocation())
    } else if(cmd.equals("obj") && checkAdmin(player)) {
        var id = java.lang.Integer.parseInt(args[0]);
        var type = 0
        var dir = java.lang.Integer.parseInt(args[1])

	var name = ObjectDefinition.forId(id).name
	var solid = ObjectDefinition.forId(id).walkable

	player.sendMessage(name)

        Static.world.getObjectManager().add(id, player.getLocation(), type, dir)
    } else if(cmd.equals("die") && checkAdmin(player)) {
        player.setHP(0)
    } else if(cmd.equals("out") && checkAdmin(player)) {
        Static.world.unregister(player, true)
    } else if(cmd.equals("lvl") && !checkInPVP(player)) {
        var id = java.lang.Integer.parseInt(args[0])
        var level = java.lang.Integer.parseInt(args[1])
        player.getLevels().doLevelCommand(id, level)
//        if(id > Levels.SKILL_COUNT || id < 0) {
//            Static.proto.sendMessage(player, "Invalid skill id set!")
//            return
//        }
//        if(level > 99 || level <= 0 || id == 3 && level < 10) {
//            Static.proto.sendMessage(player, "Invalid level amount set!")
//            return
//        }
//        
//        player.getLevels().setXP(id, xpForLevel(level))
//        player.getLevels().setLevel(id, level)
//        player.getLevels().setCurrentLevel(id, level)
//        Static.proto.sendLevel(player, id)
//        player.getLevels().calculateCombat()
    } else if(cmd.equals("noclip") && checkAdmin(player)) {
        player.setClipping(false)
    } else if(cmd.equals("clip") && checkAdmin(player)) {
        player.setClipping(true)
    }  else if(cmd.equals("animate") && checkMember(player)) {
        if(args != null) {
            if(args.length == 2) {
                player.doAnimation(java.lang.Integer.parseInt(args[0]), java.lang.Integer.parseInt(args[1]))
            } else {
                player.doAnimation(java.lang.Integer.parseInt(args[0]))
            }
        }
    } else if(cmd.equals("graphics") && checkMember(player)) {
        if(args != null) {
            if(args.length == 3) {
                player.doGraphics(java.lang.Integer.parseInt(args[0]), java.lang.Integer.parseInt(args[1]), java.lang.Integer.parseInt(args[2]))
            } else if(args.length == 2) {
                player.doGraphics(java.lang.Integer.parseInt(args[0]), java.lang.Integer.parseInt(args[1]))
            } else {
                player.doGraphics(java.lang.Integer.parseInt(args[0]))
            }
        }
    } else if(cmd.equals("strings") && checkAdmin(player)) {
        var inter = java.lang.Integer.parseInt(args[0])
        var max = java.lang.Integer.parseInt(args[1])

        for(var i=0; i<max; i++) {
            Static.proto.sendString(player, inter, i, "" + i)
        }
    } else if(cmd.equals("hit") && checkAdmin(player)) {
    	var damage = 500

    	player.getMasks().submitSplat(new Splat(null, damage, Splat.SplatType.SOAK, null))
    } else if(cmd.equals("msg") && checkAdmin(player)) {
    	var req = java.lang.Integer.parseInt(args[0])

    	Static.proto.sendMessage(player, "Test-" + req, null, req)
    } else if( cmd.equals("openge") && checkAdmin(player)) {
		Static.proto.sendConfig(player, 1109, -1);
		Static.proto.sendConfig(player, 1110, 0);
		Static.proto.sendConfig(player, 1111, 0);
		Static.proto.sendConfig(player, 1112, -1);
		Static.proto.sendConfig(player, 1113, -1);
		Static.proto.sendConfig(player, 1114, 0);
		Static.proto.sendConfig(player, 1115, 0);
		Static.proto.sendConfig(player, 1116, 0);
        Static.proto.sendInterfaceVariable(player, 199, -1);
        Static.proto.sendInterface(player, 105, 548, 18, false);
    } else if(cmd.equals("buyge") && checkAdmin(player)) {
		Static.proto.sendConfig(player, 1109, -1);
		Static.proto.sendConfig(player, 1110, 0);
		Static.proto.sendConfig(player, 1111, 0);
		Static.proto.sendConfig(player, 1112, 0); //slot
		Static.proto.sendConfig(player, 1113, 0); // buy
		Static.proto.sendConfig(player, 1114, 0);
		Static.proto.sendConfig(player, 1115, 0);
		Static.proto.sendConfig(player, 1116, 0);
        Static.proto.sendInterface(player, 449, 548, 197, false);
        Static.proto.sendInterfaceVariable(player, 1241, 16750848);
        Static.proto.sendInterfaceVariable(player, 1242, 15439903);
        Static.proto.sendInterfaceVariable(player, 741, -1);
        Static.proto.sendInterfaceVariable(player, 743, -1);
        Static.proto.sendInterfaceVariable(player, 744, 0);
        Static.proto.sendInterface(player, 389, 752, 7, true);
        Static.proto.sendInterfaceScript(player, 570, "Grand Exchange Item Search");
    } else if(cmd.equals("selectge") && checkAdmin(player)) {
        Static.proto.sendConfig(player, 1109, 564);
        Static.proto.sendConfig(player, 1114, 74);
        Static.proto.sendConfig(player, 1109, 564);
        Static.proto.sendConfig(player, 1111, 74);
        Static.proto.sendInterfaceVariable(player, 1241, 16750848);
        Static.proto.sendInterfaceVariable(player, 1242, 15439903);
        Static.proto.sendInterfaceVariable(player, 741, 564);
        Static.proto.sendInterfaceVariable(player, 743, -1);
        Static.proto.sendInterfaceVariable(player, 744, 0);
        Static.proto.sendInterfaceVariable(player, 746, -1);
        Static.proto.sendInterfaceVariable(player, 168, 98);
    } else if(cmd.equals("pnpc") && checkMember(player)) {
        if(player.isInPVP()) {
            Static.proto.sendMessage(player, "You must be outside a PVP zone to execute this command!");
            return
        }

        var id = java.lang.Integer.parseInt(args[0])

        if(id != -1) {
            var def
            if(id < 0 || (def = NPCDefinition.forId(id)) == null) {
                Static.proto.sendMessage(player, "Invalid NPC id set!")
                return
            }

            if(def.size > 3 && !player.isAdministrator()) {
                Static.proto.sendMessage(player, "This NPC is too big!")
                return
            }
        }

        player.getAppearance().toNPC(id)
        player.getAppearance().refresh()
    } else if (cmd.equals("switchprayer") && checkCombat(player)) {
    	PlayerPrayerManager.switchPrayerBook(player, java.lang.Integer.parseInt(args[0]) == 0 ? true : false)
    } else if(cmd.equals("dir") && checkAdmin(player)) {
        var dir = java.lang.Integer.parseInt(args[0])

        player.faceDirection(dir)
    } else if(cmd.equals("tut") && checkAdmin(player)) {
	    Tutorial.onFirstLogin(player)
    } else if(cmd.equals("npc") && checkAdmin(player)) {
	    Static.world.register(new NPC(new NPCSpawn(java.lang.Integer.parseInt(args[0]), player.getLocation(), null, 4, false)))
    } else if(cmd.equals("findnpc") && checkAdmin(player)) {
        player.sendMessage("Searching...")
        for(var i = 0; i<13592; i++) {
            var def = NPCDefinition.forId(i)
            if(def != null) {
                if(def.size == 1 && def.combatLevel > 0 && def.options != null) {
                    var yes = false
                    for(var i2 = 0; i2 < def.options.length; i2++) {
                        if(def.options[i2] != null && def.options[i2].toLowerCase().contains("bank")) {
                            yes = true
                        }
                    }
                    if(yes) {
                        player.sendMessage("id: " + i + ", name: " + def.name);
                    }

                }
            }
        }
        player.sendMessage("done")
    } else if (cmd.equals("findnpc2") && checkAdmin(player)) {
    	player.sendMessage("Searching for \' " + args[0] + "\'...")
    	for (var i = 0; i < 13592; i++) {
    		var def = NPCDefinition.forId(i)
    		if (def != null) {
    			if (def.name.toLowerCase().contains(args[0])) {
//    				�
    					player.sendMessage("Name: " + def.name + " id: " + i)
    			}    				
    		}
    	}
    } else if (cmd.equals("findweapon") && checkAdmin(player)) {
    	player.sendMessage("Searching...")
    	var groupId = java.lang.Integer.parseInt(args[0])
    	var offset = java.lang.Integer.parseInt(args[1])
    	var j = 0;
    	for(var i = 0; i < 20000; i++) {
    		var def = ItemDefinition.forId(i)
    		if (def != null) {
    			if (def.weaponGroupId == groupId) {
    				player.sendMessage("id: " + i)
    				j++
    				if (j >= offset) {
    					break
    				}
    			}
    		}
    	}
    } else if (cmd.equals("reloadspells") && checkAdmin(player)) {
    	Magic.load();
    } else if (cmd.equals("home") && (!checkInPVP(player) || checkAdmin(player))) {
    	player.setTeleportDestination(Tile.locate(2663, 3306, 0))
    } else if (cmd.equals("edge") && !checkInPVP(player)) {
    	player.setTeleportDestination(Tile.locate(3088, 3501, 0))
    } else if (cmd.equals("magebank") && !checkInPVP(player)) {
    	player.setTeleportDestination(Tile.locate(2534, 4715, 0))
    } else if (cmd.equals("switchspellbook") && !checkInPVP(player)) {
    	Magic.switchMagic(player);
    } else if (cmd.equals("shop") && checkAdmin(player) && checkCombat(player)) {
        ShopManager.openShop(player, java.lang.Integer.parseInt(args[0]))
    } else if (cmd.equals("searchinter")) {
    	SearchInterface.searchInterface(player, 0, 1000, 2)
	} else if (cmd.equals("chardesign") && !checkInPVP(player)) {
		player.initiateCharacterDesign()
	} else if (cmd.equals("players")) {
		Static.proto.sendMessage(player, "There is currently <col=ff0000>"  + Static.world.getPlayers().size() + "</col> players online!")
 	} else if (cmd.equals("yell") && checkMod(player)) {
 		Yell.yell(Yell.YellType.forString(args[0]), string);
 	} else if (cmd.equals("mute") && checkMod(player)) {
 		Static.world.mutePlayer(player.getProtocolName(), args[0], true)
 	} else if (cmd.equals("unmute") && checkMod(player)) {
 		Static.world.mutePlayer(player.getProtocolName(), args[0], false)
 	} else if (cmd.equals("teletome") && checkAdmin(player)) {
 		Static.world.teleportPlayerToMe(player, args[0])
 	} else if (cmd.equals("kick") && checkMod(player)) {
 		Static.world.kick(args[0]);
 	} else if (cmd.equals("restorespecial") && checkAdmin(player)) {
 		player.getCombat().getSpecialEnergy().increase(100)
 	} else if (cmd.equals("iconfig")) {
 		Static.proto.sendInterfaceShowConfig(player, java.lang.Integer.parseInt(args[0]), java.lang.Integer.parseInt(args[1]), java.lang.Integer.parseInt(args[2]) == 1 ? true : false)
 	} else if (cmd.equals("worldmap")) {
// 		//Static.proto.sendInterfaceVariable(player, 674, 51858846)
// 		Static.proto.sendInterfaceScript(player, "", 3336)
// 		//Static.proto.sendInterface(player, �new GameInterface(755))
// 		Static.proto.sendWindow(player, 753)
// 		Static.proto.sendInterface(player, 755, 753, 1, false)
 		
 		
 		Static.proto.sendWindow(player, 549)
 		Static.proto.sendInterface(player, 549, 56, 3, true)
 		Static.proto.sendInterface(player, 549, 378, 2, false)
 		Static.proto.sendInterface(player, 549, 755, 2, false)
 	} else if (cmd.equals("resetmap")) {
 		Static.proto.sendWindow(player, 746)
 		Static.proto.sendResizableScreen(player)
 	} else if (cmd.equals("getobj")) {
 		var x = java.lang.Integer.parseInt(args[0])
 		var y = java.lang.Integer.parseInt(args[1])
 		
 		var obj = Region.getObject(Tile.locate(x, y, 0))
 		var clipValue = Region.getAbsoluteClipping(x, y, 0);
 		var id = 0
 		var type = 0
 		var direction = 0
 		if (obj != null) {
 			id = obj.getId()
 			type = obj.getType()
 			direction = obj.getDirection()
 		} else
 			Static.proto.sendMessage(player, "obj is null")
 		
 		Static.proto.sendMessage(player, "obj: " + id + " type: " + type + " direction: " + direction + " clipping value: " + clipValue)
 	} else  {
    	Static.proto.sendMessage(player, "Unhandled command: <col=ff0000>" + cmd)
    }
}

function xpForLevel(level) {
    var points = 0, output = 0
    for (var lvl = 1; lvl <= level; lvl++) {
        points += java.lang.Math.floor(lvl + 300.0 * java.lang.Math.pow(2.0, lvl / 7.0))
        if (lvl >= level) {
            return output
        }
        output = java.lang.Math.floor(points / 4)
    }
    return 0
}
