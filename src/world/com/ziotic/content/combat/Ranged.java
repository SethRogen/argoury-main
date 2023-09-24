package com.ziotic.content.combat;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.ziotic.Static;
import com.ziotic.Constants.Equipment;
import com.ziotic.content.combat.Combat.ActionType;
import com.ziotic.content.combat.definitions.AmmunitionDefinition;
import com.ziotic.content.combat.definitions.BowDefinition;
import com.ziotic.content.combat.misc.CombatUtilities;
import com.ziotic.content.combat.misc.CombatUtilities.Styles;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.HPHandlerTick.HPAddition;
import com.ziotic.logic.Entity;
import com.ziotic.logic.World;
import com.ziotic.logic.HPHandlerTick.HPRemoval;
import com.ziotic.logic.item.EquipmentDefinition;
import com.ziotic.logic.item.ItemDefinition;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.item.EquipmentDefinition.Bonuses;
import com.ziotic.logic.item.EquipmentDefinition.WeaponStyles;
import com.ziotic.logic.mask.Splat;
import com.ziotic.logic.mask.SplatNode;
import com.ziotic.logic.mask.Splat.SplatCause;
import com.ziotic.logic.mask.Splat.SplatType;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;

public class Ranged {
	
	private static final Random RANDOM = new Random();
	private static final Logger LOGGER = Logging.log();
	
	private AmmunitionDefinition primaryAmmunition;
	private BowDefinition currentBow;
	private boolean hasSupplies = false;
	
	private static Map<Integer, BowDefinition> bows = null;
	private static Map<Integer, AmmunitionDefinition> projectiles = null;	
	
	public static boolean shoot(Player player, Entity victim, ActionType type, boolean special) {
		if (canShoot(player, victim)) {
			PossesedItem attack = player.getEquipment().get(EquipmentDefinition.SLOT_WEAPON);
			if (special) {
				if (attack != null) {
					boolean b = Static.<Boolean>callScript("specialattacks.handleSpecial", player, victim, player.getCombat(), attack, type, 0);
					if (!b) {
						player.sendMessage("This weapon its special is not configured yet, don't use it.");
						player.sendMessage("Using its special in combat will stop your combat.");
					}
					return b;
				} else {
					player.sendMessage("An error occured with code [RASPECIAL-01] please report this error.");
					return false;
				}
			} else {
				AmmunitionDefinition ad = getPrimaryAmmunition(player);
				BowDefinition bd = getBow(player);
				Static.world.sendProjectile(player, victim, ad.projectileId, player.getLocation(), 
						victim.getLocation(), ad.startHeight, ad.endHeight, ad.projectileSpeed, ad.projectileDelay, ad.middleHeight, 0, player.getSize());
				double distance = player.getLocation().distance(victim.getLocation());
				double splatDelay = 0;
				double projDelay = ad.projectileDelay;
				double accuracy = 1;
				double strengthMultiplier = 1;
				boolean onyxBolt = false;
				switch (ad.ammunitionType) {
				case ARROW:
					player.doGraphics(ad.graphicsId, ad.graphicsDelay, ad.graphicsHeight);	
					splatDelay = ad.projectileDelay + ad.projectileSpeed + distance * 5;
					projDelay /= 2;
					if (player.getCombat().weapon.style == Styles.STYLE_RAPID) {
						accuracy = 0.8;
						strengthMultiplier = 1.1;
					} else if (player.getCombat().weapon.style == Styles.STYLE_ACCURATE) {
						accuracy = 1.1;
					} 
					break;
				case BOLT:
					accuracy = 1.0;
					splatDelay = ad.projectileDelay + ad.projectileSpeed + distance * 4;
					projDelay /= 2.4;
					if (ad.enchanted) {
						if (World.getRandom(7) == 0) {
							switch (ad.boltTip) {
							case DRAGON_STONE:
								accuracy = 1.3;
								strengthMultiplier = 1.45;
								if (victim instanceof Player) {
									Player victim_ = (Player) victim;
									PossesedItem item = victim_.getEquipment().get(Equipment.SHIELD_SLOT);
									if (item != null) {
										if (item.getId() == 11283 || item.getId() == 11284 || item.getId() == 1540) {
											strengthMultiplier = 1;
										} else {
											strengthMultiplier = /*2.335*/ 1.95 - ((Player) victim).getLevels().getCurrentLevel(Levels.DEFENCE) * 0.00894;
										}
									} else {
										strengthMultiplier = /*2.335*/ 1.95 - ((Player) victim).getLevels().getCurrentLevel(Levels.DEFENCE) * 0.00894;
									}
								}								
								break;
							case ONYX:
								accuracy = 1.5;
								strengthMultiplier = 1.3;
								onyxBolt = true;
								int damage = player.getCombat().hit(victim, type, false, strengthMultiplier, accuracy, null, 1, null, new int[] {(int) splatDelay}, new int[]{ (int) projDelay/*ad.projectileDelay / 2*/ });
								int healLife = (int) ((double)damage * 0.25d);
								double hpDrainDelay = splatDelay * 12d;
						    	hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
								player.registerHPTick(new HPAddition(player, healLife, (int) hpDrainDelay + 1, 0, null));
								break;
							}
							victim.getCombat().executeGraphics(ad.enchantedGraphics, 0, 0);
						}
					}
					break;
				}
				if (ad.inHand) {
					player.getCombat().executeAnimation(ad.animationId, ad.animationDelay, true, false);
				} else {
					player.getCombat().executeAnimation(bd.animationId, bd.animationDelay, true, false);
				}			
				if (!onyxBolt)
					player.getCombat().hit(victim, type, false, strengthMultiplier, accuracy, null, 1, null, new int[] {(int) splatDelay}, new int[]{ (int) projDelay/*ad.projectileDelay / 2*/ });
				
				return true;
			}	
		}		
		return false;
	}
	
