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
	
var itemsNotAllowed = [ 2438, 2439, 18202, 18203, 18204, 18205, 18206, 18207, 18208, 18209, 18210, 18211, 18212, 18213, 18214, 18215, 18216, 18217, 18218, 18219, 18220, 18221, 18222, 18223, 
18224, 18225, 18226, 18227, 18228, 18229, 18230, 18231, 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239, 18240, 18241, 18242, 18243, 18244, 18245, 18246, 18247, 18248, 18249, 18250, 18251, 18252, 18253, 18254, 18255, 
18256, 18257, 18258, 18259, 18260, 18261, 18262, 18263, 18264, 18265, 18266, 18267, 18268, 18269, 18270, 18271, 18272, 18273, 18274, 18275, 18276, 18277, 18278, 18279, 18280, 18281, 18282, 18283, 18284, 18285, 18286, 18287, 
18288, 18289, 18290, 18291, 18292, 18293, 18294, 18295, 18296, 18297, 18298, 18299, 18300, 18301, 18302, 18303, 18304, 18305, 18306, 18307, 18308, 18309, 18310, 18311, 18312, 18313, 18314, 18315, 18316, 18317, 18318, 18319, 18320, 18321, 18322, 18323, 18324, 18325, 18326, 18327, 18328, 18329, 
151, 152, 153, 154, 155, 156, 13746, 13747, 13748, 13749, 13750, 13751, 13752, 13753, 16033, 16044, 11694, 13363, 13362, 15328, 15329, 15330, 15331, 15332, 15333, 15334, 15335, 13845, 13846, 13847, 13848, 13849, 13850, 13851,
13359, 13358, 15512, 15511, 13361, 13360, 15510, 15509, 15504, 15503, 15506, 15505, 15508, 15507, 14606, 14605, 16712, 
16711, 18336, 18335, 17260, 17259, 16668, 16667, 16956, 16955, 16710, 16709, 17258, 17257, 16666, 16665, 16708, 16707, 
17256, 17255, 16664, 16663, 17362, 17361, 17360, 17359, 17358, 17357, 16756, 16755, 16866, 16865, 17238, 17237, 17062, 
17061, 996, 995, 17194, 17193, 17340, 17339, 16956, 16955, 16954, 16953, 16952, 16951, 16404, 16403, 16402, 16401, 16400, 
16399, 16910, 16909, 16908, 16907, 16906, 16905, 18350, 18349, 18354, 18353, 18358, 18357, 18356, 18355, 18352, 18351, 
13356, 13355, 13351, 13350, 13349, 13348, 13347, 13346, 13353, 13352, 13355, 13354, 19670, 19669, 18360, 18359, 18362, 
18361, 18364, 18363, 4448, 4447, 13852, 13853, 13854, 13855, 13856, 13857, 13858, 13859, 13860, 13861, 13862, 13863, 
13864, 13865, 13866, 13867, 13868, 13869, 13870, 13871, 13872, 13873, 13874, 13875, 13876, 13877, 13878, 13879, 13880, 
13881, 13882, 13883, 13884, 13885, 13886, 13887, 13888, 13889, 13890, 13891, 13892, 13893, 13894, 13895, 13896, 13897, 
13898, 13899, 13900, 13901, 13902, 13903, 13904, 13905, 13906, 13907, 13908, 13909, 13910, 13911, 13912, 13913, 13914, 
13915, 13916, 13917, 13918, 13919, 
13920, 13921, 13922, 13923, 13924, 13925, 13926, 13927, 13928, 13929, 13930, 13931, 13932, 13933, 13934, 13935, 13936, 
13937, 13938, 13939, 13940, 13941, 13942, 13943, 13944, 13945, 13946, 13947, 13948, 13949, 13950, 13951, 13952, 13953, 
13954, 13955, 13956, 13957, 1038, 1037, 1039, 1040, 1041, 1042, 1043, 1044, 1045, 1046, 1047, 1048, 1049, 1050, 1051, 
1053, 1054, 1055, 1056, 1057, 1058, 2422, 13531, 13532, 13533, 13534, 13535, 13536, 13537, 13538, 13539, 13540, 
20072, 19784, 19785, 19786, 19787, 19788, 19789, 19790, 19780, 18786, 15300, 15301, 15302, 15303, 
15773,  15774,  15818, 15888, 15890, 15891, 15924, 15935, 15946, 16001, 16023, 
16034, 16045, 16090, 16126, 16137, 16152, 16184, 16206, 16217, 16258, 16259, 
16260, 16261, 16272, 16293, 16294, 16315, 16316, 16359, 16360, 16381, 16382, 
16403, 16404, 16425, 16426, 16667, 16668, 16689, 16690, 16711, 16712, 16733, 
16734, 16837, 16838, 16839, 16840, 16841, 16842, 16843, 16844, 16909, 16910, 
16955, 16956, 17039, 17040, 17143, 17144, 17145, 17146, 17147, 17148, 17149, 
17150, 17259, 17260, 17361, 17362, 20822, 20823, 20824, 20825, 20826, 20833, 
13734, 13735, 13737, 13738, 13739, 13740, 13741, 13742, 13743, 13744, 
13745, 4716, 4717, 4718, 4719, 4720, 4721, 4722, 4723, 4753, 4754, 4755, 4756, 
4757, 4758, 4759, 4760, 4732, 4733, 4734, 4735, 4736, 4737, 4738, 4739, 4708, 
4709, 4710, 4711, 4712, 4713, 4714, 4715, 14484, 14485, 14486, 11695, 13450, 
18349, 18350, 18351, 18352, 18353, 18354, 18355, 18356, 18357, 18358, 18359, 
18360, 11702, 11703, 11704, 11705, 11706, 11707, 11708, 11709, 15486, 15487, 
15502, 14642, 14645, 14642, 14645, 15433, 15435, 15474, 14641, 15432, 15434, 2570, 2571, 
4856, 4857, 4858, 4859, 4860, 4861, 4862, 4863, 4864, 4865, 4866,  4867, 4868,  4869, 4870, 4871, 4872, 4873, 4874, 4875, 4876, 4877, 4878, 4879, 4880, 4881, 4882, 4883, 4884, 4885, 4886, 4887, 4888, 4889, 4890, 4891, 4892, 4893, 4894, 4895, 4896, 4897, 4898, 4899, 
4900, 4901, 4902, 4903, 4904, 4905, 4906, 4907, 4908, 4909, 4910, 4911, 4912, 4913, 4914, 4915, 4916, 4917, 4918, 4919, 4920, 4921, 4922, 4923, 4924, 4925, 4926, 4927, 4928, 4929, 4930, 4931, 4932, 4933, 4934, 4935, 4936, 4937, 4938, 4939, 4940, 4941, 4942, 4943, 
4944, 4945, 4946, 4947, 4948, 4949, 4950, 4951, 4952, 4953, 4954, 4955, 4956, 4957, 4958, 4959, 4960, 4961, 4962, 4963, 4964, 4965, 4966, 4967, 4968, 4969, 4970, 4971, 4972, 4973, 4974, 4975, 4976, 4977, 4978, 4979, 4980, 4981, 4982, 4983, 4984, 4985, 4986, 4987, 
4988, 4989, 4990, 4991, 4992, 4993, 4994, 4995, 4996, 4997, 4998, 4999, 8850, 8849, 11665, 11664, 11663, 11676, 11675, 11674, 10611, 8842, 8839, 19711, 
8840, 8841, 8842 ]; // Define an array containing item IDs that are not allowed to be spawned

