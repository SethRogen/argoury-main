package com.ziotic.content.combat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.Logger;

import com.ziotic.Constants.Equipment;
import com.ziotic.Static;
import com.ziotic.content.combat.Combat.ActionType;
import com.ziotic.content.combat.definitions.SpellDefinition;
import com.ziotic.content.combat.definitions.SpellDefinition.CombatType;
import com.ziotic.content.combat.definitions.SpellDefinition.ElementType;
import com.ziotic.content.combat.definitions.SpellDefinition.GroupType;
import com.ziotic.content.combat.misc.CombatUtilities;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.Entity;
import com.ziotic.logic.HPHandlerTick.HPRemoval;
import com.ziotic.logic.item.EquipmentDefinition;
import com.ziotic.logic.item.EquipmentDefinition.Bonuses;
import com.ziotic.logic.item.EquipmentDefinition.WeaponStyles;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.mask.Splat;
import com.ziotic.logic.mask.Splat.SplatCause;
import com.ziotic.logic.mask.Splat.SplatType;
import com.ziotic.logic.mask.SplatNode;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.player.DisplayMode;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;

public class Magic {

    public static Map<Integer, Map<Integer, SpellDefinition>> spells = null;
    private static final Logger LOGGER = Logging.log();
    private static final Random RANDOM = new Random();

    @SuppressWarnings("unused")
    private static final int[] ELEMENTAL_STAVES = new int[]{
            6914, 1381, 1383, 1385, 1387, 1389, 6603, 1401,
            1403, 1405, 1407, 3054, 1379, 6563, 11738, 2415,
            2416, 2417, 4862, 4863, 4864, 4865, 4710
    };

    protected SpellBook book = SpellBook.ANCIENT;
    protected AutoCast autoCast = new AutoCast();
    protected boolean isCharged = false;
    protected boolean vengeanceCasted = false;
    protected long lastVengTime = 0;

    public static enum SpellBook {
        ANCIENT(193),
        LUNAR(430),
        MODERN(192);

        int id;

        SpellBook(int id) {
            this.id = id;
        }

        public static final int interfaceForValue(SpellBook book) {
            switch (book) {
                case ANCIENT:
                    return 193;
                case LUNAR:
                    return 430;
                case MODERN:
                    return 192;
                default:
                    return -1;
            }
        }

        public static SpellBook getBookForValue(int id) {
            SpellBook book = null;
            switch (id) {
                case 192:
                    book = SpellBook.MODERN;
                    break;
                case 193:
                    book = SpellBook.ANCIENT;
                    break;
                case 430:
                    book = SpellBook.LUNAR;
                    break;
            }
            return book;
        }
    }

    public static boolean setAutoCast(Player player, int interfaceId, int spellId) {
        Magic m = player.getCombat().getMagic();
        if (interfaceId == SpellBook.interfaceForValue(m.book)) {
            SpellDefinition def;
            if (m.isAutocasting())
                def = m.getAutoCastDefinition();
            else
                def = Magic.getDefinition(player, interfaceId, spellId, true);
            if (def != null) {
                if (spellId == m.autoCast.getAutoCastSpellId(m.book)) {
                    m.autoCast.setAutoCast(m.book, spellId, false);
                    Static.proto.sendConfig(player, 108, def.autocastConfig - 1);
                    Static.proto.sendConfig(player, 108, 0);
                    PossesedItem i = player.getEquipment().get(Equipment.WEAPON_SLOT);
                    if (i != null)
                        Static.proto.sendConfig(player, 43, i.getDefinition().weaponGroupId);
                    else
                        Static.proto.sendConfig(player, 43, 0);
                    if (player.getDisplayMode() == DisplayMode.FIXED)
                        Static.proto.sendAccessMask(player, -1, -1, 548, 123, 0, 2);
                    else
                        Static.proto.sendAccessMask(player, -1, -1, 746, 36, 0, 2);
                    Static.proto.sendAccessMask(player, -1, -1, 884, 11, 0, 2);
                    Static.proto.sendAccessMask(player, -1, -1, 884, 12, 0, 2);
                    Static.proto.sendAccessMask(player, -1, -1, 884, 13, 0, 2);
                    Static.proto.sendAccessMask(player, -1, -1, 884, 14, 0, 2);
                    if (m.book == SpellBook.MODERN)
                        Static.proto.sendInterfaceVariable(player, 1000, 71);

                } else {
                    m.autoCast.setAutoCast(m.book, spellId, true);
                    Static.proto.sendConfig(player, 108, 1);
                    Static.proto.sendConfig(player, 108, def.autocastConfig);
                    Static.proto.sendConfig(player, 43, 4);
                }
                return true;
            } else {
                //Static.proto.sendMessage(player, "This spell is not available yet.");
                return false;
            }
        } else {
            Static.proto.sendMessage(player, "Stop trying to use a modified client.");
            return false;
        }
    }

