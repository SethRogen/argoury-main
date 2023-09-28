package com.ziotic.content.combat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.apache.log4j.Logger;

import com.ziotic.Constants.Equipment;
import com.ziotic.Static;
import com.ziotic.content.combat.Magic.SpellBook;
import com.ziotic.content.combat.actions.NPCCombatAction;
import com.ziotic.content.combat.actions.PlayerMagicAction;
import com.ziotic.content.combat.actions.PlayerMeleeAction;
import com.ziotic.content.combat.actions.PlayerRangedAction;
import com.ziotic.content.combat.definitions.AmmunitionDefinition;
import com.ziotic.content.combat.definitions.SpellDefinition;
import com.ziotic.content.combat.misc.CombatUtilities;
import com.ziotic.content.combat.misc.CombatUtilities.SpecialEnergy;
import com.ziotic.content.combat.misc.CombatUtilities.Styles;
import com.ziotic.content.combat.misc.CombatUtilities.Weapon;
import com.ziotic.content.combat.misc.HitRegisterManager.HitRegister;
import com.ziotic.content.combat.misc.SkullRegisterManager;
import com.ziotic.content.combat.ticks.NPCCombatHandlerTick;
import com.ziotic.content.combat.ticks.PlayerCombatHandlerTick;
import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.content.handler.ButtonHandler;
import com.ziotic.engine.tick.Tick;
import com.ziotic.engine.tick.Tick.TickPolicy;
import com.ziotic.logic.Entity;
import com.ziotic.logic.HPHandlerTick.HPRemoval;
import com.ziotic.logic.item.EquipmentDefinition;
import com.ziotic.logic.item.EquipmentDefinition.Bonuses;
import com.ziotic.logic.item.EquipmentDefinition.EquipmentType;
import com.ziotic.logic.item.EquipmentDefinition.WeaponStyles;
import com.ziotic.logic.item.Item;
import com.ziotic.logic.item.ItemContainer;
import com.ziotic.logic.item.ItemDefinition;
import com.ziotic.logic.item.ItemsOnDeathManager;
import com.ziotic.logic.item.ItemsOnDeathManager.ReturnType;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.map.Areas;
import com.ziotic.logic.map.PathProcessor;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.map.pf.astar.AStarPathFinder;
import com.ziotic.logic.mask.Splat;
import com.ziotic.logic.mask.Splat.SplatCause;
import com.ziotic.logic.mask.Splat.SplatType;
import com.ziotic.logic.mask.SplatNode;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.npc.NPCDefinition;
import com.ziotic.logic.npc.NPC.MovementType;
import com.ziotic.logic.npc.NPCDefinitionsLoader.AttackType;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;

public class Combat implements ButtonHandler {

	private static final Random RANDOM = new Random();
	private static final Logger LOGGER = Logging.log();	
	protected Entity entity;
	
	public Entity lastVictim;
	public Entity lastAttacker;
	
	protected long lastAttackTime = 0;
	protected long lastDefenceTime = 0;
	protected long nextAttackTime = 0;
	protected long nextSpellTime = 0;
	
	protected boolean autoRetaliate = false;
	public boolean specialOn = false;
	private boolean isFrozen = false;
	protected boolean freezeImmunity = false;
	protected boolean priorityAnim = false;
	protected boolean eatingAnim = false;
	protected boolean inWilderness = false;
	public boolean attackFromInitiateDistance = false;
	
	public Weapon weapon = new Weapon(0, 0);
	public SpecialEnergy specialEnergy;
	
	protected int attackAnimation = -1;
	public CombatAction scheduledAction;
	
	protected Melee melee = new Melee();
	private Magic magic = new Magic();
	protected Ranged ranged = new Ranged();
	
