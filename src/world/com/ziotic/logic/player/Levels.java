package com.ziotic.logic.player;

import com.ziotic.Static;
import com.ziotic.logic.item.ItemContainer;
import com.ziotic.logic.item.ItemDefinition;
import com.ziotic.logic.item.PossesedItem;

import java.util.Map;

/**
 * @author Lazaro
 */
public class Levels {
    public static final int SKILL_COUNT = 25;
    public static final int ATTACK = 0, DEFENCE = 1, STRENGTH = 2, CONSTITUTION = 3, RANGE = 4, PRAYER = 5, MAGIC = 6, COOKING = 7, WOODCUTTING = 8, FLETCHING = 9, FISHING = 10, FIREMAKING = 11, CRAFTING = 12, SMITHING = 13, MINING = 14, HERBLORE = 15, AGILITY = 16, THIEVING = 17, SLAYER = 18, FARMING = 19, RUNECRAFTING = 20, CONSTRUCTION = 21, HUNTER = 22, SUMMONING = 23, DUNGEONNEERING = 24;
    public static final String[] SKILL_NAME = {"Attack", "Defence", "Strength", "Hitpoints", "Range", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecrafting", "Construction", "Hunter", "Summoning", "Dungeonneering"};

    private Player player;

    private short combatLevel;
    private double[] currentLevels = new double[SKILL_COUNT]; // The current levels
    private double[] experiences = new double[SKILL_COUNT]; // Experience
    private byte[] levels = new byte[SKILL_COUNT]; // The levels for experience

    private double xpGained = 0;

    public Levels(Player player) {
        this.player = player;
    }

    /**
     * Adds experience to the specified skill.
     *
     * @param skillId The skill to add experience to.
     * @param amount  The amount of experience.
     */
    public void addXP(int skillId, double amount) {
        if (skillId >= SKILL_COUNT || skillId < 0) {
            return;
        }
        byte oldLevel = levels[skillId]; // Save the old level
        experiences[skillId] += amount; // Add the experience
        byte newLevel = levels[skillId] = (byte) levelForXP(skillId); // Calculate the new level
        if (oldLevel < newLevel) { // The level changed.
            if (skillId == 3) {
                updateHP(newLevel - oldLevel); // Update the HP.
            }
            currentLevels[skillId] += (newLevel - oldLevel); // Add up the
            // levels.
            if (skillId <= 6 || skillId == 23) { 
                calculateCombat();
            }
            
            player.doGraphics(199);
            Static.proto.sendMessage(player, "You've just advanced a " + SKILL_NAME[skillId] + " level! You have reached level " + newLevel + ".");
            // TODO Level up graphics, etc.
        }
        xpGained += amount;
        Static.proto.sendLevel(player, skillId);
    }

    public void calculateCombat() {
        int combatBase = (int) Math.ceil((levels[DEFENCE] + levels[CONSTITUTION]) * .25 + ((levels[PRAYER] % 2 == 0 ? levels[PRAYER] : levels[PRAYER] - 1) + (levels[SUMMONING] % 2 == 0 ? levels[SUMMONING] : levels[SUMMONING] - 1)) * .125);
        int meleeBase = ((levels[ATTACK] * 130) + (levels[STRENGTH] * 130)) / 400;
        int rangedBase = (levels[RANGE] % 2 == 0 ? levels[RANGE] * 195 : (levels[RANGE] * 195) - 65) / 400;
        int magicBase = (levels[MAGIC] % 2 == 0 ? levels[MAGIC] * 195 : (levels[MAGIC] * 195) - 65) / 400;
        short oldCombat = combatLevel;
        combatLevel = (short) (Math.max(Math.max(meleeBase, rangedBase), magicBase) + combatBase);
        if (!player.isOnLogin() && oldCombat >= 3 && oldCombat != combatLevel)
            player.getAppearance().refresh();
    }

    public void calculateLevels() {
        for (int i = 0; i < SKILL_COUNT; i++) {
            levels[i] = (byte) levelForXP(i);
        }
    }

    public short getCombatLevel() {
        return combatLevel;
    }

    /**
     * Gets the current level of a skill.
     * <p/>
     * Note: This is not the level based on experience!
     *
     * @param skillId The skill id.
     * @return The current level.
     */
    public int getCurrentLevel(int skillId) {
        return (int) currentLevels[skillId];
    }

    public double getCurrentPrayer() {
        return currentLevels[PRAYER];
    }

    /**
     * Gets the experience for the skill specified.
     *
     * @param skillId The skill id.
     * @return The experience.
     */
    public double getExperience(int skillId) {
        return experiences[skillId];
    }

    /**
     * Gets the actual level based on the experience.
     *
     * @param skillId The skill id.
     * @return The current level.
     */
    public int getLevel(int skillId) {
        return levels[skillId];
    }

    public double getXPGained() {
        return xpGained;
    }

    public void setXPGained(int xpGained) {
        this.xpGained = xpGained;
    }

    /**
     * Calculates the actual level for the amount of experience.
     *
     * @param skillId The skill id to calculate for.
     * @return The actual level based on the experience.
     */
    private int levelForXP(int skillId) {
        int exp = (int) experiences[skillId];
        int points = 0;
        int output = 0;
        for (byte lvl = 1; lvl <= maxLevelForSkill(skillId); lvl++) {
            points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
            output = (int) Math.floor(points / 4);
            if (exp < output) {
                return lvl;
            }
        }
        return maxLevelForSkill(skillId);
    }

    private int maxLevelForSkill(int skillId) {
        if (skillId == DUNGEONNEERING)
            return 120;
        return 99;
    }

    public void resetXPGained() {
        xpGained = 0;
    }

    public void restore() {
        for (int i = 0; i < SKILL_COUNT; i++) {
            currentLevels[i] = levels[i];
            Static.proto.sendLevel(player, i);
        }
    }

    /**
     * @param skillId The skill id.
     * @param level   The level.
     */
    public void setCurrentLevel(int skillId, int level) {
        if (skillId == PRAYER && level <= 0) {
            level = 0;
            if (!player.isOnLogin()) {
                // TODO player.getPrayer().resetPrayer();
                Static.proto.sendMessage(player, "You have run out of Prayer points; you can recharge at an altar.");
            }
        }
        double oldLevel = currentLevels[skillId];
        currentLevels[skillId] = (byte) level;
        if (!player.isOnLogin() && skillId == CONSTITUTION) {
            updateHP((int) (level - oldLevel));
        }
    }

    public void setCurrentPrayer(double level) {
        currentLevels[PRAYER] = level;
    }

    public void removePrayer(double level) {
        currentLevels[PRAYER] -= level;
    }

    /**
     * For player loading ONLY!
     *
     * @param skillId    The skill id.
     * @param experience The experience.
     */
    public void setXP(int skillId, double experience) {
        experiences[skillId] = experience;
    }

    public void setLevel(int skillId, int level) {
        levels[skillId] = (byte) level;
    }

    public void refresh() {
        for (int i = 0; i < SKILL_COUNT; i++) {
            Static.proto.sendLevel(player, i);
        }
    }

    /**
     * Updates the client with the new HP.
     *
     * @param diffHp The difference in HP.
     */
    private void updateHP(int diffHp) {
        player.setHP(player.getHP() + (diffHp * 10));
    }

    //XXX Move this somewhere appropriate
    public boolean meetsRequirements(ItemDefinition def, boolean sendMessage) {
        Map<Integer, Integer> req = def.levelRequirements;
        if (req != null) {
            for (int skill : req.keySet()) {
                if (skill < 0 || skill > 24) {
                    continue;
                }
                int level = req.get(skill);
                if (level < 0 || level > 120) {
                    continue;
                }
                int actualLevel = levelForXP(skill);
                if (actualLevel < level) {
                    if (sendMessage) {
                        String prefix = skill == 0 || skill == 16 ? "an " : "a ";
                        player.sendMessage("You need to have " + prefix + Levels.SKILL_NAME[skill] + " level of " + level + " to wear this item.");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public void doLevelCommand(int id, int level) {
        if (id > Levels.SKILL_COUNT || id < 0) {
            Static.proto.sendMessage(player, "Invalid skill id set!");
            return;
        }
        if (level > 99 || level <= 0 || id == 3 && level < 10) {
            Static.proto.sendMessage(player, "Invalid level amount set!");
            return;
        }

        player.getLevels().setXP(id, xpForLevel(level));
        player.getLevels().setLevel(id, level);
        player.getLevels().setCurrentLevel(id, level);
        Static.proto.sendLevel(player, id);
        player.getLevels().calculateCombat();

        checkEquipmentForRequirements(true);
    }

    public void checkEquipmentForRequirements(boolean setLevel) {
        ItemContainer equipment = player.getEquipment();
        for (PossesedItem i : equipment.array()) {
            if (i != null) {
                ItemDefinition def = i.getDefinition();
                if (def != null) {
                    if (!meetsRequirements(def, false)) {
                        if (setLevel) {
                            player.sendMessage("Your new set level did not meet the level requirements for your " + def.name + ".");
                        } else
                            player.sendMessage("You are not meeting the level requirements for your " + def.name + ".");
                        if (!Static.<Boolean>callScript("equipment.forceRemoveEquipment", player, i.getId())) {
                            player.sendMessage("There was no inventory space and the item was dropped on the ground.");
                            Static.world.getGroundItemManager().add(i.getId(), i.getAmount(), player.getLocation(), player.getProtocolName(), false);
                        }
                    }
                }
            }
        }
    }

    public int xpForLevel(int level) {
        double points = 0, output = 0;
        for (int lvl = 1; lvl <= level; lvl++) {
            points += java.lang.Math.floor(lvl + 300.0 * java.lang.Math.pow(2.0, lvl / 7.0));
            if (lvl >= level) {
                return (int) output;
            }
            output = Math.floor(points / 4);
        }
        return 0;
    }
}
