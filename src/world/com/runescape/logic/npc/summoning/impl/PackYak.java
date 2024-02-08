package com.runescape.logic.npc.summoning.impl;

import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.npc.summoning.BeastOfBurden;
import com.runescape.logic.npc.summoning.FamiliarSpecial;
import com.runescape.logic.player.Player;

/**
 * 
 * @author 'Mystic Flow
 */
public class PackYak extends BeastOfBurden {

	public PackYak(Player owner) {
		super(owner);
	}

	@Override
	public int getMinutes() {
		return 58;
	}

	@Override
	public String getSpecialAttackDescription() {
		return "Use special move on an item in your inventory to send it to your bank";
	}



	@Override
	public int specialCost() {
		return 12;
	}

	@Override
	public int maxBurdenSlots() {
		return 30;
	}

	@Override
	protected boolean performSpecial(FamiliarSpecial type, Object context) {
		PossesedItem toBank = owner.getInventory().get((Integer) context);
		if (toBank != null) {
			boolean ok = owner.getBank().add(toBank);
			if (ok) {
				owner.sendMessage("The pack yak has sent an item to your bank.");
				owner.getInventory().remove(toBank, (Integer) context);
			}
			return ok;
		}
		owner.sendMessage("There is no item there!");
		return false;
	}

	@Override
	public FamiliarSpecial specialType() {
		return FamiliarSpecial.ITEM_TARGET;
	}

}
