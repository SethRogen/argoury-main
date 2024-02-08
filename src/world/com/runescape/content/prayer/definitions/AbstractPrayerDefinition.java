package com.runescape.content.prayer.definitions;

public class AbstractPrayerDefinition {

    protected int id;

    protected float attackMultiplier;
    protected float strengthMultiplier;
    protected float defenceMultiplier;
    protected float magicMultiplier;
    protected float rangedMultiplier;

    protected boolean drainEnemyPrayer = false;
    protected int drainEnemyPrayerRate;

    protected float drainRate;

    protected boolean protectItem = false;

    protected String jsFunction = "";

    protected int configMaskValue;

    protected int requiredLevel;

    protected int overheadId;

    public final int getId() {
        return id;
    }

    public final float getAttackMultiplier() {
        return attackMultiplier;
    }

    public final float getStrengthMultiplier() {
        return strengthMultiplier;
    }

    public final float getDefenceMultiplier() {
        return defenceMultiplier;
    }

    public final float getMagicMultiplier() {
        return magicMultiplier;
    }

    public final float getRangedMultiplier() {
        return rangedMultiplier;
    }

    public final boolean isDrainEnemyPrayer() {
        return drainEnemyPrayer;
    }

    public final int getDrainEnemyPrayerRate() {
        return drainEnemyPrayerRate;
    }

    public final float getDrainrate() {
        return drainRate;
    }

    public final boolean isProtectItem() {
        return protectItem;
    }

    public final String getJsFunction() {
        return jsFunction;
    }

    public final int getConfigMaskValue() {
        return configMaskValue;
    }

    public final int getRequiredLevel() {
        return requiredLevel;
    }

    public final int getPrayerIcon() {
        return overheadId;
    }
}
