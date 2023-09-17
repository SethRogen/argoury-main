package com.ziotic.logic.npc.summoning.impl;

import com.ziotic.Static;
import com.ziotic.logic.npc.summoning.Familiar;
import com.ziotic.logic.npc.summoning.FamiliarSpecial;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;

/**
 * 
 * @author 'Mystic Flow
 * @author Maxi
 */
public class WolperTinger extends Familiar {

	public WolperTinger(Player owner) {
		super(owner);
	}

	@Override
	public int getMinutes() {
		return 62;
	}

	@Override
	public String getSpecialAttackDescription() {
		return "Gives you a +7 Magic boost";
	}

	@Override
	protected boolean performSpecial(FamiliarSpecial type, Object context) {
		int newLevel = owner.getLevels().getCurrentLevel(Levels.MAGIC) + 7;
		if (newLevel > owner.getLevels().getLevel(Levels.MAGIC) + 7)
			newLevel = owner.getLevels().getLevel(Levels.MAGIC) + 7;
		owner.doGraphics(2011);
		owner.getLevels().setCurrentLevel(Levels.MAGIC, newLevel);
		Static.proto.sendLevel(owner, Levels.MAGIC);
		return true;
	}

	@Override
	public int specialCost() {
		return 20;
	}
	
	@Override
	public FamiliarSpecial specialType() {
		return FamiliarSpecial.CLICK;
	}

}
