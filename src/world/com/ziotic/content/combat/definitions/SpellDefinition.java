package com.ziotic.content.combat.definitions;

import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;

public class SpellDefinition {

    public static enum ElementType {
        AIR,
        WATER,
        EARTH,
        FIRE,
        SMOKE,
        SHADOW,
        BLOOD,
        ICE,
        MIASMIC,
        MISC;

        public static int intValue(ElementType type) {
            switch (type) {
                case AIR:
                    return 10;
                case WATER:
                    return 20;
                case EARTH:
                    return 30;
                case FIRE:
                    return 40;
                case SMOKE:
                    return 10;
                case SHADOW:
                    return 20;
                case BLOOD:
                    return 30;
                case ICE:
                    return 40;
                case MIASMIC:
                    return 60;
                default:
                    return 0;
            }
        }
    }

    public static enum CombatType {
        STRIKE,
        BOLT,
        BLAST,
        WAVE,
        SURGE,
        CRUMBLE_UNDEAD,
        MAGIC_DART,
        IBAN_BLAST,
        GOD_SPELL,
        RUSH,
        BURST,
        BLITZ,
        BARRAGE,
        BIND,
        VENGEANCE,
        TELEBLOCK;

        public static int getBaseMaxHit(Player player, CombatType combatType, ElementType elementType) {
            switch (combatType) {
                case STRIKE:
                    return 2 * ElementType.intValue(elementType);
                case BOLT:
                    return 80 + ElementType.intValue(elementType);
                case BLAST:
                    return 120 + ElementType.intValue(elementType);
                case WAVE:
                    return 160 + ElementType.intValue(elementType);
                case SURGE:
                    return 200 + (2 * ElementType.intValue(elementType));
                case CRUMBLE_UNDEAD:
                    return 150;
                case MAGIC_DART:
                    return 100 + player.getLevels().getCurrentLevel(Levels.MAGIC);
                case GOD_SPELL:
                    return player.getCombat().getMagic().isCharged() ? 300 : 200;
                case RUSH:
                    return 140 + ElementType.intValue(elementType);
                case BURST:
                    return 180 + ElementType.intValue(elementType);
                case BLITZ:
                    return 220 + ElementType.intValue(elementType);
                case BARRAGE:
                    return 260 + ElementType.intValue(elementType);
                case BIND:
                    return 50 + ElementType.intValue(elementType);
                default:
                    return 0;
            }
        }
    }

    public static enum GroupType {
        COMBAT,
        ENCHANTMENT,
        CURSE,
        ALCHEMY,
        MISC;
    }

    public String name;
    public int buttonId = -1;
    public int interfaceId = -1;
    public int requiredLevel = 0;
    public ElementType elementType;
    public CombatType combatType;
    public GroupType groupType;
    public int startAnim = -1;
    public int startAnimDelay = 0;
    public int startGfx = -1;
    public int startGfxHeight = 0;
    public int startGfxDelay = 0;
    public int projectileId = -1;
    public int startHeight = 0;
    public int middleHeight = 0;
    public int endHeight = 0;
    public int projDelay = 0;
    public int projSpeed = 0;
    public int endGfx = -1;
    public int endGfxHeight = 0;
    public int tX = -1;
    public int tY = -1;
    public int tZ = -1;
    public int trX = -1;
    public int trY = -1;
    public int freezeTime = -1;
    public int poison = -1;
    public int reqStaff = -1;
    public int hpXP = -1;
    public int magicXP = -1;
    public boolean multi = false;
    public int autocastConfig = -1;
    public boolean drainsAttack = false;
    public boolean drainsHP = false;
    public PossesedItem[] requiredRunes;
    public int spellDelay = 6;
    public int attackDelay = 4;
    public double accuracy = 1;
    public double xp;
}
