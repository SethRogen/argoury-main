package com.runescape.content.combat.definitions;

public class BowDefinition {

    public static enum Type {
        SHORT_BOW, LONG_BOW, CROSS_BOW, OTHER;
    }

    public static enum Material {
        NORMAL, OAK, WILLOW, MAPLE, YEW, MAGIC, RUNE, OTHER;
    }

    public int id;
    public int animationId;
    public int animationDelay;
    public String specialName;
    public Type bowType;
    public Material bowMaterial;
    public int speed;

}
