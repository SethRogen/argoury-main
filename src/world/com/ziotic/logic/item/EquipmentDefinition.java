/**
 *
 */
package com.ziotic.logic.item;

import com.ziotic.Static;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * An items equipment definition.
 *
 * @author Michael
 * @author Sir Sean
 */
public class EquipmentDefinition {

    /**
     * Logger instance.
     */
    private static final Logger logger = Logging.log();

    /**
     * The <code>EquipmentDefinition</code> map.
     */
    private static Map<Integer, EquipmentDefinition> definitions;

    /**
     * The default item animations.
     */
    public static final EquipmentAnimations DEFAULT_ANIMATIONS = new EquipmentAnimations(new int[]{422, 423, 422, 422}, 424);

    /**
     * Gets a definition for the specified id.
     *
     * @param id The id.
     * @return The definition.
     */
    public static EquipmentDefinition forId(int id) {
        return definitions.get(id);
    }

    /**
     * Loads the equipment definitions.
     *
     * @throws IOException           if an I/O error occurs.
     * @throws IllegalStateException if the definitions have been loaded already.
     */
    @SuppressWarnings("unchecked")
    public static void load() throws IOException {
        if (definitions != null) {
            throw new IllegalStateException("Equipment definitions already loaded.");
        }
        try {
            /**
             * Load equipment definitions.
             */
            definitions = (Map<Integer, EquipmentDefinition>) Static.xml.readObject(Static.parseString("%WORK_DIR%/world/itemData/equipmentdefs.xml"));
            if (definitions != null) {
                logger.info("Loaded " + definitions.size() + " equipment definitions");
            } else {
                logger.error("Equipment definitions not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The items equipment animations.
     */
    private EquipmentAnimations equipmentAnimations;

    /**
     * The items weapon styles based on attack type (ie salamander having mage, melee & range based on combat style).
     */
    private WeaponStyles[] weaponStyles;

    /**
     * The type of equipment this item is.
     */
    private EquipmentType equipmentType;

    /**
     * The poison this equipment has.
     */
    private PoisonType poisonType;

    /**
     * The speed of this weapon in cycles.
     */
    private int speed;

    /**
     * The items bonuses.
     */
    private double[] bonuses;

    /**
     * The special flag.
     */
    private boolean special;

    public EquipmentDefinition(EquipmentAnimations equipmentAnimations, WeaponStyles[] weaponStyles, EquipmentType equipmentType, PoisonType poisonType, int speed, double[] bonuses, boolean special) {
        if (equipmentAnimations != null) {
            this.equipmentAnimations = equipmentAnimations;
        } else {
            // Disabled to make definition XML smaller.
            // this.equipmentAnimations = DEFAULT_ANIMATIONS;
        }
        this.weaponStyles = weaponStyles;
        this.equipmentType = equipmentType;
        this.poisonType = poisonType;
        this.speed = speed;
        this.bonuses = bonuses;
        this.special = special;
    }

    /**
     * @return the equipmentAnimations
     */
    public EquipmentAnimations getEquipmentAnimations() {
        return equipmentAnimations;
    }

    /**
     * @return the weaponStyles
     */
    public WeaponStyles[] getWeaponStyles() {
        return weaponStyles;
    }

    /**
     * @return the equipmentType
     */
    public EquipmentType getEquipmentType() {
        return equipmentType;
    }

    /**
     * @return the poisonType
     */
    public PoisonType getPoisonType() {
        return poisonType;
    }

    /**
     * @return the speed
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * @return the bonuses
     */
    public double[] getBonuses() {
        return bonuses;
    }

    /**
     * @return the special
     */
    public boolean hasSpecial() {
        return special;
    }

    /**
     * The items equipment animations.
     *
     * @author Michael
     */
    public static class EquipmentAnimations {

        /**
         * The attack animation array, based on what combat style you are using.
         */
        public int[] attackAnimations;

        /**
         * The defend animation of this item.
         */
        public int defendAnimation;

        public EquipmentAnimations(int[] attackAnimations, int defendAnimation) {
            this.attackAnimations = attackAnimations;
            this.defendAnimation = defendAnimation;
        }

        /**
         * @return the attackAnimations
         */
        public int[] getAttackAnimations() {
            return attackAnimations;
        }

        /**
         * @return the defendAnimation
         */
        public int getDefendAnimation() {
            return defendAnimation;
        }
    }

    /**
     * The items weapon styles, if it is a weapon.
     *
     * @author Michael
     */
    public static enum WeaponStyles {

        /**
         * Range type weapons.
         */
        RANGED,

        /**
         * Magic type weapons.
         */
        MAGIC,

        /**
         * Melee type weapons.
         */
        MELEE,

        /**
         * Weapon has a special attack.
         */
        SPECIAL;
    }

    /**
     * Equipment type enum.
     *
     * @author Lothy
     * @author Miss Silabsoft
     */
    public static enum EquipmentType {
        /**
         * Item is a cape.
         */
        CAPE("Cape", SLOT_CAPE),

        /**
         * Item is a pair of boots.
         */
        BOOTS("Boots", SLOT_BOOTS),

        /**
         * Item is a pair of gloves.
         */
        GLOVES("Gloves", SLOT_GLOVES),

        /**
         * Item is a shield.
         */
        SHIELD("Shield", SLOT_SHIELD),

        /**
         * Item is a hat.
         */
        HAT("Hat", SLOT_HELM),

        /**
         * Item is an amulet.
         */
        AMULET("Amulet", SLOT_AMULET),

        /**
         * Item is a set of arrows.
         */
        ARROWS("Arrows", SLOT_ARROWS),

        /**
         * Item is a ring.
         */
        RING("Ring", SLOT_RING),

        /**
         * Item is a body with no sleeves.
         */
        BODY("Body", SLOT_CHEST),

        /**
         * Item is a pair of legs.
         */
        LEGS("Legs", SLOT_BOTTOMS),

        /**
         * Item is a body with sleeves.
         */
        PLATEBODY("Platebody", SLOT_CHEST),

        /**
         * Item covers over hair.
         */
        FULL_HELM("Full helm", SLOT_HELM),

        /**
         * Item covers over head fully
         */
        FULL_MASK("Full mask", SLOT_HELM),

        /**
         * Item is a weapon
         */
        WEAPON("Weapon", SLOT_WEAPON),

        /**
         * Item is a weapon 2 handed
         */
        WEAPON_2H("Two-handed weapon", SLOT_WEAPON);

        /**
         * The description.
         */
        private String description;

        /**
         * The slot.
         */
        private int slot;

        /**
         * Creates the equipment type.
         *
         * @param description The description.
         * @param slot        The slot.
         */
        private EquipmentType(String description, int slot) {
            this.description = description;
            this.slot = slot;
        }

        /**
         * Gets the description.
         *
         * @return The description.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Gets the slot.
         *
         * @return The slot.
         */
        public int getSlot() {
            return slot;
        }

    }

    /**
     * The type of poison on this equipment.
     *
     * @author Michael
     */
    public static enum PoisonType {

        /**
         * Normal poison. (4, 2)
         */
        POISON(4, 2),

        /**
         * Extra poison. (5, 3)
         */
        EXTRA_POISON(5, 3),

        /**
         * Super poison. (6, 4)
         */
        SUPER_POISON(6, 4);

        /**
         * The melee damage amount.
         */
        private int meleeDamage;

        /**
         * The range damage amount.
         */
        private int rangeDamage;

        private PoisonType(int meleeDamage, int rangeDamage) {
            this.meleeDamage = meleeDamage;
            this.rangeDamage = rangeDamage;
        }

        /**
         * @return the melee damage amount.
         */
        public int getMeleeDamage() {
            return meleeDamage;
        }

        /**
         * @return the range damage amount.
         */
        public int getRangeDamage() {
            return rangeDamage;
        }
    }

    /**
     * The helmet slot.
     */
    public static final int SLOT_HELM = 0;

    /**
     * The cape slot.
     */
    public static final int SLOT_CAPE = 1;

    /**
     * The amulet slot.
     */
    public static final int SLOT_AMULET = 2;

    /**
     * The weapon slot.
     */
    public static final int SLOT_WEAPON = 3;

    /**
     * The chest slot.
     */
    public static final int SLOT_CHEST = 4;

    /**
     * The shield slot.
     */
    public static final int SLOT_SHIELD = 5;

    /**
     * The bottoms slot.
     */
    public static final int SLOT_BOTTOMS = 7;

    /**
     * The gloves slot.
     */
    public static final int SLOT_GLOVES = 9;

    /**
     * The boots slot.
     */
    public static final int SLOT_BOOTS = 10;

    /**
     * The rings slot.
     */
    public static final int SLOT_RING = 12;

    /**
     * The arrows slot.
     */
    public static final int SLOT_ARROWS = 13;

    public static final String[] BONUS_NAMES = new String[]{"Stab", "Slash", "Crush", "Magic", "Ranged", "Stab", "Slash", "Crush", "Magic", "Ranged",
            "Summoning", "Absorb Melee", "Absorb Magic", "Absorb Ranged", "Strength", "Ranged Strength", "Prayer", "Magic Damage"};

    public static class Bonuses {
        public static final int OFFENSIVE_STAB = 0;
        public static final int OFFENSIVE_SLASH = 1;
        public static final int OFFENSIVE_CRUSH = 2;
        public static final int OFFENSIVE_MAGIC = 3;
        public static final int OFFENSIVE_RANGED = 4;
        public static final int DEFENSIVE_STAB = 5;
        public static final int DEFENSIVE_SLASH = 6;
        public static final int DEFENSIVE_CRUSH = 7;
        public static final int DEFENSIVE_MAGIC = 8;
        public static final int DEFENSIVE_RANGED = 9;
        public static final int DEFENSIVE_SUMMONING = 10;
        public static final int ABSORB_MELEE = 11;
        public static final int ABSORB_MAGIC = 12;
        public static final int ABSORB_RANGED = 13;
        public static final int OFFENSIVE_STRENGTH = 14;
        public static final int OFFENSIVE_RANGED_STRENGTH = 15;
        public static final int PRAYER = 16;
        public static final int OFFENSIVE_MAGIC_DAMAGE = 17;
    }

}
