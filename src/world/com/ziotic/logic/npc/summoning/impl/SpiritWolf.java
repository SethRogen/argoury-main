package com.ziotic.logic.npc.summoning.impl;

import com.ziotic.logic.npc.summoning.Familiar;
import com.ziotic.logic.npc.summoning.FamiliarSpecial;
import com.ziotic.logic.player.Player;

/**
 * @author 'Mystic Flow
 */
public class SpiritWolf extends Familiar {

    public SpiritWolf(Player owner) {
        super(owner);
    }

    @Override
    public String getSpecialAttackDescription() {
        return "Causes NPC foes to flee";
    }

    @Override
    public int getMinutes() {
        return 6;
    }

    @Override
    public int specialCost() {
        return 3;
    }

	@Override
	protected boolean performSpecial(FamiliarSpecial type, Object context) {
		return false;
	}

	@Override
	public FamiliarSpecial specialType() {
		return FamiliarSpecial.ENTITY_TARGET;
	}

}
