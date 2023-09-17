importPackage(com.ziotic.logic.item)
importPackage(com.ziotic.logic.player)

function wieldEquipment(player, id, index) {
    var item = player.getInventory().get(index)
    if(item == null || item.getId() != id) {
        return
    }
    item = item.clone()
    var def = item.getDefinition()
    if (def == null) {
    	return
    }
    var equipDef = def.getEquipmentDefinition()
    if(equipDef == null) { // TODO This is going to kill us
        Static.proto.sendMessage(player, "You can\'t wear that.")
        return
    }
	var meetsReqs = player.getLevels().meetsRequirements(def, true);
	if (!meetsReqs) {
		return
	}
    if(equipDef.getEquipmentType() == EquipmentDefinition.EquipmentType.WEAPON_2H) {
    	var curWep = player.getEquipment().get(EquipmentDefinition.SLOT_WEAPON)
    	var curShield = player.getEquipment().get(EquipmentDefinition.SLOT_SHIELD)
    	if(curWep != null && curShield != null) { //Need to put shield & wep in inven, and equip 2h
    		if(player.getInventory().remaining() < 1) {
                Static.proto.sendMessage(player, "Not enough inventory space.")
    			return
    		} else {
    			player.getInventory().remove(item, index, false)
    			player.getInventory().add(curWep, index, true)
    			player.getInventory().add(curShield)
    			player.getEquipment().remove(curWep)
    			player.getEquipment().remove(curShield)
    			player.getEquipment().add(item, equipDef.getEquipmentType().getSlot())
        	return
    		}
    	} else if(curWep != null && curShield == null) {
    		player.getInventory().remove(item, index, false)
    		player.getInventory().add(curWep, index, true)
    		player.getEquipment().remove(curWep)
    		player.getEquipment().add(item, equipDef.getEquipmentType().getSlot())
        	return
    	} else if(curWep == null && curShield != null) {
    		player.getInventory().remove(item, index, false)
    		player.getInventory().add(curShield, index, true)
    		player.getEquipment().remove(curShield)
    		player.getEquipment().add(item, equipDef.getEquipmentType().getSlot())
        	return
    	}
    }
    
    if(equipDef.getEquipmentType() == EquipmentDefinition.EquipmentType.SHIELD) {
    	var curWep = player.getEquipment().get(EquipmentDefinition.SLOT_WEAPON)
    	if(curWep != null && curWep.getDefinition().getEquipmentDefinition().getEquipmentType() == EquipmentDefinition.EquipmentType.WEAPON_2H) {
    		player.getInventory().remove(item, index, false)
    		player.getInventory().add(curWep, index, true)
    		player.getEquipment().remove(curWep)
    		player.getEquipment().add(item, equipDef.getEquipmentType().getSlot())
    		return
    	}
    }
    var curItem = player.getEquipment().get(equipDef.getEquipmentType().getSlot())
    player.getInventory().remove(item, index, curItem == null)

    if(curItem != null && !(id == curItem.getId() && def.isStackable())) {
        player.getInventory().add(curItem, index)
        player.getEquipment().remove(curItem, equipDef.getEquipmentType().getSlot(), false)
    }
    player.getEquipment().add(item, equipDef.getEquipmentType().getSlot(), true)

}

function removeEquipment(player, id) {
    var def = ItemDefinition.forId(id)
    var equipDef = def.getEquipmentDefinition()
    
    var item = player.getEquipment().get(equipDef.getEquipmentType().getSlot())
    if(item == null || id != item.getId()) {
        return
    }

    if(player.getInventory().add(item)) {
        player.getEquipment().remove(item, equipDef.getEquipmentType().getSlot())
    }
}

function forceRemoveEquipment(player, id) {
    var def = ItemDefinition.forId(id)
    var equipDef = def.getEquipmentDefinition()
    
    var item = player.getEquipment().get(equipDef.getEquipmentType().getSlot())
    if(item == null || id != item.getId()) {
        return
    }
    
    player.getEquipment().remove(item, equipDef.getEquipmentType().getSlot())    
    return player.getInventory().add(item)
}