	public static boolean canShoot(Player player, Entity victim) {
		// TODO projectile clipping
		if (!CombatUtilities.clippedProjectile(player, victim)) {
    		if (player.getCombat().isFrozen())
    			Static.proto.sendMessage(player, "A magical force stops you from moving.");
    		return false;
    	}
		if (!getHasSupplies(player)) {
			PossesedItem i = player.getEquipment().get(Equipment.WEAPON_SLOT);
			if (i != null) {
				ItemDefinition idef = i.getDefinition();
				if (idef != null) {
					EquipmentDefinition edef = idef.getEquipmentDefinition();
					if (edef.getWeaponStyles()[player.getCombat().weapon.index] == WeaponStyles.RANGED) {
						player.sendMessage("You don't have the right ammunition for this bow.");
					}
				}
			}
			return false;
		}
		
		if (!(player.getLevels().getLevel(Levels.RANGE) >= getPrimaryAmmunition(player).requiredLevel)) {
			Static.proto.sendMessage(player, "You don't have the required level to use this ranged projectile.");
			return false;
		}
		
		removeSupplies(player, 1); //May need tweaking.
		return true;
	}
	
	public static boolean hasSupplies(Player player) {
		AmmunitionDefinition pd;
		PossesedItem w = player.getEquipment().get(Equipment.WEAPON_SLOT);
		if (w != null) {
			pd = projectiles.get(w.getId());
			if (pd != null) {
				setPrimaryAmmunition(player, pd);
				setBow(player, null);
				return true;
			} else {
				PossesedItem a = player.getEquipment().get(Equipment.ARROWS_SLOT);
				if (a != null) {
					pd = projectiles.get(a.getId());
					if (pd != null) {
						setPrimaryAmmunition(player, pd);
						BowDefinition bd = bows.get(w.getId());
						if (bd != null) {
							for (int i : pd.requiredBows) {
								if (i == bd.id) {
									setBow(player, bd);
									return true;
								}
							}
							setBow(player, null);
							return false;
						} else {
							setBow(player, null);
							return false;
						}
					} else {
						setPrimaryAmmunition(player, null);
						return false;
					}	
				} else {
					setPrimaryAmmunition(player, null);
					setBow(player, null);
					return false;
				}
			}
		} else {
			setBow(player, null);
			PossesedItem a = player.getEquipment().get(Equipment.ARROWS_SLOT);
			if (a != null) {
				pd = projectiles.get(a.getId());
				if (pd != null) {
					setPrimaryAmmunition(player, pd);
				} else {
					setPrimaryAmmunition(player, null);
				}
			} else {
				setPrimaryAmmunition(player, null);
			}
			return false;
		}
	}
	
	public static void removeSupplies(Player player, int amount) { 
		PossesedItem a = player.getEquipment().get(Equipment.ARROWS_SLOT);
		player.getEquipment().remove(a.getId(), amount);
	}
	
