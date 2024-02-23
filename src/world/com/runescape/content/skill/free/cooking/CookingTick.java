package com.runescape.content.skill.free.cooking;

import java.util.Random;


import com.runescape.content.skill.ProducingTick;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.player.Player;

/**
 * @author Seth Rogen
 * Cooking Tick Class | execute produce tick here.
 */
public class CookingTick extends ProducingTick  {
	
    private static final String[] MESSAGES = new String[] {
            "You have run out of ingredients!",
            "You successfully cook the ",
            "You accidentally burn the ",
            "You do not have the required level to cook this!"
    };

	public CookingTick(Player player) {
		super(player);
	}

	@Override
	public int getInterval() {
		return 2;
	}

	@Override
	public void stopped(boolean forceResetMasks) {
	}

	@Override
	public int getAnimationDelay() {
		return 2;
	}

	@Override
	public PossesedItem getFailItem() {
	    for (CookingRecipe recipe : CookingRecipe.values()) {
	            return new PossesedItem(recipe.getBurntId());
	    }
	    return null;
	}

	@Override
	public PossesedItem produceItem() {
	    for (CookingRecipe recipe : CookingRecipe.values()) {
	            return new PossesedItem(recipe.getCookedId());
	    }
	    return null;
	}
	
	@Override
	public PossesedItem getRequiredItems() {
	    for (CookingRecipe recipe : CookingRecipe.values()) {
	    	System.out.println("Item Used = " + recipe.getRawId());
	        return new PossesedItem(recipe.getRawId());
	    }
	    return null;
	}


	@Override
	public int getRequiredLevel() {
	    for (CookingRecipe recipe : CookingRecipe.values()) {
	            return recipe.getLevel();
	    }
	    return 0;
	}

	@Override
	public int getSkill() {
		return player.getLevels().COOKING;
	}

	@Override
	public double getExperience() {
	    for (CookingRecipe recipe : CookingRecipe.values()) {
	            return recipe.getExperience();
	    }
	    return -1;
	}

	@Override
	public String getMessage(int type) {
		
		String message = MESSAGES[type];
		switch (type) {
		case 1:
		case 2:
			for (CookingRecipe recipe : CookingRecipe.values()) {
				message += produceItem().getDefinition().name.toLowerCase();
			}
			break;
		}
		return message;
	}

	@Override
	public int getAnimation() {
		return 897;
	}

	@Override
	public int getGraphic() {
		return 0;
	}

	@Override
	public boolean isSuccessfull() {
		for (CookingRecipe recipe : CookingRecipe.values()) {
		int success = player.getLevels().interpolate(recipe.getLowRoll(), recipe.getHighRoll(), player.getLevels().COOKING);
		if (success > new Random().nextInt(256)) {
			return true;
			}
		}
        return false;
	}

	
}
