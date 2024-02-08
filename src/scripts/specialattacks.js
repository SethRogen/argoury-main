importPackage(com.runescape)
importPackage(com.runescape.content.combat)
importPackage(com.runescape.content.prayer)
importPackage(com.runescape.content.combat.ticks)
importPackage(com.runescape.engine.tick)
importPackage(com.runescape.logic)
importPackage(java.lang)

function handleSpecial(player, victim, combat, weapon, actiontype, defAnim) {
	try {
		var reqSpecAmt = getReqSpecialAmount(weapon.getId())
		if(reqSpecAmt > combat.getSpecialEnergy().getAmount()) {
			Static.proto.sendMessage(player, "Not enough special energy left.")
			return false
		}
	
		combat.getSpecialEnergy().decrease(reqSpecAmt)
		combat.getSpecialEnergy().update()
		var distance = player.getLocation().distance(victim.getLocation())
		switch(weapon.getId()) {
			case 14484:
		        	combat.executeAnimation(10961, 0, true, false)
		        	combat.executeGraphics(1950, 100 << 16, 0)	
		        	
		        	var hit
		        	var accuracy = 2.2
		        	var strength = 1.0
		        	var splatDelay = 20
		        	for (var i = 0; i < 4; i++) {
		        		accuracy = accuracy + 0.1
		        		switch (i) {
		        		case 1:
		        			splatDelay += 10
		        			break
		        		case 2:
		        			splatDelay += 5
		        			break
		        		case 3:
		        			splatDelay += 5
		        			break
		        		}
		        			
		        		hit = combat.hit(victim, actiontype, true, strength, accuracy, null, 1, null, [splatDelay], [splatDelay])
		        		var hasHit = false
		        		if (hit != 0)
		        			hasHit = true;
		        		if (hasHit) {
			        		switch (i) {
				        		case 0:
				        			doDragonClawsSpec(combat, victim, actiontype, 3, hit)
				        			break
				        		case 1:
				        			doDragonClawsSpec(combat, victim, actiontype, 2, hit)
				        			break
				        		case 2:
				        			doDragonClawsSpec(combat, victim, actiontype, 1, hit)
				        			break
			        		}
			        		break
		        		}
		        		splatDelay += 5
		        	}
	                victim.getCombat().executeAnimation(defAnim, 0, false, false)
				return true
	        	case 1215:
	        	case 1231:
	        	case 5680:
	        	case 5698:
	        	case 13465:
	        	case 13467:
	        	case 13976:
	        	case 13978: //Dragon dagger
		        	combat.executeAnimation(1062, 0, true, false)
		        	combat.executeGraphics(252, 100 << 16, 55)	
					combat.hit(victim, actiontype, true, 1.1, 1.3, null, 2, null, [20, 25], [0])
					victim.getCombat().executeAnimation(defAnim, 0, false, false)
					return true
	            case 11694: //Ags
	            	combat.executeAnimation(7074, 0, true, false)
	            	combat.executeGraphics(1222, 0, 55)
	            	combat.hit(victim, actiontype, true, 1.25, 1.3, null, 1, null, [20], [0])
	            	victim.getCombat().executeAnimation(defAnim, 0, false, false)
	                return true
	            case 861: // magic shortbow
	            	combat.executeAnimation(1074, 0, true, false)
	            	combat.executeGraphics(256, 0, 96)
	            	combat.executeGraphics(256, 30, 96)
	            	combat.hit(victim, actiontype, true, 1, 0.9, null, 2, null, [20 + 5 + distance * 5, 50 + 5 + distance * 5], [10, 25])
	            	Static.world.sendProjectile(player, victim, 249, player.getLocation(), victim.getLocation(), 44, 41, 5, 20, 15, 0, player.getSize())
	            	Static.world.sendProjectile(player, victim, 249, player.getLocation(), victim.getLocation(), 44, 41, 5, 50, 15, 0, player.getSize())
	            	return true
	            case 4587:
	            	combat.executeAnimation(12031, 0, true, false)
	            	combat.executeGraphics(2118, 0, 55)
	            	var damage = combat.hit(victim, actiontype, true, 1, 1.2, null, 1, null, [20], [0])
	            	if (damage > 0) {
	            		PrayerManager.hitByDragonScimitar(victim)
	            	}
	            	return true
		}
	
		return false
	} catch (err) {
		err.printStackTrace()
		return false
	}
}

function getReqSpecialAmount(id) {
    switch(id) {
        case 11694:
        case 14484:
            return 50
        case 1215:
        case 1231:
        case 5680:
        case 5698:
        case 13465:
        case 13467:
        case 13976:
            return 25
        case 11235:
            return 65
        case 861:
        	return 55
        case 4587:
        	return 55
    }
    return 50
}

function doDragonClawsSpec(combat, victim, actiontype, hitAmount, damage) {
	var hit2
	var hit3
	var hit4
	switch(hitAmount) {
	case 3:
		hit2 = damage / 2
		combat.hit(victim, actiontype, true, 1.0, 1.0, null, 1, [hit2], [30], [30])
		hit3 = World.getRandom(hit2)
		hit4 = hit2 - hit3
		combat.hit(victim, actiontype, true, 1.0, 1.0, null, 1, [hit3], [35], [35])
		combat.hit(victim, actiontype, true, 1.0, 1.0, null, 1, [hit4], [40], [40])
		break
	case 2:
		hit3 = damage / 2
		hit4 = damage / 2
		combat.hit(victim, actiontype, true, 1.0, 1.0, null, 1, [hit3], [35], [35])
		combat.hit(victim, actiontype, true, 1.0, 1.0, null, 1, [hit4], [40], [40])
		break
	case 1:
		combat.hit(victim, actiontype, true, 1.5, 2.3, null, 1, null, [40], [40])
		break
	}
}