	public static void setHasSupplies(Player player) {
		player.getCombat().ranged.hasSupplies = hasSupplies(player);
	}
	
	public static boolean getHasSupplies(Player player) {
		return player.getCombat().ranged.hasSupplies;
	}
	
	public static void setWeapon(Player player) {
		setHasSupplies(player);
	}
	
	public static AmmunitionDefinition getPrimaryAmmunition(Player player) {
		return player.getCombat().ranged.primaryAmmunition;
	}
	
	private static void setPrimaryAmmunition(Player player, AmmunitionDefinition pd) {
		player.getCombat().ranged.primaryAmmunition = pd;
	}
	
	private static BowDefinition getBow(Player player) {
		return player.getCombat().ranged.currentBow;
	}
	
	private static void setBow(Player player, BowDefinition bd) {
		player.getCombat().ranged.currentBow = bd;
	}

	@SuppressWarnings("unchecked")
	public static final void load() throws IOException {
		if (bows != null) {
			throw new IllegalStateException("Bows were already loaded");
		}
		if (projectiles != null) {
			throw new IllegalStateException("Ammunitions were already loaded");
		}
		try {
			bows = (Map<Integer, BowDefinition>) Static.xml.readObject(Static.parseString("%WORK_DIR%/world/itemData/rangeData/bows.xml"));
			projectiles = (Map<Integer, AmmunitionDefinition>) Static.xml.readObject(Static.parseString("%WORK_DIR%/world/itemData/rangeData/ammunitions.xml"));
			LOGGER.info("Loaded " + bows.size() + " bow(s)");
			LOGGER.info("Loaded " + projectiles.size() + " projectile(s)");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public int doRangedHit(final Entity entity, final Entity victim, final boolean special, 
			final double strengthMultiplier, final double accuracyMultiplier, final int amountOfHits, final int[] damages, final int[] delays, final int[] splatDelays) {
		int hp = victim.getHP();
		int totalDamage = 0;
		for (int i = 0; i < amountOfHits; i++) {
			Player player = (Player) entity;
			double maxHit = getMaxRangedHit(entity, strengthMultiplier);
			double hitChance = 0;
			SplatType damageSplat = SplatType.DAMAGE;
			SplatType absorbSplat = null;
			double splatDelay = delays[i];
	    	double hpDrainDelay = splatDelay * 12d;
	    	hpDrainDelay = Math.ceil((hpDrainDelay / 600d * 1000d) / 1000d);
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
                int damage = maxHit > 0 ? (damages == null ? ((int) (maxHit * hitMultiplier)) : damages[i]) : 0;
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
                final int damage_ = damage;
                final boolean criticalHit = damage + (0.03 * damage) >= maxHit - absorbAmount;
				if (absorbSplat != null) {
					node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.RANGE, criticalHit, 0/*(int) splatDelay2*/)
						, new Splat(victim, entity, (int) absorbAmount, absorbSplat, SplatCause.NONE, false, 0/*(int) splatDelay*/));
				} else 
					node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.RANGE, criticalHit, 0/*(int) splatDelay2*/));
		    	if (hpDrainDelay < 0)
		    		hpDrainDelay = 1;
		    	PossesedItem defence = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_WEAPON);
		    	PossesedItem shield = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_SHIELD);
		    	final int defenceAnim;
				if (shield != null) {
					String name = shield.getDefinition().name;
					if (name.contains("shield")) {
						defenceAnim = 1156;
					} else if (name.contains("defender")) {
						defenceAnim = 4177;
					} else if (defence != null) {
						defenceAnim = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_WEAPON)
						.getDefinition().getEquipmentDefinition().getEquipmentAnimations().defendAnimation;
					} else {
						defenceAnim = EquipmentDefinition.DEFAULT_ANIMATIONS.defendAnimation;
					}
				} else if (defence != null) {
					defenceAnim = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_WEAPON)
					.getDefinition().getEquipmentDefinition().getEquipmentAnimations().defendAnimation;
				} else {
					defenceAnim = EquipmentDefinition.DEFAULT_ANIMATIONS.defendAnimation;
				}		    	
		    	victim.registerTick(new Tick(null, (int) hpDrainDelay) {

					@Override
					public boolean execute() {
				    	victim.getCombat().executeAnimation(defenceAnim, 0, false, false);
						return false;
					}
		    		
		    	});
				victim.registerHPTick(new HPRemoval(victim, damage, (int) hpDrainDelay + 1, node));
				totalDamage += damage_;
				player.getPrayerManager().dealHit(player, victim_, damage_, WeaponStyles.RANGED, (int) splatDelay, (int) hpDrainDelay);
				victim_.getPrayerManager().takeHit(victim_, player, damage_, WeaponStyles.RANGED, (int) splatDelay, (int) hpDrainDelay);
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
                int damage = maxHit > 0 ? (damages == null ? ((int) (maxHit * hitMultiplier)) : damages[i]) : 0;
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
                final int damage_ = damage;
                final boolean criticalHit = damage + (0.03 * damage) >= maxHit - absorbAmount;
				if (absorbSplat != null) {
					node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.RANGE, criticalHit, 0/*(int) splatDelay2*/)
						, new Splat(victim, entity, (int) absorbAmount, absorbSplat, SplatCause.NONE, false, 0/*(int) splatDelay*/));
				} else 
					node = new SplatNode(new Splat(victim, entity, damage_, damageSplat, SplatCause.RANGE, criticalHit, 0/*(int) splatDelay2*/));
		    	if (hpDrainDelay < 0)
		    		hpDrainDelay = 1;
		    	final int defenceAnim;
		    	if (victim instanceof Player) {
		    		Player target = (Player) victim;
		    		PossesedItem defence = player.getEquipment().get(EquipmentDefinition.SLOT_WEAPON);
		    		PossesedItem shield = player.getEquipment().get(EquipmentDefinition.SLOT_SHIELD);
		    		
					if (shield != null) {
						String name = shield.getDefinition().name;
						if (name.contains("shield")) {
							defenceAnim = 1156;
						} else if (name.contains("defender")) {
							defenceAnim = 4177;
						} else if (defence != null) {
							defenceAnim = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_WEAPON)
							.getDefinition().getEquipmentDefinition().getEquipmentAnimations().defendAnimation;
						} else {
							defenceAnim = EquipmentDefinition.DEFAULT_ANIMATIONS.defendAnimation;
						}
					} else if (defence != null) {
						defenceAnim = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_WEAPON)
						.getDefinition().getEquipmentDefinition().getEquipmentAnimations().defendAnimation;
					} else {
						defenceAnim = EquipmentDefinition.DEFAULT_ANIMATIONS.defendAnimation;
					}	
			    	victim.registerTick(new Tick(null, (int) hpDrainDelay) {
						@Override
						public boolean execute() {
					    	victim.getCombat().executeAnimation(defenceAnim, 0, false, false);
							return false;
						}
			    		
			    	});
		    	}	
				victim.registerHPTick(new HPRemoval(victim, damage, (int) hpDrainDelay + 1, node));
				totalDamage += damage_;
				player.getPrayerManager().dealHit(player, victim_, damage_, WeaponStyles.RANGED, (int) splatDelay, (int) hpDrainDelay);
