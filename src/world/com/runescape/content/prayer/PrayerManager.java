package com.runescape.content.prayer;

import com.runescape.Static;
import com.runescape.content.handler.ButtonHandler;
import com.runescape.content.prayer.definitions.AbstractPrayerDefinition;
import com.runescape.content.prayer.definitions.AncientCurseDefinition;
import com.runescape.content.prayer.definitions.PrayerDefinition;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.Entity;
import com.runescape.logic.item.EquipmentDefinition.WeaponStyles;
import com.runescape.logic.player.Player;
import com.runescape.utility.Logging;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class PrayerManager implements ButtonHandler {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = Logging.log();

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3) {
        handleButtons(player, opcode, interfaceId, b, b2);
    }

    public PrayerManager(Book book) {
        this.book = book;
        for (AbstractPrayerDefinition a : curseDefinitions.values()) {
            AncientCurseDefinition def = (AncientCurseDefinition) a;
            this.curses.put(def.getId(), new AncientCurse(def));
        }
        for (AbstractPrayerDefinition a : prayerDefinitions.values()) {
            PrayerDefinition def = (PrayerDefinition) a;
            this.prayers.put(def.getId(), new Prayer(def));
        }
    }

    public PrayerManager() {
    }

    public static enum Book {
        PRAYER,
        ANCIENT_CURSES;

        private int getConfigId(InterfaceType type) {
            switch (this) {
                case PRAYER:
                    if (type == InterfaceType.NORMAL)
                        return 1395;
                    else
                        return 1397;
                case ANCIENT_CURSES:
                    if (type == InterfaceType.NORMAL)
                        return 1582;
                    else
                        return 1587;
            }
            return 0;
        }
    }

    private static enum InterfaceType {
        NORMAL,
        QUICK_SELECT
    }

    public static enum CurseType {
        SAP,
        LEECH,
        OVERHEAD,
        SUPER,
        OTHER
    }

    public static enum PrayerType {
        SKIN,
        STRENGTH,
        REFLEXES,
        EYE,
        MYSTIC,
        SUPER,
        OVERHEAD,
        OTHER
    }

    private static Map<Integer, AncientCurseDefinition> curseDefinitions;
    private static Map<Integer, PrayerDefinition> prayerDefinitions;

    protected Map<Integer, AncientCurse> curses = new HashMap<Integer, AncientCurse>();
    private Map<Integer, Prayer> prayers = new HashMap<Integer, Prayer>();

    private boolean[] quickSelectCurses = new boolean[20];
    private boolean[] quickSelectPrayers = new boolean[30];

    protected Book book;
    protected InterfaceType interfaceType = InterfaceType.NORMAL;

    private boolean quickSelectionOn = false;

    protected double strengthMultiplier = 1;
    protected double attackMultiplier = 1;
    protected double defenceMultiplier = 1;
    protected double magicMultiplier = 1;
    protected double rangedMultiplier = 1;

    protected double additionalDrainedStrengthPercentage = 0;
    protected double additionalDrainedAttackPercentage = 0;
    protected double additionalDrainedDefencePercentage = 0;
    protected double additionalDrainedRangedPercentage = 0;
    protected double additionalDrainedMagicPercentage = 0;
    // spirit

    protected double initialDrainedStrengthPercentage = 0;
    protected double initialDrainedAttackPercentage = 0;
    protected double initialDrainedDefencePercentage = 0;
    protected double initialDrainedRangedPercentage = 0;
    protected double initialDrainedMagicPercentage = 0;

    protected double additionalStrengthBoostPercentage = 0;
    protected double additionalAttackBoostPercentage = 0;
    protected double additionalDefenceBoostPercentage = 0;
    protected double additionalRangedBoostPercentage = 0;
    protected double additionalMagicBoostPercentage = 0;
    // energy
    // spirit

    protected double turmoilAttackBoost = 0;
    protected double turmoilStrengthBoost = 0;
    protected double turmoilDefenceBoost = 0;

    protected boolean deflectMelee = false;
    protected boolean protectMelee = false;

    protected boolean deflectMagic = false;
    protected boolean protectMagic = false;

    protected boolean deflectRanged = false;
    protected boolean protectRanged = false;

    protected boolean rapidRestore = false;
    protected boolean rapidHeal = false;
    protected boolean retribution = false;
    protected boolean redemption = false;
    protected boolean smite = false;
    protected boolean rapidRenewal = false;

    protected boolean berserker = false;
    protected boolean wrath = false;
    protected boolean soulSplit = false;
    protected boolean turmoil = false;

    protected boolean dScimAffected = false;
    
    public abstract void takeHit(Entity owner, Entity enemy, int damage, WeaponStyles type, int splatDelay, int hpDrainDelay);

    public abstract void dealHit(Entity owner, Entity enemy, int damage, WeaponStyles type, int splatDelay, int hpDrainDelay);

    private static void handleButtons(Player player, int opcode, int interfaceId, int b1, int b2) {
        switch (interfaceId) {
            case 749:
                switch (b1) {
                    case 1:
                        switch (opcode) {
                            case 1:
                                if (player.getPrayerManager().quickSelectionOn)
                                    turnQuickSelectionOff(player, true);
                                else
                                    turnQuickSelectionOn(player);
                                break;
                            case 2:
                                if (player.getPrayerManager().interfaceType == InterfaceType.NORMAL)
                                    openQuickSelection(player);
                                else
                                    closeQuickSelection(player);
                                break;
                        }
                        break;
                }
                break;
            case 271:
                switch (b1) {
                    case 12:
                        // show stats adjustments;
                        break;
                    case 8:
                        handleSelection(player, b2);
                        break;
                    case 42:
                        handleQuickSelection(player, b2);
                        break;
                    case 43:
                        closeQuickSelection(player);
                }
                break;
        }
    }

    private static void handleSelection(Player player, int prayerId) {
    	if (!player.getPrayerManager().dScimAffected) {
	        switch (player.getPrayerManager().book) {
	            case PRAYER:
	                Prayer prayer = player.getPrayerManager().prayers.get(prayerId);
	                handleSwitching(player, prayer);
	                break;
	            case ANCIENT_CURSES:
	                AncientCurse curse = player.getPrayerManager().curses.get(prayerId);
	                handleSwitching(player, curse);
	                break;
	        }
	        sendButtonConfiguration(player);
    	} else {
    		player.sendMessage("You are affected by a dragon scimitar special.");
    	}
    }

    private static void closeQuickSelection(Player player) {
        player.getPrayerManager().interfaceType = InterfaceType.NORMAL;
        Static.proto.sendConfig(player, 1396, 0);
        Static.proto.sendInterfaceVariable(player, 181, 0);
        Static.proto.sendInterfaceVariable(player, 186, 6);
        sendButtonConfiguration(player);
    }

    private static void openQuickSelection(Player player) {
        player.getPrayerManager().interfaceType = InterfaceType.QUICK_SELECT;
        Static.proto.sendConfig(player, 1396, 1);
        Static.proto.sendInterfaceVariable(player, 181, 1);
        Static.proto.sendInterfaceVariable(player, 168, 6);
        Static.proto.sendAccessMask(player, 0, 29, 271, 42, 0, 2);
        sendButtonConfiguration(player);
    }

    private static void handleQuickSelection(Player player, int prayerId) {
        turnQuickSelectionOff(player, false);
        switch (player.getPrayerManager().book) {
            case PRAYER:
                if (!player.getPrayerManager().quickSelectPrayers[prayerId]) {
                    PrayerDefinition def = player.getPrayerManager().prayers.get(prayerId).getDefinition();
                    if (player.getLevels().getLevel(5) >= def.getRequiredLevel()) {
                        PrayerType type = def.getType();
                        if (type != PrayerType.SUPER && type != PrayerType.OTHER) {
                            for (int i = 0; i < player.getPrayerManager().quickSelectPrayers.length; i++) {
                                Prayer p = player.getPrayerManager().prayers.get(i);
                                if (i != prayerId) {
                                    if (type == p.getDefinition().getType())
                                        player.getPrayerManager().quickSelectPrayers[i] = false;
                                    if (p.getDefinition().getType() == PrayerType.SUPER)
                                        player.getPrayerManager().quickSelectPrayers[p.definition.getId()] = false;
                                    if (type == PrayerType.MYSTIC) {
                                        if (p.getDefinition().getType() == PrayerType.EYE)
                                            player.getPrayerManager().quickSelectPrayers[p.definition.getId()] = false;
                                        if (p.getDefinition().getType() == PrayerType.REFLEXES)
                                            player.getPrayerManager().quickSelectPrayers[p.definition.getId()] = false;
                                        if (p.getDefinition().getType() == PrayerType.STRENGTH)
                                            player.getPrayerManager().quickSelectPrayers[p.definition.getId()] = false;
                                    }
                                    if (type == PrayerType.EYE) {
                                        if (p.getDefinition().getType() == PrayerType.MYSTIC)
                                            player.getPrayerManager().quickSelectPrayers[p.definition.getId()] = false;
                                        if (p.getDefinition().getType() == PrayerType.REFLEXES)
                                            player.getPrayerManager().quickSelectPrayers[p.definition.getId()] = false;
                                        if (p.getDefinition().getType() == PrayerType.STRENGTH)
                                            player.getPrayerManager().quickSelectPrayers[p.definition.getId()] = false;
                                    }
                                    if (type == PrayerType.REFLEXES || type == PrayerType.STRENGTH) {
                                        if (p.getDefinition().getType() == PrayerType.EYE)
                                            player.getPrayerManager().quickSelectPrayers[p.definition.getId()] = false;
                                        if (p.getDefinition().getType() == PrayerType.MYSTIC)
                                            player.getPrayerManager().quickSelectPrayers[p.definition.getId()] = false;
                                    }
                                }
                            }
                        } else if (type == PrayerType.SUPER) {
                            for (int i = 0; i < player.getPrayerManager().quickSelectPrayers.length; i++) {
                                Prayer p = player.getPrayerManager().prayers.get(i);
                                if (i != prayerId) {
                                    switch (p.getDefinition().getType()) {
                                        case EYE:
                                        case MYSTIC:
                                        case REFLEXES:
                                        case SKIN:
                                        case STRENGTH:
                                        case SUPER:
                                            player.getPrayerManager().quickSelectPrayers[i] = false;
                                            break;
                                    }
                                }
                            }
                        }
                    } else
                        Static.proto.sendMessage(player, "You need level " + def.getRequiredLevel() + " prayer to select this quick prayer.");
                }
                player.getPrayerManager().quickSelectPrayers[prayerId] = player.getPrayerManager().quickSelectPrayers[prayerId] ? false : true;
                break;
            case ANCIENT_CURSES:
                if (!player.getPrayerManager().quickSelectCurses[prayerId]) {
                    AncientCurseDefinition def = player.getPrayerManager().curses.get(prayerId).getDefinition();
                    if (player.getLevels().getLevel(5) >= def.getRequiredLevel()) {
                        CurseType type = def.getType();
                        if (type != CurseType.SUPER && type != CurseType.OTHER) {
                            if (type == CurseType.SAP) {
                                for (int i = 0; i < player.getPrayerManager().quickSelectCurses.length; i++) {
                                    AncientCurse c = player.getPrayerManager().curses.get(i);
                                    if (i != prayerId) {
                                        if (c.getDefinition().getType() == CurseType.LEECH || c.getDefinition().getType() == CurseType.SUPER) {
                                            player.getPrayerManager().quickSelectCurses[i] = false;
                                        }
                                    }
                                }
                            } else if (type == CurseType.LEECH) {
                                for (int i = 0; i < player.getPrayerManager().quickSelectCurses.length; i++) {
                                    AncientCurse c = player.getPrayerManager().curses.get(i);
                                    if (i != prayerId) {
                                        if (c.getDefinition().getType() == CurseType.SAP || c.getDefinition().getType() == CurseType.SUPER) {
                                            player.getPrayerManager().quickSelectCurses[i] = false;
                                        }
                                    }
                                }
                            } else if (type == CurseType.OVERHEAD) {
                                for (int i = 0; i < player.getPrayerManager().quickSelectCurses.length; i++) {
                                    AncientCurse c = player.getPrayerManager().curses.get(i);
                                    if (i != prayerId) {
                                        if (c.getDefinition().getType() == CurseType.OVERHEAD/*c.getDefinition().getType()*/) {
                                            player.getPrayerManager().quickSelectCurses[i] = false;
                                        }
                                    }
                                }
                            }
                        } else if (type == CurseType.SUPER) {
                            for (int i = 0; i < player.getPrayerManager().quickSelectCurses.length; i++) {
                                AncientCurse c = player.getPrayerManager().curses.get(i);
                                if (i != prayerId) {
                                    switch (c.getDefinition().getType()) {
                                        case LEECH:
                                        case SAP:
                                            player.getPrayerManager().quickSelectCurses[i] = false;
                                            break;
                                    }
                                }
                            }
                        }
                    } else
                        Static.proto.sendMessage(player, "You need level " + def.getRequiredLevel() + " prayer to select this quick prayer.");
                }
                player.getPrayerManager().quickSelectCurses[prayerId] = !player.getPrayerManager().quickSelectCurses[prayerId];
                break;
        }
        sendButtonConfiguration(player);
    }

    private static void turnQuickSelectionOn(Player player) {
    	if (!player.getPrayerManager().dScimAffected) {
	        player.getAppearance().setPrayerIcon(-1);
	        switch (player.getPrayerManager().book) {
	            case PRAYER:
	                for (Prayer p : player.getPrayerManager().prayers.values())
	                    p.switchStatus(player, false);
	                for (int i = 0; i < player.getPrayerManager().prayers.size(); i++) {
	                    if (player.getPrayerManager().quickSelectPrayers[i]) {
	
	                        handleSwitching(player, player.getPrayerManager().prayers.get(i));
	                        player.getPrayerManager().quickSelectionOn = true;
	                    }
	                }
	                break;
	            case ANCIENT_CURSES:
	                for (AncientCurse c : player.getPrayerManager().curses.values())
	                    c.switchStatus(player, false);
	                for (int i = 0; i < player.getPrayerManager().curses.size(); i++) {
	                    if (player.getPrayerManager().quickSelectCurses[i]) {
	                        handleSwitching(player, player.getPrayerManager().curses.get(i));
	                        player.getPrayerManager().quickSelectionOn = true;
	                    }
	                }
	                break;
	        }
	        updateAdjustments(player);
	        sendButtonConfiguration(player);
	        if (player.getPrayerManager().quickSelectionOn)
	            Static.proto.sendInterfaceVariable(player, 182, 1);
	        else
	            Static.proto.sendMessage(player, "You have not made a quick prayer selection.");
	        player.getAppearance().refresh();
    	} else {
    		player.sendMessage("Your prayer abilities were taken away by a dragon scimitar.");
    	}
    }

    public static void turnQuickSelectionOff(Player player, boolean reset) {
        Static.proto.sendInterfaceVariable(player, 182, 0);
        player.getPrayerManager().quickSelectionOn = false;
        if (reset)
            resetPrayers(player);
    }

    public static void resetPrayers(Player player) {
        for (Prayer p : player.getPrayerManager().prayers.values())
            p.switchStatus(player, false);
        for (AncientCurse c : player.getPrayerManager().curses.values())
            c.switchStatus(player, false);
        player.getAppearance().setPrayerIcon(-1);
        player.getAppearance().refresh();
        updateAdjustments(player);
        sendButtonConfiguration(player);
    }
    
    public static void hitByDragonScimitar(Entity entity) {
    	if (entity instanceof Player) {
    		final Player player = (Player) entity;
    		resetPrayers(player);
    		turnQuickSelectionOff(player, false);
    		player.getPrayerManager().dScimAffected = true;
    		player.registerTick(new Tick("DScimEffect", 9) {
    			@Override
    			public boolean execute() {
    				player.getPrayerManager().dScimAffected = false;
    				return false;
    			}
    		});
    	}
    }

    private static void handleSwitching(Player player, AbstractPrayer prayer) {
        turnQuickSelectionOff(player, false);
        switch (player.getPrayerManager().book) {
            case PRAYER:
                Prayer p = (Prayer) prayer;
                PrayerType type = p.getDefinition().getType();
                if (!p.isOn) {
                    if (player.getLevels().getCurrentLevel(5) > 0) {
                        if (player.getLevels().getLevel(5) >= p.definition.getRequiredLevel()) {
                            p.switchStatus(player, true);
                            if (type != PrayerType.SUPER && type != PrayerType.OTHER) {
                                for (Prayer p2 : player.getPrayerManager().prayers.values()) {
                                    if (p2 != p) {
                                        if (type == p2.getDefinition().getType())
                                            p2.switchStatus(player, false);
                                        if (p2.getDefinition().getType() == PrayerType.SUPER)
                                            p2.switchStatus(player, false);
                                        if (type == PrayerType.MYSTIC) {
                                            if (p2.getDefinition().getType() == PrayerType.EYE)
                                                p2.switchStatus(player, false);
                                            if (p2.getDefinition().getType() == PrayerType.REFLEXES)
                                                p2.switchStatus(player, false);
                                            if (p2.getDefinition().getType() == PrayerType.STRENGTH)
                                                p2.switchStatus(player, false);
                                        }
                                        if (type == PrayerType.EYE) {
                                            if (p2.getDefinition().getType() == PrayerType.MYSTIC)
                                                p2.switchStatus(player, false);
                                            if (p2.getDefinition().getType() == PrayerType.REFLEXES)
                                                p2.switchStatus(player, false);
                                            if (p2.getDefinition().getType() == PrayerType.STRENGTH)
                                                p2.switchStatus(player, false);
                                        }
                                        if (type == PrayerType.REFLEXES || type == PrayerType.STRENGTH) {
                                            if (p2.getDefinition().getType() == PrayerType.EYE)
                                                p2.switchStatus(player, false);
                                            if (p2.getDefinition().getType() == PrayerType.MYSTIC)
                                                p2.switchStatus(player, false);
                                        }
                                    }
                                }
                            } else if (type == PrayerType.SUPER) {
                                for (Prayer p2 : player.getPrayerManager().prayers.values()) {
                                    if (p2 != p) {
                                        switch (p2.getDefinition().getType()) {
                                            case EYE:
                                            case MYSTIC:
                                            case REFLEXES:
                                            case SKIN:
                                            case STRENGTH:
                                            case SUPER:
                                                p2.switchStatus(player, false);
                                                break;
                                        }
                                    }
                                }
                            }
                            if (type == PrayerType.OVERHEAD) {
                                player.getAppearance().setPrayerIcon(p.definition.getPrayerIcon());
                                player.getAppearance().refresh();
                            }
                        } else
                            Static.proto.sendMessage(player, "You need level " + p.definition.getRequiredLevel() + " prayer to activate this prayer.");
                    } else
                        Static.proto.sendMessage(player, "You don't have enough prayer points to do this; you can recharge your prayer at an altar.");
                } else {
                    p.switchStatus(player, false);
                }
                if (type == PrayerType.OVERHEAD && !p.isOn) {
                    player.getAppearance().setPrayerIcon(-1);
                    player.getAppearance().refresh();
                }
                break;
            case ANCIENT_CURSES:
                AncientCurse c = (AncientCurse) prayer;
                CurseType type1 = c.getDefinition().getType();
                if (!c.isOn) {
                    if (player.getLevels().getCurrentLevel(5) > 0) {
                        if (player.getLevels().getLevel(5) >= c.definition.getRequiredLevel()) {
                            c.switchStatus(player, true);
                            if (type1 != CurseType.SUPER && type1 != CurseType.OTHER) {
                                if (type1 == CurseType.LEECH) {
                                    for (AncientCurse c2 : player.getPrayerManager().curses.values()) {
                                        if (c2 != c)
                                            if (c2.getDefinition().getType() == CurseType.SAP || c2.getDefinition().getType() == CurseType.SUPER)
                                                c2.switchStatus(player, false);
                                    }
                                } else if (type1 == CurseType.SAP) {
                                    for (AncientCurse c2 : player.getPrayerManager().curses.values()) {
                                        if (c2 != c)
                                            if (c2.getDefinition().getType() == CurseType.LEECH || c2.getDefinition().getType() == CurseType.SUPER)
                                                c2.switchStatus(player, false);
                                    }
                                } else if (type1 == CurseType.OVERHEAD) {
                                    for (AncientCurse c2 : player.getPrayerManager().curses.values()) {
                                        if (c2 != c)
                                            if (c2.getDefinition().getType() == CurseType.OVERHEAD)
                                                c2.switchStatus(player, false);
                                    }
                                }
                            } else if (type1 == CurseType.SUPER) {
                                for (AncientCurse c2 : player.getPrayerManager().curses.values()) {
                                    if (c2 != c) {
                                        switch (c2.getDefinition().getType()) {
                                            case LEECH:
                                            case SAP:
                                                c2.switchStatus(player, false);
                                        }
                                    }
                                }
                            }
                            if (c.getDefinition().isUseAnimation())
                                player.doAnimation(c.getDefinition().getAnimationId());
                            if (c.getDefinition().isUseGraphic())
                                player.doGraphics(c.getDefinition().getGraphicId());
                            if (type1 == CurseType.OVERHEAD) {
                                player.getAppearance().setPrayerIcon(c.definition.getPrayerIcon());
                                player.getAppearance().refresh();
                            }
                        } else
                            Static.proto.sendMessage(player, "You need level " + c.definition.getRequiredLevel() + " prayer to activate this prayer.");
                    } else
                        Static.proto.sendMessage(player, "You don't have enough prayer points to do this; you can recharge your prayer at an altar.");
                } else {
                    c.switchStatus(player, false);
                }
                if (type1 == CurseType.OVERHEAD && !c.isOn) {
                    player.getAppearance().setPrayerIcon(-1);
                    player.getAppearance().refresh();
                }
                break;
        }
        updateAdjustments(player);
    }

    private static void sendButtonConfiguration(Player player) {
        int config = 0;
        switch (player.getPrayerManager().interfaceType) {
            case NORMAL:
                switch (player.getPrayerManager().book) {
                    case PRAYER:
                        for (int i = 0; i < player.getPrayerManager().prayers.size(); i++) {
                            Prayer p = player.getPrayerManager().prayers.get(i);
                            config |= p.isOn ? p.definition.getConfigMaskValue() : 0;
                        }
                        break;
                    case ANCIENT_CURSES:
                        for (int i = 0; i < player.getPrayerManager().curses.size(); i++) {
                            config += player.getPrayerManager().curses.get(i).isOn ? (1 << i) : 0;
                        }
                        break;
                }
                break;
            case QUICK_SELECT:
                switch (player.getPrayerManager().book) {
                    case PRAYER:
                        int j = 0;
                        for (boolean b : player.getPrayerManager().quickSelectPrayers) {
                            Prayer p = player.getPrayerManager().prayers.get(j);
                            config |= b ? p.definition.getConfigMaskValue() : 0;
                            j++;
                        }
                        break;
                    case ANCIENT_CURSES:
                        int i = 0;
                        for (boolean b : player.getPrayerManager().quickSelectCurses) {
                            config += b ? (1 << i) : 0;
                            i++;
                        }
                        break;
                }
                break;
        }
        Static.proto.sendConfig(player, player.getPrayerManager().book.getConfigId(player.getPrayerManager().interfaceType), config);
    }

    public static void switchPrayerBook(Player player, boolean cursesOn) {
        for (Prayer p : player.getPrayerManager().prayers.values())
            p.switchStatus(player, false);
        for (AncientCurse c : player.getPrayerManager().curses.values())
            c.switchStatus(player, false);
        updateAdjustments(player);
        Static.proto.sendConfig(player, 1584, cursesOn ? 1 : 0);
        player.getPrayerManager().book = cursesOn ? Book.ANCIENT_CURSES : Book.PRAYER;
    }

    public static void updateAdjustments(Player player) {
        PrayerManager manager = player.getPrayerManager();
        double attack = 1 + manager.additionalAttackBoostPercentage + manager.additionalDrainedAttackPercentage + manager.initialDrainedAttackPercentage + manager.turmoilAttackBoost;
        double strength = 1 + manager.additionalStrengthBoostPercentage + manager.additionalDrainedStrengthPercentage + manager.initialDrainedStrengthPercentage + manager.turmoilStrengthBoost;
        double defence = 1 + manager.additionalDefenceBoostPercentage + manager.additionalDrainedDefencePercentage + manager.initialDrainedDefencePercentage + manager.turmoilDefenceBoost;
        double ranged = 1 + manager.additionalRangedBoostPercentage + manager.additionalDrainedRangedPercentage + manager.initialDrainedRangedPercentage;
        double magic = 1 + manager.additionalMagicBoostPercentage + manager.additionalDrainedMagicPercentage + manager.initialDrainedMagicPercentage;
        attack = Math.round(((attack > 1 ? attack - 1 : -(1 - attack)) * 100) + 30);
        strength = Math.round(((strength > 1 ? strength - 1 : -(1 - strength)) * 100) + 30);
        defence = Math.round(((defence > 1 ? defence - 1 : -(1 - defence)) * 100) + 30);
        ranged = Math.round(((ranged > 1 ? ranged - 1 : -(1 - ranged)) * 100) + 30);
        magic = Math.round(((magic > 1 ? magic - 1 : -(1 - magic)) * 100) + 30);
        int adjustments = 0;
        adjustments |= (int) attack;
        adjustments |= (int) strength << 6;
        adjustments |= (int) defence << 12;
        adjustments |= (int) ranged << 18;
        adjustments |= (int) magic << 24;
        Static.proto.sendConfig(player, 1583, adjustments);
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        if (curseDefinitions != null)
            throw new IllegalStateException("Ancient curses were already loaded.");
        if (prayerDefinitions != null)
            throw new IllegalStateException("Prayers were already loaded.");
        try {
            /**
             * Load prayer definitions.
             */
            curseDefinitions = (Map<Integer, AncientCurseDefinition>) Static.xml.readObject(Static.parseString("%WORK_DIR%/world/items/combat/ancientcurses.xml"));
            prayerDefinitions = (Map<Integer, PrayerDefinition>) Static.xml.readObject(Static.parseString("%WORK_DIR%/world/items/combat/prayers.xml"));
            for (int i = 0; i < quickSelectCurses.length; i++)
                quickSelectCurses[i] = false;
            for (int i = 0; i < quickSelectPrayers.length; i++)
                quickSelectPrayers[i] = false;
            if (curseDefinitions != null && prayerDefinitions != null) {
                LOGGER.info("Loaded " + curseDefinitions.size() + " curses");
                LOGGER.info("Loaded " + prayerDefinitions.size() + " prayers");
            } else
                LOGGER.error("Prayer or ancient curse definitions not found");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCurses() {
        return book == Book.ANCIENT_CURSES;
    }

    public void setCurses(boolean curses) {
        book = curses ? Book.ANCIENT_CURSES : Book.PRAYER;
    }

    public byte[] getQuickPrayers() {
        int i = 0;
        byte[] quickSelect = null;
        switch (book) {
            case ANCIENT_CURSES:
                quickSelect = new byte[20];
                for (boolean b : quickSelectCurses) {
                    quickSelect[i++] = (byte) (b ? 1 : 0);
                }
                break;
            case PRAYER:
                quickSelect = new byte[30];
                for (boolean b : quickSelectPrayers) {
                    quickSelect[i++] = (byte) (b ? 1 : 0);
                }
                break;
        }
        return quickSelect;
    }

    public void setQuickPrayers(byte[] quickPrayers) {
        int i = 0;
        switch (book) {
            case ANCIENT_CURSES:
                for (byte b : quickPrayers) {
                    quickSelectCurses[i++] = b == 1;
                }
                break;
            case PRAYER:
                for (byte b : quickPrayers) {
                    quickSelectPrayers[i++] = b == 1;
                }
                break;
        }
    }

    public void sendBook(Player player) {
        Static.proto.sendConfig(player, 1584, book == Book.ANCIENT_CURSES ? 1 : 0);
    }

    public void addStrengthMultiplier(double add) {
        strengthMultiplier += add;
        if (strengthMultiplier < 1) {
            strengthMultiplier = 1;
        }
    }

    public double getStrengthMultiplier() {
        return strengthMultiplier + initialDrainedStrengthPercentage + additionalDrainedStrengthPercentage + additionalStrengthBoostPercentage + turmoilStrengthBoost;
    }

    public void addAttackMultiplier(double add) {
        attackMultiplier += add;
        if (attackMultiplier < 1)
            attackMultiplier = 1;
    }

    public double getAttackMultiplier() {
        return attackMultiplier + initialDrainedAttackPercentage + additionalDrainedAttackPercentage + additionalAttackBoostPercentage + turmoilAttackBoost;
    }

    public void addDefenceMultiplier(double add) {
        defenceMultiplier += add;
        if (defenceMultiplier < 1)
            defenceMultiplier = 1;
    }

    public double getDefenceMultiplier() {
        return defenceMultiplier + initialDrainedDefencePercentage + additionalDrainedDefencePercentage + additionalDefenceBoostPercentage + turmoilDefenceBoost;
    }

    public void addMagicMultiplier(double add) {
        magicMultiplier += add;
        if (magicMultiplier < 1)
            magicMultiplier = 1;
    }

    public double getMagicMultiplier() {
        return magicMultiplier + initialDrainedMagicPercentage + additionalDrainedMagicPercentage + additionalMagicBoostPercentage;
    }

    public void addRangedMultiplier(double add) {
        rangedMultiplier += add;
        if (rangedMultiplier < 1)
            rangedMultiplier = 1;
    }

    public double getRangedMultiplier() {
        return rangedMultiplier + initialDrainedRangedPercentage + additionalDrainedRangedPercentage + additionalRangedBoostPercentage;
    }

    public final double getTurmoilAttackBoost() {
        return turmoilAttackBoost;
    }

    public final double getTurmoilStrengthBoost() {
        return turmoilStrengthBoost;
    }

    public final double getTurmoilDefenceBoost() {
        return turmoilDefenceBoost;
    }

    public final void setTurmoilAttackBoost(double turmoilAttackBoost) {
        this.turmoilAttackBoost = turmoilAttackBoost;
    }

    public final void setTurmoilStrengthBoost(double turmoilStrengthBoost) {
        this.turmoilStrengthBoost = turmoilStrengthBoost;
    }

    public final void setTurmoilDefenceBoost(double turmoilDefenceBoost) {
        this.turmoilDefenceBoost = turmoilDefenceBoost;
    }

    public void deflectMelee(boolean deflectMelee) {
        this.deflectMelee = deflectMelee;
    }

    public boolean deflectMelee() {
        return deflectMelee;
    }

    public void protectMelee(boolean protectMelee) {
        this.protectMelee = protectMelee;
    }

    public boolean protectMelee() {
        return protectMelee;
    }

    public void deflectMagic(boolean deflectMagic) {
        this.deflectMagic = deflectMagic;
    }

    public boolean deflectMagic() {
        return deflectMagic;
    }

    public void protectMagic(boolean protectMagic) {
        this.protectMagic = protectMagic;
    }

    public boolean protectMagic() {
        return protectMagic;
    }

    public void deflectRanged(boolean deflectRanged) {
        this.deflectRanged = deflectRanged;
    }

    public boolean deflectRanged() {
        return deflectRanged;
    }

    public void protectRanged(boolean protectRanged) {
        this.protectRanged = protectRanged;
    }

    public boolean protectRanged() {
        return protectRanged;
    }

    public void rapidRestore(boolean rapidRestore) {
        this.rapidRestore = rapidRestore;
    }

    public boolean rapidRestore() {
        return rapidRestore;
    }

    public void rapidHeal(boolean rapidHeal) {
        this.rapidHeal = rapidHeal;
    }

    public boolean rapidHeal() {
        return rapidHeal;
    }

    public void retribution(boolean retribution) {
        this.retribution = retribution;
    }

    public boolean retribution() {
        return retribution;
    }

    public void redemption(boolean redemption) {
        this.redemption = redemption;
    }

    public boolean redemption() {
        return redemption;
    }

    public void smite(boolean smite) {
        this.smite = smite;
    }

    public boolean smite() {
        return smite;
    }

    public void rapidRenewal(boolean rapidRenewal) {
        this.rapidRenewal = rapidRenewal;
    }

    public boolean rapidRenewal() {
        return rapidRenewal;
    }

    public void berserker(boolean berserker) {
        this.berserker = berserker;
    }

    public boolean berserker() {
        return berserker;
    }

    public void wrath(boolean wrath) {
        this.wrath = wrath;
    }

    public boolean wrath() {
        return wrath;
    }

    public void soulSplit(boolean soulSplit) {
        this.soulSplit = soulSplit;
    }

    public boolean soulSplit() {
        return soulSplit;
    }

    public void turmoil(boolean turmoil) {
        this.turmoil = turmoil;
    }

    public boolean turmoil() {
        return turmoil;
    }

}