    public static void switchBook(Player player, SpellBook book) {
        Magic m = player.getCombat().getMagic();
        m.book = book;
        m.autoCast.resetAutoCast();
    }

    public static Map<Integer, SpellDefinition> getSpellsForBook(SpellBook book) {
        return spells.get(SpellBook.interfaceForValue(book));
    }

    public static SpellDefinition getDefinition(Player player, int interfaceId, int spellId, boolean sendMessage) {
        Magic m = player.getCombat().getMagic();
        if (interfaceId == SpellBook.interfaceForValue(m.book)) {
            return getSpellsForBook(m.book).get(spellId);
        } else {
            if (sendMessage) {
                Static.proto.sendMessage(player, "[Magic] [code:ME1] A bug occured while trying to cast a spell.");
                Static.proto.sendMessage(player, "Please post a bug report according to the rules showing the error code.");
            }
            return null;
        }
    }

    public int getAutoCastSpellId() {
        return autoCast.getAutoCastSpellId(book);
    }

    public SpellDefinition getAutoCastDefinition() {
        SpellDefinition sd = null;
        if (book == SpellBook.ANCIENT) {
            sd = spells.get(SpellBook.interfaceForValue(book)).get(autoCast.ancientId);
        } else if (book == SpellBook.MODERN) {
            sd = spells.get(SpellBook.interfaceForValue(book)).get(autoCast.modernId);
        }
        return sd;
    }

    private class AutoCast {

        private boolean autoCast = false;

        private int ancientId = -1;
        private int modernId = -1;

        private void resetAutoCast() {
            this.setAutoCast(false);
            this.ancientId = -1;
            this.modernId = -1;
        }

        private void setAutoCast(boolean set) {
            this.autoCast = false;
        }

        private void setAutoCast(SpellBook book, int spellId, boolean set) {
            switch (book) {
                case ANCIENT:
                    this.ancientId = spellId;
                    break;
                case MODERN:
                    this.modernId = spellId;
                    break;
            }
            this.autoCast = set;
        }

        private int getAutoCastSpellId(SpellBook book) {
            if (this.autoCast) {
                switch (book) {
                    case ANCIENT:
                        return ancientId;
                    case MODERN:
                        return modernId;
                    default:
                        return -1;
                }
            } else
                return -1;
        }

    }