//				victim_.getPrayerManager().takeHit(victim_, player, damage_, WeaponStyles.RANGED, (int) splatDelay, (int) hpDrainDelay); TODO add prayer support for npc's
			}
		}
		return totalDamage;
	}
	
	public int getMaxRangedHit(Entity entity, double specialStrengthMultiplier) {
		Player player = (Player) entity;
		double strengthMultiplier = 1;
		strengthMultiplier += player.getPrayerManager().getRangedMultiplier() - 1;
		double effectiveStrength = (double)player.getLevels().getCurrentLevel(Levels.RANGE) * strengthMultiplier;
		effectiveStrength = Math.round(effectiveStrength);
		if (player.getCombat().weapon.style == Styles.STYLE_ACCURATE)
			effectiveStrength += 3;
		// TODO if wearing void knight ranged set effectiveStrength * 1.1, round down and do again
		double baseDamage = 5 + effectiveStrength * (1d + (double)player.getBonuses()[Bonuses.OFFENSIVE_RANGED_STRENGTH] / 64d);
		return (int)Math.floor(Math.round(baseDamage) * specialStrengthMultiplier);
		
	}
	
	public double getChance(double base, double addition, double include) {
		return ((base + (RANDOM.nextDouble() * addition)) / 10 + include);
	}

}
