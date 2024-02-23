package com.runescape.content.skill.free.cooking;

/**
 * @author Seth Rogen
 */

public enum CookingRecipe {
    // RawID, CookedID, BurntID, Level, Experience, OnlyFire
	
    SHRIMP(317, 315, 7954, 1, 30, false , 128, 512),
    
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