// Function to check if the item is allowed to be spawned here
function isItemAllowed(itemId, itemsNotAllowed) {
    return itemsNotAllowed.indexOf(itemId) === -1; // Check if the item ID is not found in the itemsNotAllowed array
}

if (cmd === "item" && !checkInPVP(player)) {
    var id = java.lang.Integer.parseInt(args[0]);
    var amount = 1;
    
    if (args.length > 1) {
        amount = java.lang.Integer.parseInt(args[1]);
    }
    
    // Check if the player is an admin
    var isAdmin = checkAdmin(player);
    
    // Check if the item is not allowed to be spawned here and the player is not an admin
    if (!isItemAllowed(id, itemsNotAllowed) && !isAdmin) {
        player.sendMessage("You cannot spawn this item.");
        return; // Exit the method as the item is not allowed to be spawned here
    }
    
    if (ItemDefinition.forId(id) !== null) {
        if (!ItemDefinition.forId(id).isStackable()) {
            if (amount > player.getInventory().remaining()) {
                amount = player.getInventory().remaining();
            }
        }
    }
    
    player.getInventory().add(id, amount);
}
 else if(cmd.equals("empty")) {
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
      } else if(cmd.equals("varbit") && checkAdmin(player)) {
        Static.proto.sendCloseInterface(player)
        var id = java.lang.Integer.parseInt(args[0])
        var id2 = java.lang.Integer.parseInt(args[1])
        Static.proto.sendInterfaceVariable(player, id, id2)
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