    public static boolean castSpell(Player player, Entity victim, ActionType type, SpellDefinition def) {
        try {
            Magic magic = player.getCombat().getMagic();
            if (def != null) {
                switch (def.groupType) {
                    case COMBAT:
                        if (canCastSpell(player, victim, def)) {
                            if (def.combatType == CombatType.VENGEANCE) {
                                magic.vengeanceCasted = true;
                                magic.lastVengTime = System.currentTimeMillis();
                                player.getCombat().executeAnimation(def.startAnim, def.startAnimDelay, true, false);
                                player.getCombat().executeGraphics(def.startGfx, def.startGfxDelay, def.startGfxHeight);
                                removeRunes(player, def);
                                return true;
                            } else if (def.combatType == CombatType.TELEBLOCK) {
                            	teleBlock(player, victim, def);
                            	removeRunes(player, def);
                            	return true;
                            }
                            if ((def.freezeTime != -1 && victim.getCombat().isFrozen() != true && def.name.equalsIgnoreCase("entangle"))
                                    || (def.freezeTime != -1 && !def.name.equalsIgnoreCase("entangle"))
                                    || def.freezeTime == -1) {
                                removeRunes(player, def);
                                if (def.startAnim != -1)
                                    player.getCombat().executeAnimation(def.startAnim, def.startAnimDelay, true, false);
                                if (def.startGfx != -1)
                                    player.doGraphics(def.startGfx, def.startGfxDelay, def.startGfxHeight);
                                if (def.projectileId != -1) {
                                    Static.world.sendProjectile(player, victim, def.projectileId, player.getLocation(), victim.getLocation(), def.startHeight, def.endHeight, def.projSpeed, def.projDelay, def.middleHeight, 0, 1);
                                }
                                HashMap<Long, Entity> victims = new HashMap<Long, Entity>();
                                if (def.multi) {
                                    victims = Combat.getMultiVictims(player, victim);
                                }
                                Iterator<Entry<Long, Entity>> it = victims.entrySet().iterator();
                                Entry<Long, Entity> entry = null;
                                while (it.hasNext()) {
                                    entry = it.next();
                                    if (entry != null) {
                                        Entity victim_ = entry.getValue();
                                        if (CombatUtilities.canAttackLevelBased(player, victim_, false))
                                            player.getCombat().hit(victim_, type, false, 1, def.accuracy, def, 1, null, new int[]{0}, new int[]{0});
                                    }
                                }
                                player.getCombat().hit(victim, type, false, 1, def.accuracy, def, 1, null, new int[]{0}, new int[]{0});
                                return true;
                            } else {
                                Static.proto.sendMessage(player, "Your target is already held by a magical force.");
                                return false;
                            }
                        } else {
                            return false;
                        }
                    case ENCHANTMENT:
                        break;
                    case CURSE:
                        break;
                    case ALCHEMY:
                        break;
                    case MISC:
                        break;
                }
            } else {
                //Static.proto.sendMessage(player, "This spell can't be used yet.");
                return false;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean canCastSpell(Player player, Entity victim, SpellDefinition definition) {
        // TODO CLIPPING OF THE PROJECTILE!
    	if (definition.combatType != CombatType.VENGEANCE) {
	    	if (!CombatUtilities.clippedProjectile(player, victim)) {
	    		if (player.getCombat().isFrozen())
	    			Static.proto.sendMessage(player, "A magical force stops you from moving.");
	    		return false;
	    	}
    	}
    	if (!(player.getLevels().getCurrentLevel(Levels.MAGIC) >= definition.requiredLevel)) {
            Static.proto.sendMessage(player, "You need a magic level of " + definition.requiredLevel + " to cast this spell.");
            return false;
        } else if (definition.reqStaff != -1) {
            if (player.getEquipment().get(Equipment.WEAPON_SLOT).getId() != definition.reqStaff) {
                Static.proto.sendMessage(player, "You need to wear a specific staff to cast this spell.");
                return false;
            }
        } else if (!hasRunes(player, definition)) {
            Static.proto.sendMessage(player, "You don't have the required runes to cast this spell.");
            return false;
        } else if (definition.combatType == CombatType.VENGEANCE) {
            return canCastVengeance(player, player.getCombat().getMagic());
        }

        return true;
    }

    public static boolean canCastVengeance(Player player, Magic magic) {
        if (magic.vengeanceCasted) {
            player.sendMessage("You already have Vengeance casted.");
            return false;
        } else {
            long secondsRemaining = Math.round((30000 - (java.lang.System.currentTimeMillis() - magic.lastVengTime)) / 1000);//Math.round((System.currentTimeMillis() - (magic.lastVengTime + 30000)) / 1000);
            if (magic.lastVengTime != 0 && secondsRemaining > 0) {
                player.sendMessage("You must wait " + secondsRemaining + " more seconds before casting Vengeance again.");
                return false;
            } else {
                return true;
            }
        }
    }
    
    public static void teleBlock(final Player player, final Entity victim, SpellDefinition def) {
    	boolean canCast = false;
    	if (victim.teleBlocked) {
    		Tick t = victim.retrieveTick("TeleBlock");
    		if (t != null) {
    			if (t.getCounter() <= 25)
    				canCast = true;
    		} else {
    			canCast = true;
    		}
    	} else
    		canCast = true;
    	if (canCast) {
    		if (def.startAnim != -1)
                player.getCombat().executeAnimation(def.startAnim, def.startAnimDelay, true, false);
            if (def.startGfx != -1)
                player.doGraphics(def.startGfx, def.startGfxDelay, def.startGfxHeight);
            if (def.projectileId != -1) {
                Static.world.sendProjectile(player, victim, def.projectileId, player.getLocation(), victim.getLocation(), def.startHeight, def.endHeight, def.projSpeed, def.projDelay, def.middleHeight, 0, 1);
            }
    		double hitChance = Combat.getAccuracy(player, victim, def.accuracy);

    		double distance = player.getLocation().distance(victim.getLocation());
            double splatDelay = def.projDelay + def.projSpeed + distance * 5;
            if (hitChance >= RANDOM.nextDouble()) {
            	
        		int timer = 0;
        		final boolean isPlayer;
        		if (victim instanceof Player) {
        			Player victim_ = (Player) victim;
        			if (victim_.getPrayerManager().deflectMagic() || victim_.getPrayerManager().protectMagic())
        				timer = 167;
        			else
        				timer = 500;
        			isPlayer = true;
        		} else
        			isPlayer = false;
            	Tick t = new Tick("SetTeleBlock", 2) {
        			@Override
        			public boolean execute() {
        				victim.teleBlocked = true;
        				if (isPlayer) {
        					((Player) victim).sendMessage("You have been teleblocked!");
        				}
        				return false;
        			}
        		};
        		Tick t2 = new Tick("TeleBlock", timer) {
        			@Override
        			public boolean execute() {
        				victim.teleBlocked = false;
        				return false;
        			}
        		};
        		victim.registerTick(t);
        		victim.registerTick(t2);
        		
        		victim.doGraphics(def.endGfx, (int) splatDelay, def.endGfxHeight);
        		
            } else {
            	victim.doGraphics(85, (int) splatDelay, def.endGfxHeight);
            }
    		
    	} else {
    		if (victim instanceof Player) {
    			player.sendMessage("Your victim is already teleblocked.");
    		}
    	}
    }

    public int doMagicHit(final Entity entity, final Entity victim, SpellDefinition def, double accuracyMultiplier, int[] damages, int[] delays) {
        int totalDamage = 0;
        int hp = victim.getHP();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            double distance = player.getLocation().distance(victim.getLocation());
            double splatDelay = def.projDelay + def.projSpeed + distance * 5;
            double hpDrainDelay = splatDelay * 12d;
            hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
            final int defenceAnim;
            double maxHit = getMaxMagicHit(player, def);
            double hitChance = 0;
            SplatType damageSplat = SplatType.DAMAGE;
            SplatType absorbSplat = null;
            if (victim instanceof Player) {
                Player victim_ = (Player) victim;
                hitChance = Combat.getAccuracy(entity, victim, accuracyMultiplier);
                if (hitChance <= RANDOM.nextDouble()) {
                    maxHit = 0;
                    damageSplat = SplatType.BLOCK;
                } else {
                    if ((victim_.getPrayerManager().protectMelee() || victim_.getPrayerManager().deflectMelee()) && !CombatUtilities.wearsArmourSet(player, CombatUtilities.VERACS))
                        maxHit *= 0.6;
                }
                double hitMultiplier = Combat.getHitMultiplier(hitChance);
                int damage = maxHit > 0 ? (damages == null ? ((int) (maxHit * hitMultiplier)) : damages[0]) : 0;
                if (damage > maxHit)
                	damage = (int) maxHit;
                int damageExcess = 0;
                double absorbAmount = 0;
                if (damage > 200) {
                    damageExcess = damage - 200;
                }
                absorbAmount = damageExcess * (victim_.getBonuses()[Bonuses.ABSORB_MELEE] / 100);
                if (absorbAmount > 0) {
                    absorbSplat = SplatType.SOAK;
                    absorbAmount = Math.ceil(absorbAmount);
                    damage -= absorbAmount;
                }
                boolean dead = false;
                if (damage < 1 && maxHit != 0)
                    damage = 1;
                if (damage > hp) {
                    damage = hp;
                    dead = true;
                }
                if (dead) {
                    damage += absorbAmount;
                    if (damage > hp)
                        damage = hp;
                }
                hp -= damage;
                if (damage == 0) {
                    damageSplat = SplatType.BLOCK;
                }
                SplatNode node;
                totalDamage += damage;
                final int damage_ = damage;
                final boolean criticalHit = damage + (0.03 * damage) >= maxHit - absorbAmount;
                if (absorbSplat != null) {
                    node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.MAGIC, criticalHit, 0/*(int) splatDelay2*/)
                            , new Splat(victim, entity, (int) absorbAmount, absorbSplat, SplatCause.NONE, false, 0/*(int) splatDelay*/));
                } else
                    node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.MAGIC, criticalHit, 0/*(int) splatDelay2*/));
                if (hpDrainDelay < 0)
                    hpDrainDelay = 1;
                if (def.endGfx != -1 && damage > 0) {
                    if (def.combatType == CombatType.BARRAGE && def.elementType == ElementType.ICE && victim.getCombat().isFrozen())
                        victim.doGraphics(1677, (int) splatDelay, 50);
                    else
                        victim.doGraphics(def.endGfx, (int) splatDelay, def.endGfxHeight);
                } else if (def.endGfx != -1)
                    victim.doGraphics(85, (int) splatDelay, def.endGfxHeight);
                if (def.freezeTime != -1 && victim.getCombat().freezeImmunity != true && damage > 0)
                    victim.registerTick(Combat.resetFreezeTimerTick(victim, def.freezeTime));
                if (victim instanceof Player) {
                    PossesedItem defence = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_WEAPON);
                    PossesedItem shield = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_SHIELD);
                    if (shield != null) {
                        String name = shield.getDefinition().name;
                        if (name.contains("shield")) {
                            defenceAnim = 1156;
                        } else if (name.contains("defender")) {
                            defenceAnim = 4177;
                        } else {
                            defenceAnim = EquipmentDefinition.DEFAULT_ANIMATIONS.defendAnimation;
                        }
                    } else if (defence != null) {
                        defenceAnim = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_WEAPON)
                                .getDefinition().getEquipmentDefinition().getEquipmentAnimations().defendAnimation;
                    } else {
                        defenceAnim = EquipmentDefinition.DEFAULT_ANIMATIONS.defendAnimation;
                    }
                } else
                    defenceAnim = 0; // temporary

                victim.registerTick(new Tick(null, (int) hpDrainDelay) {

                    @Override
                    public boolean execute() {
                        victim.getCombat().executeAnimation(defenceAnim, 0, false, false);
                        return false;
                    }

                });
                victim.registerHPTick(new HPRemoval(victim, damage_, (int) hpDrainDelay + 1, node));
                player.getPrayerManager().dealHit(player, victim_, damage_, WeaponStyles.MAGIC, (int) splatDelay, (int) hpDrainDelay);
                victim_.getPrayerManager().takeHit(victim_, player, damage_, WeaponStyles.MAGIC, (int) splatDelay, (int) hpDrainDelay);
            } else {
            	NPC victim_ = (NPC) victim;
                hitChance = Combat.getAccuracy(entity, victim, accuracyMultiplier);
                if (hitChance <= RANDOM.nextDouble()) {
                    maxHit = 0;
                    damageSplat = SplatType.BLOCK;
                } else {
//                    if ((victim_.getPrayerManager().protectMelee() || victim_.getPrayerManager().deflectMelee()) && !CombatUtilities.wearsArmourSet(player, CombatUtilities.VERACS))
//                        maxHit *= 0.6; TODO add prayer support for npc's
                }
                double hitMultiplier = Combat.getHitMultiplier(hitChance);
                int damage = maxHit > 0 ? (damages == null ? ((int) (maxHit * hitMultiplier)) : damages[0]) : 0;
                if (damage > maxHit)
                	damage = (int) maxHit;
                int damageExcess = 0;
                double absorbAmount = 0;
                if (damage > 200) {
                    damageExcess = damage - 200;
                }
                absorbAmount = damageExcess * (victim_.getBonuses()[Bonuses.ABSORB_MELEE] / 100);
                if (absorbAmount > 0) {
                    absorbSplat = SplatType.SOAK;
                    absorbAmount = Math.ceil(absorbAmount);
                    damage -= absorbAmount;
                }
                boolean dead = false;
                if (damage < 1 && maxHit != 0)
                    damage = 1;
                if (damage > hp) {
                    damage = hp;
                    dead = true;
                }
                if (dead) {
                    damage += absorbAmount;
                    if (damage > hp)
                        damage = hp;
                }
                hp -= damage;
                if (damage == 0) {
                    damageSplat = SplatType.BLOCK;
                }
                SplatNode node;
                totalDamage += damage;
                final int damage_ = damage;
                final boolean criticalHit = damage + (0.03 * damage) >= maxHit - absorbAmount;
                if (absorbSplat != null) {
                    node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.MAGIC, criticalHit, 0/*(int) splatDelay2*/)
                            , new Splat(victim, entity, (int) absorbAmount, absorbSplat, SplatCause.NONE, false, 0/*(int) splatDelay*/));
                } else
                    node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.MAGIC, criticalHit, 0/*(int) splatDelay2*/));
                if (hpDrainDelay < 0)
                    hpDrainDelay = 1;
                if (def.endGfx != -1 && damage > 0) {
                    if (def.combatType == CombatType.BARRAGE && def.elementType == ElementType.ICE && victim.getCombat().isFrozen())
                        victim.doGraphics(1677, (int) splatDelay, 50);
                    else
                        victim.doGraphics(def.endGfx, (int) splatDelay, def.endGfxHeight);
                } else if (def.endGfx != -1)
                    victim.doGraphics(85, (int) splatDelay, def.endGfxHeight);
                if (def.freezeTime != -1 && victim.getCombat().freezeImmunity != true && damage > 0)
                    victim.registerTick(Combat.resetFreezeTimerTick(victim, def.freezeTime));
                if (victim instanceof Player) {
                    PossesedItem defence = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_WEAPON);
                    PossesedItem shield = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_SHIELD);
                    if (shield != null) {
                        String name = shield.getDefinition().name;
                        if (name.contains("shield")) {
                            defenceAnim = 1156;
                        } else if (name.contains("defender")) {
                            defenceAnim = 4177;
                        } else {
                            defenceAnim = EquipmentDefinition.DEFAULT_ANIMATIONS.defendAnimation;
                        }
                    } else if (defence != null) {
                        defenceAnim = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_WEAPON)
                                .getDefinition().getEquipmentDefinition().getEquipmentAnimations().defendAnimation;
                    } else {
                        defenceAnim = EquipmentDefinition.DEFAULT_ANIMATIONS.defendAnimation;
                    }
                } else
                    defenceAnim = 0; // temporary

                victim.registerTick(new Tick(null, (int) hpDrainDelay) {

                    @Override
                    public boolean execute() {
                        victim.getCombat().executeAnimation(defenceAnim, 0, false, false);
                        return false;
                    }

                });
                victim.registerHPTick(new HPRemoval(victim, damage_, (int) hpDrainDelay + 1, node));
                player.getPrayerManager().dealHit(player, victim_, damage_, WeaponStyles.MAGIC, (int) splatDelay, (int) hpDrainDelay);