    private static final int[][] WAYPOINTS = new int[][]{{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
	
    /**
     * Created for the action handler loader
     */
    public Combat() { }
    
	public Combat(Entity entity) {
		this.entity = entity;
		if (entity instanceof Player) 
			specialEnergy = new SpecialEnergy((Player) entity);
	}
	
	/**
	 * This method initiates player to entity combat.
	 * @param player The player initiating the combat.
	 * @param victim The approached victim.
	 */
	public void createNewCombatAction(final Player player, final Entity victim, boolean buttonMagic, int interfaceId, int spellId) {
		try {
			player.faceEntity(victim);
//			if (playerCanAttack(victim)) {
				scheduledAction = getNextCombatAction(player, victim, false, buttonMagic, Magic.getDefinition(player, interfaceId, spellId, false));
				entity.registerTick(new PlayerCombatHandlerTick(this));
				if (scheduledAction != null) {
					if (!CombatUtilities.inCorrectPosition(player, victim, scheduledAction.type) && !isFrozen)
						Static.world.submitPath(new AStarPathFinder(), entity, victim.getX(), victim.getY(), victim, PathProcessor.MOVE_SPEED_ANY, true, null);
					else if (isFrozen) {
						Static.proto.sendMessage(player, "A magical force stops you from moving.");
					}
				} else {
					LOGGER.info("Nulled scheduled combat action (PLAYER)");
				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method initiates NPC to entity combat.
	 * @param npc The npc initiating the combat.
	 * @param victim The approached victim.
	 */
	public void createNewCombatAction(final NPC npc, final Entity victim) {
		try {
			npc.faceEntity(victim);
//			if (playerCanAttack(victim)) {
				scheduledAction = getNextCombatAction(npc, victim);
				if (scheduledAction == null)
					LOGGER.info("Nulled scheduled combat action (NPC)");
				LOGGER.info("Scheduled new combat action");
				entity.registerTick(new NPCCombatHandlerTick(this));
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prepares a new combat action.
	 * @param player
	 * @param victim
	 * @param calledFromExisting This boolean says whether this method is called from within a current action or is
	 * a new action
	 * @param buttonMagic This boolean says whether this method is called because of clicking a spell
	 * @param consuming This boolean says whether this action is an consuming action
	 * @return
	 */
	public CombatAction getNextCombatAction(Player player, Entity victim, boolean calledFromExisting, boolean buttonMagic, SpellDefinition spell) {
		CombatAction ca = null;
		if (victim instanceof NPC) {
			NPC npc = (NPC) victim;
			NPCDefinition def = npc.getDefinition();
			if (!def.attackable) {
				player.sendMessage("You can't attack this npc (yet).");
				return null;
			}
		}
		if (buttonMagic) {
			if (!matchesCurrentAction(ActionType.MAGIC)) {
				if (spell == null)
					player.sendMessage("Spell is not supported yet.");
				else 
					ca = new PlayerMagicAction(player, victim, false, spell.attackDelay, spell.spellDelay, spell); 
			} else {
				if (calledFromExisting) {
					ca = new PlayerMagicAction(player, victim, false, spell.attackDelay, spell.spellDelay, spell);
				} else {
					ca = new PlayerMagicAction(player, victim, false, spell.attackDelay, spell.spellDelay, spell);
					((PlayerMagicAction) scheduledAction).queuedSpell = spell;
				}
			}
			return ca;
		} else if (magic.isAutocasting()) {
			SpellDefinition def = magic.getAutoCastDefinition();
			if (def == null) 
				player.sendMessage("Autocasting this spell is not supported yet.");
			else {
				if (calledFromExisting) {
					ca = new PlayerMagicAction(player, victim, true, def.attackDelay, def.spellDelay, def);
				} else {
					ca = new PlayerMagicAction(player, victim, true, def.attackDelay, def.spellDelay, def); // create a new one
				}
				return ca;
			}
		} else {
			WeaponStyles style;
			PossesedItem i = player.getEquipment().get(Equipment.WEAPON_SLOT);
			int delay = 5;
			if (i == null)
				style = WeaponStyles.MELEE;
			else {
				ItemDefinition idef = i.getDefinition();
				if (idef == null) 
					style = WeaponStyles.MELEE;
				else {
					EquipmentDefinition edef = idef.getEquipmentDefinition();
					if (edef == null)
						style = WeaponStyles.MELEE;
					else {
						style = edef.getWeaponStyles()[weapon.index];
						delay = edef.getSpeed();
					}
				}
			}
			switch (style) {
			case MELEE:
				if (!matchesCurrentAction(ActionType.MELEE)) 
					ca = new PlayerMeleeAction(player, victim, true, delay, delay);
				else
					ca = new PlayerMeleeAction(player, victim, true, delay, delay);
				break;
			case MAGIC:
				LOGGER.error("[Unexpected usage of magic]", new Exception());
				break;
			case RANGED:
				if (weapon.style == Styles.STYLE_RAPID)
					delay -= 1;
				if (!matchesCurrentAction(ActionType.RANGED))
					ca = new PlayerRangedAction(player, victim, true, delay, delay);
				else
					ca = new PlayerRangedAction(player, victim, true, delay, delay);
				break;
				default:
					LOGGER.error("[Unexpected usage of combat]", new Exception());
					break;
			}
		}
		return ca;
	}
	
	/**
	 * Only used once upon every combat initiation.
	 * @param npc
	 * @param victim
	 * @return
	 */
	public CombatAction getNextCombatAction(NPC npc, Entity victim) {
		NPCDefinition def = npc.getDefinition();
		int attackDelay = 5;
		ActionType type;
		if (def != null) {
			attackDelay = def.attackSpeed;
			type = getNPCActionType(def);
		} else {
			type = ActionType.MELEE;
		}
		return new NPCCombatAction(npc, victim, attackDelay, type);
	}
	
	private ActionType getNPCActionType(NPCDefinition def) {
		AttackType type = def.attackType;
		if (type.usesMelee) 
			return ActionType.MELEE;
		if (type.usesMagic)
			return ActionType.MAGIC;
		if (type.usesRanged)
			return ActionType.RANGED;
		else
			return ActionType.OTHER;
	}
	
	/**
	 * 
	 * @param victim The victim to hit.
	 * @param delays The delays for when to do the hit.
	 * @param special Whether the attack is a special attack.
	 * @param specialStrengthMultiplier Needs to be 1 if special if false.
	 */
	public int hit(final Entity victim, ActionType type, final boolean special, final double specialStrengthMultiplier,
			final double specialAccuracyMultiplier, final SpellDefinition spellDefinition, final int amountOfHits, final int[] damages, final int[] delays, final int[] splatDelays) {
		entity.faceEntity(victim);
		if (entity instanceof Player) {
			double autoRetaliateDelay = 1;
			double distance = entity.getLocation().distance(victim.getLocation());
			final Player attacker = (Player) entity;
			double damage = 0;
			double attackXp = 0;
			double strengthXp = 0;
			double defenceXp = 0;
			double magicXp = 0;
			double rangedXp = 0;
			double constitutionXp = 0;
			switch (type) {
			case RANGED:				
				damage = (double) ranged.doRangedHit(entity, victim, special, specialStrengthMultiplier, specialAccuracyMultiplier, amountOfHits, damages, delays, splatDelays);
				AmmunitionDefinition ad = Ranged.getPrimaryAmmunition(attacker);
				if (damage > 0) {
					constitutionXp = (int) Math.floor((double) damage * 1.33d);
					attacker.getLevels().addXP(Levels.CONSTITUTION, constitutionXp);
					if (weapon.style == Styles.STYLE_LONG_RANGE) {
						rangedXp = damage * 2;
						defenceXp = damage * 2;
						attacker.getLevels().addXP(Levels.DEFENCE, defenceXp);
					} else {
						rangedXp = damage * 4;
					}
					attacker.getLevels().addXP(Levels.RANGE, rangedXp);
					attacker.updateXPCounter();
					if (victim.getCombat().getMagic().vengeanceCasted) {
						double splatDelay = ad.projectileDelay / 2;						
						double hitDelay = ad.projectileDelay + ad.projectileSpeed + distance * 5;
				    	hitDelay = hitDelay * 12d;
				    	hitDelay = Math.ceil((hitDelay / 600d * 1000d) / 1000d);
						doVengeance((Player) victim, entity, (int) damage, (int) hitDelay, (int) splatDelay, type);
					}
				}
				
				if (ad != null) {
					double hitDelay = ad.projectileDelay + ad.projectileSpeed + distance * 5;
			    	autoRetaliateDelay = hitDelay * 12d;
			    	autoRetaliateDelay = Math.floor(Math.floor(autoRetaliateDelay / 600d))  + 1;
				}
				break;
			case MELEE:
				damage = (double) melee.doMeleeHit(entity, victim, special, specialStrengthMultiplier, specialAccuracyMultiplier, amountOfHits, damages, delays);
				if (damage > 0) {
					constitutionXp = (int) Math.floor((double) damage * 1.33d);
					attacker.getLevels().addXP(Levels.CONSTITUTION, constitutionXp);
					switch (weapon.style) {
					case Styles.STYLE_ACCURATE:
						attackXp = damage * 4;
						attacker.getLevels().addXP(Levels.ATTACK, attackXp);
						break;
					case Styles.STYLE_AGGRESSIVE:
						strengthXp = damage * 4;
						attacker.getLevels().addXP(Levels.STRENGTH, strengthXp);
						break;
					case Styles.STYLE_DEFENSIVE:
						defenceXp = damage * 4;
						attacker.getLevels().addXP(Levels.DEFENCE, defenceXp);
						break;
					case Styles.STYLE_CONTROLLED:
						attackXp = (int) Math.floor((double) damage * 1.33d);
						strengthXp = (int) Math.floor((double) damage * 1.33d);
						defenceXp = (int) Math.floor((double) damage * 1.33d);
						attacker.getLevels().addXP(Levels.ATTACK, attackXp);
						attacker.getLevels().addXP(Levels.STRENGTH, strengthXp);
						attacker.getLevels().addXP(Levels.DEFENCE, defenceXp);
						break;
					}
					attacker.updateXPCounter();
					if (victim.getCombat().getMagic().vengeanceCasted) {
						doVengeance((Player) victim, entity, (int) damage, 1, 0, type);
					}
				}
				autoRetaliateDelay = ((distance * 300d) / 600d) + 1;
				break;
			case MAGIC:
				if (spellDefinition != null) {
					damage = (double) magic.doMagicHit(entity, victim, spellDefinition, specialAccuracyMultiplier, damages, delays);
					if (damage > 0) {						
						if (victim.getCombat().getMagic().vengeanceCasted) {						
							double splatDelay = spellDefinition.projDelay / 2;						
							double hitDelay = spellDefinition.projDelay + spellDefinition.projSpeed + distance * 5;
					    	hitDelay = hitDelay * 12d;
					    	hitDelay = Math.ceil((hitDelay / 600d * 1000d) / 1000d);
							doVengeance((Player) victim, entity, (int) damage, (int) hitDelay, (int) splatDelay, type);
						}
					}
					magicXp = spellDefinition.xp + (double) damage * 0.2;
					constitutionXp = (double) damage * 0.133;
					attacker.getLevels().addXP(Levels.CONSTITUTION, constitutionXp);
					attacker.getLevels().addXP(Levels.MAGIC, magicXp);
					attacker.updateXPCounter();
					double hitDelay = spellDefinition.projDelay + spellDefinition.projSpeed + distance * 5;
				    autoRetaliateDelay = hitDelay * 12d;
				    autoRetaliateDelay = Math.floor(Math.floor(autoRetaliateDelay / 600d)) + 1;
				}
				break;			
			}
			if (victim instanceof Player) {
				SkullRegisterManager.handleSkulling((Player) entity, (Player) victim);
				if (victim.getCombat().autoRetaliate) {
					victim.registerTick(new Tick("AutoRetaliate", (int) autoRetaliateDelay) {
						@Override
						public boolean execute() {
							Magic magic = victim.getCombat().magic;
							magic.getSpellBook();
							victim.getCombat().createNewCombatAction((Player) victim, (Entity) attacker, false, SpellBook.interfaceForValue(magic.book), magic.getAutoCastSpellId());
							return false;
						}
					});
				}
			} else {
				victim.registerTick(new Tick("AutoRetaliate", (int) autoRetaliateDelay) {
					@Override
					public boolean execute() {
						victim.getCombat().createNewCombatAction((NPC) victim, (Entity) attacker);
						return false;
					}
				});
			}
			victim.hitRegisterManager.registerHit(attacker, (int) damage);
            return (int) (damage);
		} else {
			double autoRetaliateDelay = 1;
			double distance = entity.getLocation().distance(victim.getLocation());
			switch (type) {
			case MELEE:
				LOGGER.info("hit method melee call");
				melee.doMeleeHit(entity, victim, special, specialStrengthMultiplier, specialAccuracyMultiplier, amountOfHits, damages, delays);
				autoRetaliateDelay = ((distance * 300d) / 600d) + 1;
				break;
			}
			if (victim instanceof Player) {
				if (victim.getCombat().autoRetaliate) {
					victim.registerTick(new Tick("AutoRetaliate", (int) autoRetaliateDelay) {
						@Override
						public boolean execute() {
							Magic magic = victim.getCombat().magic;
							magic.getSpellBook();
							victim.getCombat().createNewCombatAction((Player) victim, (Entity) entity, false, SpellBook.interfaceForValue(magic.book), magic.getAutoCastSpellId());
							return false;
						}
					});
				}
			} else {
				victim.registerTick(new Tick("AutoRetaliate", (int) autoRetaliateDelay) {
					@Override
					public boolean execute() {
						victim.getCombat().createNewCombatAction((NPC) victim, entity);
						return false;
					}
				});
			}
		}
        return 0;
	}
	
	public static void doVengeance(final Player player, Entity victim, int damage, int delay, int splatDelay, ActionType type) {
		double damage_;
		SplatNode node;
		switch (type) {
		case MELEE:
			damage_ = Math.round((double) damage * 0.75d);
			node = new SplatNode(new Splat(victim, player, (int) damage_, SplatType.DAMAGE, SplatCause.NONE, false, 0));
			player.doForceChat("Taste Vengeance!");
			victim.registerHPTick(new HPRemoval(victim, (int) damage_, delay, node));
			player.registerTick(new Tick(null, 1, TickPolicy.PERSISTENT) {
				@Override
				public boolean execute() {
					player.getCombat().getMagic().vengeanceCasted = false;
					return false;
				}
			});
			break;
		case MAGIC:
		case RANGED:
			damage_ = Math.round((double) damage * 0.75d);
			node = new SplatNode(new Splat(victim, player, (int) damage_, SplatType.DAMAGE, SplatCause.NONE, false, splatDelay));
			victim.registerHPTick(new HPRemoval(victim, (int) damage_, delay, node));
			player.registerTick(new Tick(null, delay, TickPolicy.PERSISTENT) {
				@Override
				public boolean execute() {
					player.getCombat().getMagic().vengeanceCasted = false;
					player.doForceChat("Taste Vengeance!");
					return false;
				}
			});
			break;
		}
	}
	
	/**
	 * This boolean determines whether two CombatAction are from the same type.
	 * @param type
	 * @return
	 */
	public boolean matchesCurrentAction(ActionType type) {
		if (scheduledAction == null)
			return false;
		return type == scheduledAction.type;
	}
	
	public void appendAttackTime() {
		lastAttackTime = System.currentTimeMillis();
	}
	
	public void appendAttackTime(long time, boolean addOnly) {
		if (addOnly)
			lastAttackTime += time;
		else
			lastAttackTime = System.currentTimeMillis() + time;
	}
	
	public void appendDefenceTime() {
		lastDefenceTime = System.currentTimeMillis();
	}
	
	public void appendDefenceTime(long time, boolean addOnly) {
		if (addOnly)
			lastDefenceTime += time;
		else
			lastDefenceTime = System.currentTimeMillis() + time;
	}
	
	public void setNextAttackTime(int delay) {
		nextAttackTime = Static.world.getTime() + delay;
	}
	
	public void setNextSpellTime(int delay) {
		nextSpellTime = Static.world.getTime() + delay;
	}
	
	public void addNextAttackTime(int delay) {
		nextAttackTime += delay;
	}
	
	public void addNextSpellTime(int delay) {
		nextSpellTime += delay;
	}
	
	public boolean isReadyToAttack(int attackDelay, int spellDelay, ActionType type) { 
		switch (type) {
		case MELEE:
		case RANGED:
			return nextAttackTime <= Static.world.getTime();
		case MAGIC:
			return nextSpellTime <= Static.world.getTime();
		}
		return false;
	}
	
	public boolean inCombat() {
		return lastDefenceTime + 10000 > System.currentTimeMillis() && lastAttackTime + 10000 > System.currentTimeMillis();
	}
	
	public boolean isAttacking() {
		return lastAttackTime + 5000 > System.currentTimeMillis();
	}
	
	public boolean canLogout() {
		return lastDefenceTime + 10000 < System.currentTimeMillis();
	}
	
	public boolean underAttack() {
		return lastDefenceTime + 6000 > System.currentTimeMillis();
	}
	
	public boolean underAttackBy(Entity entity) {
		return entity == lastAttacker;
	}
	
    public static boolean isInPVPZone(Player player) {
        return Areas.WILDERNESS.inArea(player.getLocation());
    }
    
	public static boolean isInMultiZone(Player player) {
		return Areas.MULTI.inArea(player.getLocation());
	}

    public static void updatePVPStatus(Player player) {
        Static.proto.sendPlayerOption(player, player.isInPVP() ? "Attack" : "null", 1, true);
    }
    
	public boolean isInWilderness() {
		return inWilderness;
	}

	public void setInWilderness(boolean inWilderness) {
		this.inWilderness = inWilderness;
	}
	
	/**
	 * This method handles the logic of whether you are able to attack another player
	 * based on multi-zones and in combat statuses.
	 * @param victim
	 * @return
	 */
	public boolean playerCanAttack(Entity victim) {
		// TODO multi-zones
		if (victim.isInMulti()) {
			return true;
		} else {		
			if (!underAttack()) {
				if (victim.getCombat().underAttack()) {
					if (victim.getCombat().underAttackBy(this.entity) /*&& victim.getCombat().getLastAttacker() == entity*/) // TODO change the attack initiation to fix 2 hit bug 
						return true;
					else {
						if (entity instanceof Player)
							((Player) entity).sendMessage("The other player is already under attack.");
						return false;
					}
				} else {
					return true;
				}
			} else {
				if (underAttackBy(victim))
					return true;
				else {
					if (entity instanceof Player)
						((Player) entity).sendMessage("You are already under attack.");
					return false;
				}
			}
		}
	}
	
	public boolean npcCanAttack(Entity victim) {
		if (victim.isInMulti()) {
			return true;
		} else {		
			if (!underAttack()) {
				if (victim.getCombat().lastDefenceTime + 4200 > System.currentTimeMillis()) {
					if (victim.getCombat().underAttackBy(this.entity) /*&& victim.getCombat().getLastAttacker() == entity*/) // TODO change the attack initiation to fix 2 hit bug 
						return true;
					else {
						return false;
					}
				} else {
					return true;
				}
			} else {
				if (underAttackBy(victim))
					return true;
				else {
					return false;
				}
			}
		}
	}
	
	public static Tick resetFreezeImmunityTick(final Entity entity) {
		double delay = 1200 + RANDOM.nextInt(1200);
		double cycles = Math.ceil(delay / 600d);
		Tick tick = new Tick("FreezeImmunityTimer", (int) cycles) {
			@Override
			public boolean execute() {
				entity.getCombat().freezeImmunity = false;
				return false;
			}
		};
		return tick;
	}
	
	public static Tick resetFreezeTimerTick(final Entity entity, double delay) {
		boolean delayed = false;
		if (entity.getCombat().attackFromInitiateDistance) {
			delayed = true;
			entity.registerTick(new Tick(null, 1) {
				@Override
				public boolean execute() {
					if (entity instanceof Player)
						Static.proto.sendMessage((Player) entity, "You have been frozen.");
					entity.getCombat().isFrozen = true;
					entity.getCombat().freezeImmunity = true;	
					entity.cancelTick("event");
			        entity.getPathProcessor().reset();
			        entity.subResetEvents();
					return false;
				}
			});
		} else {
			if (entity instanceof Player)
				Static.proto.sendMessage((Player) entity, "You have been frozen.");
			entity.getCombat().isFrozen = true;
			entity.getCombat().freezeImmunity = true;	
			entity.cancelTick("event");
	        entity.getPathProcessor().reset();
	        entity.subResetEvents();
		}
		double cycles = Math.ceil(((delay / 600d) * 1000d) / 1000d);
		Tick tick = new Tick("FreezeTimer", (int) cycles + (delayed ? 1 : 0)) {
			@Override
			public boolean execute() {
				entity.getCombat().isFrozen = false;
				entity.registerTick(resetFreezeImmunityTick(entity));
				return false;
			}
		};
		return tick;
	}
	
	public static HashMap<Long, Entity> getMultiVictims(Entity attacker, Entity victim) {
		HashMap<Long, Entity> victims = new HashMap<Long, Entity>();
		Tile loc = victim.getLocation();
		victims.put(victim.getIdentifier(), victim);
		if (loc.containsNPCs()) {
			for (NPC n : loc.getNPCs()) {
				if (n.isInMulti())
					victims.put(n.getIdentifier(), n);
			}
		}
		if (loc.containsPlayers()) {
			for (Player p : loc.getPlayers()) {
				if (p.isInPVP() && p.isInMulti())
					victims.put(p.getIdentifier(), p);
			}
		}
		for (int[] waypoint : WAYPOINTS) {
			Tile loc_ = Tile.locate(loc.getX() + waypoint[0], loc.getY() + waypoint[1], loc.getZ());
			if (loc_.containsNPCs()) {
				for (NPC n : loc_.getNPCs()) {
					if (n.isInMulti())
						victims.put(n.getIdentifier(), n);
				}
			}
			if (loc_.containsPlayers()) {
				for (Player p : loc_.getPlayers()) {
					if (p.isInPVP() && p.isInMulti()) 
						victims.put(p.getIdentifier(), p);
				}
			}
		}
		victims.remove(attacker.getIdentifier());
		victims.remove(victim.getIdentifier());
		return victims;
	}
	
	public void executeAnimation(int animId, int animDelay, boolean priority, boolean eating) {
		if (eating && !priorityAnim) {
			entity.doAnimation(animId, animDelay);
			entity.registerTick(new Tick(null, 2) {
                @Override
                public boolean execute() {
                    eatingAnim = false;
                    return false;
                }
            });
			eatingAnim = true;
		} else if (priority/* || eating*/) {
			entity.doAnimation(animId, animDelay);
			entity.registerTick(new Tick(null, 2) {
                @Override
                public boolean execute() {
                    priorityAnim = false;
                    return false;
                }
            });
			priorityAnim = true;
		} else if (!priorityAnim && !eatingAnim/* && !priority*/) {
			entity.doAnimation(animId, animDelay);
		}
	}
	
	public void executeGraphics(int gfxId, int gfxDelay, int gfxHeight) {
		System.out.println("Special effect GFX ID: " + gfxId);
		entity.doGraphics(gfxId, gfxDelay, gfxHeight);
	}
	
	public void uponDeath1() {
		entity.registerTick(new Tick("IOD", 4) {
			@Override
			public boolean execute() {
				try {
					/**
					 * NPC Drops/Loot
					 * TODO:
					 */
					if (entity instanceof NPC ) { 
						Player killer = null;
						int j = 0;
						HitRegister[] register = entity.hitRegisterManager.getSortedHitRegisters();
						while (killer == null) {
							killer = register[j++].player;
							if (j >= register.length)
								break;
						}
						//Just have bones drop right now.
						
						Static.world.getGroundItemManager().add(526, 1, entity.getLocation(), killer.getProtocolName(), false);
					}
					/**
					 * Player Drops/Loot
					 */
					Object[] iod = null;
					if (entity instanceof Player) { 
						iod = ItemsOnDeathManager.getIOD((Player) entity, ReturnType.DEATH);
					}
					if (iod == null) {
						return false;
					}
					final ItemContainer ikod = (ItemContainer) iod[0];
					final ItemContainer idod = (ItemContainer) iod[1];
					if (entity instanceof Player) {
						Player player = (Player) entity;
						Tick tick = player.retrieveTick("SkullTimer");
						if (tick != null) {
							tick.setCounter(0);
						}
						player.getCombat().specialEnergy.setAmount(100);
						player.getCombat().specialEnergy.setDrain(0);
						player.getInventory().clear();
						player.getEquipment().clear();
						for (PossesedItem i : ikod.array()) {
							if (i != null)
								player.getInventory().add(i);
						}
						boolean spawned = entity instanceof NPC;
						Player killer = null;
						HitRegister[] register = entity.hitRegisterManager.getSortedHitRegisters();
						int j = 0;
						while (killer == null) {
							killer = register[j++].player;
							if (j >= register.length)
								break;
						}
						if (killer == null)
							spawned = true;
						try {
							for (PossesedItem i : idod.array()) {
								if (i != null) {
									Static.world.getGroundItemManager().add(i.getId(), i.getAmount(), entity.getLocation(), killer.getProtocolName()/*((Player) entity.getCombat().lastAttacker).getProtocolName()*/, spawned);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						stop();
						if (killer != null)
							killer.sendMessage(CombatUtilities.getKillMessage(player));
					}
					return false;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		});
	}
	
	public void stop(boolean resetWalking) {		
		if (getEvent() != null) {
			getEvent().stop();
		} 
        //entity.cancelTick("event");
        entity.cancelStrictTicks();
        if (resetWalking)
        	entity.getPathProcessor().reset();
        entity.resetFaceDirection();
        //entity.subResetEvents();
//		entity.resetFaceDirection();
		Tick ar = entity.retrieveTick("AutoRetaliate");
		if (ar != null)
			ar.stop();
		if (entity instanceof NPC) {
			LOGGER.info("Movement type set to RANDOM");
			((NPC) entity).setMovementType(MovementType.RANDOM);
		}
	}	
	
	public static enum ActionType {
		MELEE, RANGED, MAGIC, OTHER;
	}

	@Override
	public void load(ActionHandlerSystem system) throws Exception {
		system.registerButtonHandler(new int[]{149, 884, 387, 667, 670, 192, 193, 430}, this);	
	}

	@Override
	public void handleButton(Player player, int opcode, int interfaceId, int b,
			int b2, int b3) {
		
		switch (interfaceId) {
		case 192:
		case 193:
		case 430:
			switch (opcode) {
			case 6:
				try {
					Combat combat = player.getCombat();
					boolean succesful = Magic.setAutoCast(player, interfaceId, b);
					if (succesful) {
						if (!combat.magic.isAutocasting()) {
							combat.stop(false);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 1:
				SpellDefinition def = Magic.getDefinition(player, interfaceId, b, true);
				Magic.castSpell(player, null, ActionType.OTHER, def);
				break;
			}
	        Static.callScript("buttons.unhandledButton", player, opcode, interfaceId, b, b2, b3);
			break;
		case 149:
			switch (opcode) {
			case 2:

				break;
			}
			break;
		case 884:
			switch (opcode) {
			case 1:
				switch (b) {
				case 11:
				case 12:
				case 13:
				case 14:
					if (player.getCombat().magic.isAutocasting()) {
						Magic.setAutoCast(player, SpellBook.interfaceForValue(player.getCombat().magic.getSpellBook()), 
								player.getCombat().magic.getAutoCastSpellId());
						player.getCombat().stop(false);
					}
					int button = b - 11;
					PossesedItem i = player.getEquipment().get(EquipmentDefinition.SLOT_WEAPON);
					int groupId;
					if (i != null) {
						groupId = i.getDefinition().weaponGroupId;
					} else {
						groupId = 0;
					}
					Static.proto.sendConfig(player, 43, button);
					player.getCombat().weapon.index = button;
					player.getCombat().weapon.style = Styles.getStyle(groupId, button);
					player.getCombat().weapon.type = Styles.getType(groupId, button);
					break;
				case 15:
					if (player.getCombat().autoRetaliate)
						player.getCombat().autoRetaliate = false;
					else
						player.getCombat().autoRetaliate = true;
					Static.proto.sendConfig(player, 172, player.getCombat().autoRetaliate ? 0 : 1);
					break;
				case 4:
					if (player.getCombat().specialOn)
						player.getCombat().specialOn = false;
					else
						player.getCombat().specialOn = true;
					Static.proto.sendConfig(player, 301, player.getCombat().specialOn ? 1 : 0);
					break;
				}
				break;
			}
			break;
		case 387: // equipment tab
			//Static.callScript("buttons.unhandledButton", player, opcode, interfaceId, b, b2, b3);
			if (opcode == 1) {
				switch (b) {
				case 8:
	            case 11:
	            case 14:
	            case 38:
	            case 17:
	            case 20:
	            case 23:
	            case 26:
	            case 29:
	            case 32:
	            case 35:
	            	player.getCombat().stop(false);
	            	break;
				}
				switch (b) {
				case 17:
				case 38:
					Ranged.setWeapon(player);
					break;
				}
			}
			break;
		case 667: // equipment interface
			//Static.callScript("buttons.unhandledButton", player, opcode, interfaceId, b, b2, b3);
			if (opcode == 1) {
				switch (b) {
				case 7:
					switch (b2) {
					case 3:
						player.getCombat().weapon.style = Styles.getStyle(0, player.getCombat().weapon.index);
						player.getCombat().weapon.type = Styles.getType(0, player.getCombat().weapon.index);
						player.getCombat().stop(false);
		            	break;
					}
				}
			}
			break;
		case 670: // equipment interface inventory
			//Static.callScript("buttons.unhandledButton", player, opcode, interfaceId, b, b2, b3);
			break;
		}
		
	}
	
	public void wieldEquipment(PossesedItem w) {
		if (getEvent() != null)
			getEvent().stop();
		if (w != null) {
			Player player = (Player) entity;
			player.getCombat().weapon.style = Styles.getStyle(w.getDefinition().weaponGroupId, entity.getCombat().weapon.index);
			player.getCombat().weapon.type = Styles.getType(w.getDefinition().weaponGroupId, entity.getCombat().weapon.index);
			if (player.getCombat().weapon.index != Styles.setIndex(w.getDefinition().weaponGroupId, player.getCombat().weapon, entity.getCombat().weapon.index)) {
				Static.proto.sendConfig((Player) entity, 43, player.getCombat().weapon.index);
			}
			if (w.getDefinition().weaponSpecial) {
				player.getCombat().specialEnergy.update();
			}
			player.getCombat().specialOn = false;
			Static.proto.sendConfig(player, 301, 0);
			ItemDefinition idef = w.getDefinition();
			if (idef != null) {
				EquipmentDefinition eqdef = idef.getEquipmentDefinition();
				if (eqdef != null) {
					if (eqdef.getEquipmentType() == EquipmentType.ARROWS || eqdef.getEquipmentType() == EquipmentType.WEAPON
							|| eqdef.getEquipmentType() == EquipmentType.WEAPON_2H || eqdef.getEquipmentType() == EquipmentType.SHIELD) {
						Ranged.setWeapon(player);
					}
				}
			}
		} else {
			try {
				throw new Exception("Illegal item handled? [149 - 2]");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean explicitlyForMembers() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isFrozen() {
		return isFrozen;
	}

	public Magic getMagic() {
		return magic;
	}
	
	public boolean autoRetaliate() {
		return autoRetaliate;
	}
	
	public void setAutoRetaliate(boolean autoRetaliate) {
		this.autoRetaliate = autoRetaliate;
	}
	
	public SpecialEnergy getSpecialEnergy() {
		return specialEnergy;
	}
	
	public Weapon getWeapon() {
		return weapon;
	}
	
	public void setWeapon(Player player, int index) {
		try {
			PossesedItem i = player.getEquipment().get(EquipmentDefinition.SLOT_WEAPON);
			weapon.index = index;
			if (i != null) {
				weapon.style = Styles.getStyle(i.getDefinition().weaponGroupId, weapon.index);
				weapon.type = Styles.getType(i.getDefinition().weaponGroupId, weapon.index);
			} else {
				weapon.style = Styles.getStyle(0, weapon.index);
				weapon.type = Styles.getType(0, weapon.index);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Tick getEvent() {
		return entity.retrieveTick("CombatTick");
	}
	
	public void setLastAttacker(Entity lastAttacker) {
		this.lastAttacker = lastAttacker;
	}

	public Entity getLastAttacker() {
		return lastAttacker;
	}
	
	public static int getHPDrainDelay(int splatDelay) {
		double hpDrainDelay = splatDelay * 12d;
    	hpDrainDelay = Math.floor((hpDrainDelay / 600d * 1000d) / 1000d);
    	return (int) hpDrainDelay;
	}
	
	public static double getAccuracy(Entity attacker, Entity defender, double specialMultiplier) {
		double atEL = getEffectiveLevel(attacker, attacker, attacker.getCombat().scheduledAction.type, false);
		double defEL = 0;

		double atEB = 0;
		double defEB = 0;
		
		switch (attacker.getCombat().scheduledAction.type) {
		
		case MELEE:
			atEB = CombatUtilities.Styles.getAttackBonusForType(attacker, attacker.getCombat().getWeapon().type);
			defEB = CombatUtilities.Styles.getDefenceBonusForType(defender, attacker.getCombat().getWeapon().type);			
			defEL = getEffectiveLevel(defender, attacker, attacker.getCombat().scheduledAction.type, true);
			break;
		case MAGIC:
			atEB = attacker.getBonuses()[Bonuses.OFFENSIVE_MAGIC];
			defEB = defender.getBonuses()[Bonuses.DEFENSIVE_MAGIC];
			defEL = (getEffectiveLevel(defender, attacker, ActionType.MAGIC, true) * 0.7) 
					+ (getEffectiveLevel(defender, attacker, ActionType.MELEE, true) * 0.3);
			break;
		case RANGED:
			atEB = attacker.getBonuses()[Bonuses.OFFENSIVE_RANGED];
			defEB = defender.getBonuses()[Bonuses.DEFENSIVE_RANGED];
			defEL = getEffectiveLevel(defender, attacker, attacker.getCombat().scheduledAction.type, true);
			break;
		}
		
		
		double a = Math.floor(atEL * (1 + atEB / 64));
		double d = Math.floor(defEL * (1 + defEB / 64));
		
		a *= specialMultiplier;
		// TODO floor(a) = > a *= other accuracy bonuses
		
		double accuracy;
		if (a <= d) 
			accuracy = (a - 1) / (2 * d);
		else
			accuracy = 1 - (d + 1) / (2 * a);		
		return accuracy;
	}
	
	public static double getEffectiveLevel(Entity subject, Entity attacker, ActionType type, boolean defender) {
		double level;
		double prayerMultiplier;
		double attackTypeBonus = 0;
		double voidBonus = 1;
		boolean isNPC = subject instanceof NPC;
		NPC npc = subject instanceof NPC ? (NPC) subject : null;
		Player player = subject instanceof Player ? (Player) subject : null;
		Weapon weapon;
		switch (type) {
		case MELEE:
			int index = defender ? Levels.DEFENCE : Levels.ATTACK;
			level = isNPC ? npc.getDefinition().levels[index] : player.getLevels().getCurrentLevel(index);
			prayerMultiplier = isNPC ? 1 : (defender ? player.getPrayerManager().getDefenceMultiplier() : player.getPrayerManager().getAttackMultiplier());
			if (!isNPC) {
				if ((weapon = subject.getCombat().getWeapon()).style == CombatUtilities.Styles.STYLE_ACCURATE && !defender)
					attackTypeBonus = 3;
				else if (weapon.style == CombatUtilities.Styles.STYLE_DEFENSIVE && defender)
					attackTypeBonus = 3;
				else if (weapon.style == CombatUtilities.Styles.STYLE_CONTROLLED)
					attackTypeBonus = 1;
			}
			if (!isNPC && CombatUtilities.wearsArmourSet(player, CombatUtilities.VOID_MELEE))
				voidBonus = 1.1;
			break;
		case MAGIC:
			level = isNPC ? npc.getDefinition().levels[Levels.MAGIC] : player.getLevels().getCurrentLevel(Levels.MAGIC);
			prayerMultiplier = isNPC ? 1 : player.getPrayerManager().getMagicMultiplier();
			if (!isNPC && CombatUtilities.wearsArmourSet(player, CombatUtilities.VOID_MAGIC))
				voidBonus = 1.3;
			break;
		case RANGED:
			level = isNPC ? npc.getDefinition().levels[Levels.RANGE] : player.getLevels().getCurrentLevel(Levels.RANGE);
			prayerMultiplier = isNPC ? 1 : player.getPrayerManager().getRangedMultiplier();
			if (!isNPC) {
				if ((weapon = subject.getCombat().getWeapon()).style == CombatUtilities.Styles.STYLE_ACCURATE && !defender)
					attackTypeBonus = 3;
				else if (weapon.style == CombatUtilities.Styles.STYLE_LONG_RANGE && defender)
					attackTypeBonus = 3;
			}
			if (!isNPC && CombatUtilities.wearsArmourSet(player, CombatUtilities.VOID_RANGED))
				voidBonus = 1.1;
			break;
			default:
				level = 1;
				prayerMultiplier = 1;
				attackTypeBonus = 0;
				break;
		}
		double step1 = level * prayerMultiplier;
		double step2 = step1 + 8;
		double step3 = step2 + attackTypeBonus;
		double step4 = step3 * voidBonus;	
		return Math.floor(step4);
	}
	
	public static double getHitMultiplier(double hitChance) {
		double hitMultiplier = 0;
        double c = RANDOM.nextDouble();
        if (hitChance >= 0.90) {
            hitMultiplier = c <= 0.8 ? getChance(0.5, 9.5, 0.1) : getChance(0, 10, 0.05);
        } else if (hitChance >= 0.80 && hitChance < 0.90) {
            hitMultiplier = c <= 0.65 ? getChance(0.5, 9.5, 0.1) : getChance(0, 10, 0.05);
        } else if (hitChance >= 0.60 && hitChance < 0.80) {
            hitMultiplier = c <= 0.50 ? getChance(0.5, 9.5, 0.1) : getChance(0, 10, 0.05);
        } else if (hitChance >= 0.40 && hitChance < 0.60) {
            hitMultiplier = c <= 0.35 ? getChance(0.5, 9.5, 0.1) : getChance(0, 10, 0.05);
        } else if (hitChance >= 0.20 && hitChance < 0.40) {
            hitMultiplier = c <= 0.20 ? getChance(0.5, 9.5, 0.1) : getChance(0, 10, 0.05);
        } else if (hitChance >= 0 && hitChance < 0.20) {
            hitMultiplier = c <= 0.05 ? getChance(0.5, 9.5, 0.1) : getChance(0, 10, 0.05);
        }
        return hitMultiplier;
	}
	
    public static double getChance(double base, double addition, double include) {
        return ((base + (RANDOM.nextDouble() * addition)) / 10 + include);
    }

}
