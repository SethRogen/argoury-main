package com.runescape.content.skill.free.cooking;

import java.util.Random;

import com.runescape.Static;
import com.runescape.content.handler.ActionHandler;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ItemOnObjectHandler;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.object.GameObject;
import com.runescape.logic.player.Player;



/**
 * @author Seth Rogen
 */


public class Cooking implements ActionHandler, ItemOnObjectHandler {
	
	/**
	 * Enum Struture 
	 * 
	 * RawID, CookedID, BurntID, Level, Experince, OnlyFire
	 */
	
	
    public enum CookingRecipe {
        // RawID, CookedID, BurntID, Level, Experience, OnlyFire
        SHRIMP(317, 315, 7954,60, 30, false , 128, 512),
        ANCHOVIES(321, 319, 323, 1, 30, false, 128, 512),
        SARDINE(327, 325, 369, 1, 40, false, 128, 512),
        HERRING(345, 347, 357, 5, 50, false, 128, 512),
        MACKEREL(353, 355, 357, 10, 60, false, 128, 512);

        private final int rawId;
        private final int cookedId;
        private final int burntId;
        private final int level;
        private final int experience;
        private final boolean onlyFire;
        private final int LowRoll;
        private final int HighRoll;

        CookingRecipe(int rawId, int cookedId, int burntId, int level, int experience, boolean onlyFire, int LowRoll, int HighRoll) {
            this.rawId = rawId;
            this.cookedId = cookedId;
            this.burntId = burntId;
            this.level = level;
            this.experience = experience;
            this.onlyFire = onlyFire;
            this.LowRoll = LowRoll;
            this.HighRoll = HighRoll;
        }

        public int getRawId() {
            return rawId;
        }

        public int getCookedId() {
            return cookedId;
        }

        public int getBurntId() {
            return burntId;
        }

        public int getLevel() {
            return level;
        }

        public int getExperience() {
            return experience;
        }

        public boolean isOnlyFire() {
            return onlyFire;
        }
        
        public int getLowRoll() { 
        	return LowRoll;
        }
        
        public int getHighRoll() { 
        	return HighRoll;
        }
    }
    private CookingRecipe getCookingRecipeForRawId(int rawId) {
        for (CookingRecipe recipe : CookingRecipe.values()) {
            if (recipe.getRawId() == rawId) {
                return recipe;
            }
        }
        return null; // Recipe not found for the given raw ID
    }
    /**
     * Animation IDS - Cooking on fire, 897
     */
	@Override
	public void handleItemOnObject(final Player player, PossesedItem item, int itemIndex, GameObject obj) {
	    int rawId = item.getId(); // Get the ID of the raw item
	    CookingRecipe recipe = getCookingRecipeForRawId(rawId); // Look up the CookingRecipe based on the raw ID
	    final String key = item.getDefinition().name;
	    if (recipe != null) {
	    	if (player.getLevels().COOKING > recipe.getLevel()) { 
	    		player.getSkillDialouge().Open(player, player.getSkillDialouge().COOK, "Choose how many you would like to cook, <br> then click on the item to begin", new int[] { rawId }, player.getInventory().amount(rawId), key, null);
	    	} else { 
	    		Static.proto.sendMessage(player, "You do not have the required level to cook this.");
	    		
	    	}
	    }
	}

	
    public boolean Roll(Player player) {
    	int success = player.getLevels().interpolate(128/*Low Roll*/, 512/*High Roll*/, player.getLevels().COOKING);
		if (success > new Random().nextInt(256)) {
			return true;
		}
        return false;
    }
    
    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        int[][] rawFishIds = new int[CookingRecipe.values().length][2];

        int index = 0;
        for (CookingRecipe recipe : CookingRecipe.values()) {
            rawFishIds[index][0] = recipe.getRawId();
            rawFishIds[index][1] = 2732; // The object ID where the cooking action is performed
            index++;
        }

        system.registerItemOnObjectHandler(rawFishIds, this);
    }

	@Override
	public boolean explicitlyForMembers() {
		// TODO Auto-generated method stub
		return false;
	}

}
