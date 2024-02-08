package com.runescape.content.combat.definitions;

public class AmmunitionDefinition {

    public static enum Type {
        ARROW, BOLT, THROWING_AXE, THROWING_KNIFE, CHINCHOMPA, OTHER;
    }

    public static enum Material {
        BRONZE, IRON, STEEL, BLACK, MITHRIL, ADAMANT, RUNE, DRAGON, OTHER;
    }

    public static enum Poison {
        POISON, POISON_PLUS, POISON_PP, NONE;
    }

    public static enum BoltTip {
        OPAL, SAPPHIRE, JADE, PEARL, EMERALD, RED_TOPAZ, RUBY, DIAMOND, DRAGON_STONE, ONYX, NONE;
    }

    public int id;
    public boolean inHand;
    public int[] requiredBows;
    public int requiredLevel;
    public int animationId;
    public int animationDelay;
    public int graphicsId;
    public int graphicsDelay;
    public int graphicsHeight;
    public int projectileId;
    public int projectileDelay;
    public int projectileSpeed;
    public int startHeight;
    public int middleHeight;
    public int endHeight;
    public String specialName;
    public Type ammunitionType;
    public Material ammunitionMaterial;
    public Poison ammunitionPoison;
    public BoltTip boltTip;
    public int speed;
    public boolean enchanted;
    public int enchantedGraphics;
    public int enchantedGraphicsHeight;

}