//                victim_.getPrayerManager().takeHit(victim_, player, damage_, WeaponStyles.MAGIC, (int) splatDelay, (int) hpDrainDelay); TODO add prayer support for npc's
            }
        }
        return totalDamage;
    }

    public int getMaxMagicHit(Player player, SpellDefinition def) {
        double baseHit = SpellDefinition.CombatType.getBaseMaxHit(player, def.combatType, def.elementType);
        double magicDamageBonus = 1 + player.getBonuses()[Bonuses.OFFENSIVE_MAGIC_DAMAGE] / 100;
        double boostedLevels = player.getLevels().getCurrentLevel(Levels.MAGIC) - player.getLevels().getLevel(Levels.MAGIC);
        if (boostedLevels < 0)
            boostedLevels = 0;
        double levelBoostMultiplier = 1 + 0.03 * boostedLevels;
        double maxHit = Math.floor(baseHit * magicDamageBonus);
        maxHit *= levelBoostMultiplier;
        return (int) Math.floor(maxHit);
    }

    public double getChance(double base, double addition, double include) {
        return ((base + (RANDOM.nextDouble() * addition)) / 10 + include);
    }

    public static boolean hasRunes(Player player, SpellDefinition definition) {
        for (PossesedItem i : definition.requiredRunes) {
            if (!wearsStaffForRune(player, i.getId()) && !player.getInventory().contains(i.getId(), i.getAmount()))
                return false;
        }
        return true;
    }

    public static int[] getStavesForRune(int runeId) {
        if (runeId == 554) {
            return new int[]{1387, 1401, 1393, 3053, 3054, 11738, 11736};
        } else if (runeId == 555) {
            return new int[]{1395, 6562, 1383, 6563, 1403, 11736, 11738};
        } else if (runeId == 556) {
            return new int[]{1405, 1381, 1397};
        } else if (runeId == 557) {
            return new int[]{1399, 3054, 3053, 1407, 1385};
        }
        return null;
    }

    public static boolean wearsStaffForRune(Player player, int runeId) {
        int[] staves = getStavesForRune(runeId);
        PossesedItem item = player.getEquipment().get(Equipment.WEAPON_SLOT);
        if (item == null)
            return false;
        if (staves != null) {
            for (int i : staves) {
                if (item.getId() == i)
                    return true;
            }
        }
        return false;
    }

    public static void removeRunes(Player player, SpellDefinition definition) {
        for (PossesedItem i : definition.requiredRunes) {
            if (!wearsStaffForRune(player, i.getId())) {
                player.getInventory().remove(i);
            }
        }
    }

    public static void switchMagic(Player player) {
        Magic magic = player.getCombat().getMagic();
        if (magic.getSpellBook() == SpellBook.MODERN) {
            magic.setSpellBook(SpellBook.ANCIENT);
            Static.proto.sendConfig(player, 439, 1025);
        } else {
            if (magic.getSpellBook() == SpellBook.ANCIENT) {
                magic.setSpellBook(SpellBook.LUNAR);
                Static.proto.sendConfig(player, 439, 1026);
            } else {
                magic.setSpellBook(SpellBook.MODERN);
                Static.proto.sendConfig(player, 439, 1024);
            }
        }
        if (player.getDisplayMode() == DisplayMode.FIXED) {
            Static.proto.sendInterface(player, SpellBook.interfaceForValue(magic.book), 548, 209, true);
            Static.proto.sendAccessMask(player, -1, -1, 548, 135, 0, 2);
        } else {
            Static.proto.sendInterface(player, SpellBook.interfaceForValue(magic.book), 746, 94, true);
            Static.proto.sendAccessMask(player, -1, -1, 746, 43, 0, 2);
        }
    }

    public static void setMagic(Player player, SpellBook book) {
        Magic magic = player.getCombat().getMagic();
        if (book == SpellBook.ANCIENT) {
            magic.setSpellBook(SpellBook.ANCIENT);
            Static.proto.sendConfig(player, 439, 1025);
        } else {
            if (book == SpellBook.LUNAR) {
                magic.setSpellBook(SpellBook.LUNAR);
                Static.proto.sendConfig(player, 439, 1026);
            } else {
                magic.setSpellBook(SpellBook.MODERN);
                Static.proto.sendConfig(player, 439, 1024);
            }
        }
        if (player.getDisplayMode() == DisplayMode.FIXED) {
            Static.proto.sendInterface(player, SpellBook.interfaceForValue(magic.book), 548, 209, true);
            Static.proto.sendAccessMask(player, -1, -1, 548, 135, 0, 2);
        } else {
            Static.proto.sendInterface(player, SpellBook.interfaceForValue(magic.book), 746, 94, true);
            Static.proto.sendAccessMask(player, -1, -1, 746, 43, 0, 2);
        }
    }

    public static void setMagic(Player player, int spellBookId) {
        player.getCombat().getMagic().book = SpellBook.getBookForValue(spellBookId);
    }

    public static void sendMagicInterface(Player player) {
        Magic magic = player.getCombat().getMagic();
        if (magic.getSpellBook() == SpellBook.MODERN) {
            Static.proto.sendConfig(player, 439, 1025);
        } else {
            if (magic.getSpellBook() == SpellBook.ANCIENT) {
                Static.proto.sendConfig(player, 439, 1026);
            } else {
                Static.proto.sendConfig(player, 439, 1024);
            }
        }
        if (player.getDisplayMode() == DisplayMode.FIXED) {
            Static.proto.sendInterface(player, SpellBook.interfaceForValue(magic.book), 548, 209, true);
            Static.proto.sendAccessMask(player, -1, -1, 548, 135, 0, 2);
        } else {
            Static.proto.sendInterface(player, SpellBook.interfaceForValue(magic.book), 746, 94, true);
            Static.proto.sendAccessMask(player, -1, -1, 746, 43, 0, 2);
        }
    }

    public boolean isAutocasting() {
        return autoCast.autoCast;
    }

    public SpellBook getSpellBook() {
        return book;
    }

    public void setSpellBook(SpellBook book) {
        this.book = book;
    }

    public void setCharged(boolean isCharged) {
        this.isCharged = isCharged;
    }

    public boolean isCharged() {
        return isCharged;
    }

    @SuppressWarnings("unchecked")
    public static final void load() throws IOException {
//		if (spells != null) {
//			throw new IllegalStateException("Spells were already loaded");
//		}
        try {
            spells = (Map<Integer, Map<Integer, SpellDefinition>>) Static.xml.readObject(Static.parseString("%WORK_DIR%/world/miscData/spells.xml"));
            LOGGER.info("Loaded " + spells.size() + " spellbook(s)");
            int spellsLoaded = 0;
            for (Map<Integer, SpellDefinition> m : spells.values()) {
                spellsLoaded += m.size();
            }
            LOGGER.info("Loaded " + spellsLoaded + " spell(s)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
