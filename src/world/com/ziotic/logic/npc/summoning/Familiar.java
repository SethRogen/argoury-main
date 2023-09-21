package com.ziotic.logic.npc.summoning;

import org.apache.log4j.Logger;

import com.ziotic.Static;
import com.ziotic.content.combat.Combat.ActionType;
import com.ziotic.content.combat.misc.CombatUtilities;
import com.ziotic.content.skill.members.summoning.SummoningPouch;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.Entity;
import com.ziotic.logic.item.ItemDefinition;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.mask.Graphic;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.npc.NPCDefinition;
import com.ziotic.logic.npc.NPCPathProcessor;
import com.ziotic.logic.npc.NPCSpawn;
import com.ziotic.logic.player.DisplayMode;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;

/**
 * @author 'Mystic Flow
 */
public abstract class Familiar extends NPC {

	public static final Graphic SMALL_SUMMON_GRAPHIC = new Graphic(1314), LARGE_SUMMON_GRAPHIC = new Graphic(1315);
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logging.log();

	protected Player owner;

	private SummoningPouch pouch;
	private int ticks;
	private int currentTimeLeft;
	private int specialPoints = 60, specialRestore;
	private int stuckTicks = 0;

	public Familiar(Player owner) {
		this.owner = owner;
		this.movementType = MovementType.CONTROLLED;
	}

	@Override
	public void subPreProcess() {
		if (owner == null || !owner.isConnected()) {
			dismiss();
			return;
		}
		ticks--;
		if (ticks < 1) {
			dismiss();
			return;
		}
		int timeLeft = ((ticks / 50) + 1) << 6;
		if (currentTimeLeft != timeLeft) {
			Static.proto.sendConfig(owner, 1176, timeLeft);
			currentTimeLeft = timeLeft;
		}
		if (ticks == 100) {
			owner.sendMessage("You have 1 minute before your familiar vanishes.");
		} else if (ticks == 50) {
			owner.sendMessage("You have 30 seconds before your familiar vanishes.");
		}
		if (specialPoints < 60) {
			specialRestore++;
			if (specialRestore == 50) {
				specialRestore = 0;
				incrementSpecialPoints(15);
			}
		}
		boolean pos = CombatUtilities.inCorrectPosition(this, this.getOwner(), ActionType.MELEE);
		if (!pos) {
			if (!CombatUtilities.moveToCorrectPosition(this, this.getOwner(), ActionType.MELEE, false, false))
				stuckTicks++;
			else 
				stuckTicks = 0;
		}
		if (getCoverage().center().distance(owner.getCoverage().center()) >= 15 || stuckTicks >= 10) {
			stuckTicks = 0;
			call(false);
		} 
	}

	public void summon(SummoningPouch pouch) {
		this.pouch = pouch;
		this.ticks = getMinutes() * 100;

		Tile foundTile = Entity.locationNextTo(owner, NPCDefinition.forId(pouch.getNPCId()).size);

		if (foundTile == null) {
			owner.sendMessage("Your familiar doesn't fit in this area!");
			return;
		}
		owner.getInventory().remove(pouch.getPouchId(), 1);
		// send packet's first
		Static.proto.sendConfig(owner, 448, pouch.getPouchId());
		Static.proto.sendConfig(owner, 1174, pouch.getNPCId());
		Static.proto.sendConfig(owner, 1175, specialCost() << 23);
		Static.proto.sendConfig(owner, 1176, 768);
		Static.proto.sendConfig(owner, 1160, 243269632);
		Static.proto.sendSpecialString(owner, 204, getSpecialAttackName());
		Static.proto.sendSpecialString(owner, 205, getSpecialAttackDescription());
		incrementSpecialPoints(0); // send spec points
		switch (specialType()) {
		case CLICK:
			Static.proto.sendAccessMask(owner, 0, 0, 747, 17, 0, 2);
			Static.proto.sendAccessMask(owner, 0, 0, 662, 74, 0, 2); 
			Static.proto.sendInterfaceVariable(owner, 1436, 1);
			break;
		case ENTITY_TARGET:
			Static.proto.sendAccessMask(owner, 0, 0, 747, 17, 0, 20480);
			Static.proto.sendAccessMask(owner, 0, 0, 662, 74, 0, 20480); 
			Static.proto.sendInterfaceVariable(owner, 1436, 0);
			break;
		case ITEM_TARGET:
			Static.proto.sendAccessMask(owner, 0, 0, 747, 17, 0, 65536);
			Static.proto.sendAccessMask(owner, 0, 0, 662, 74, 0, 65536); 
			Static.proto.sendInterfaceVariable(owner, 1436, 0);
			break;
		}
		Static.proto.sendInterfaceShowConfig(owner, 747, 8, false);

		// prepare the entity
		setId(pouch.getNPCId());
		setSpawn(new NPCSpawn(pouch.getNPCId(), foundTile, null, -1, false));
		setPathProcessor(new NPCPathProcessor(this));
		setLocation(foundTile);
		coverage.update(foundTile, getSize());

		// prepare the beast of burden (if it is)
		BeastOfBurden bob = beastOfBurden();
		if (bob != null)
			bob.createContainer();

		// register it to the world
		Static.world.register(this);

		// perform masks
		performSummoningCircle();
		faceEntity(owner);

		// affect the owner
		owner.getLevels().setCurrentLevel(Levels.SUMMONING, owner.getLevels().getCurrentLevel(Levels.SUMMONING) - pouch.summonCost());
		owner.setFamiliar(this);

		// final packet
		Static.proto.sendLevel(owner, Levels.SUMMONING);
	}

	public void dismiss() {
		if (owner.getFamiliar() != null) {
			owner.setFamiliar(null);
			Static.proto.sendCloseInterface(owner, 548, 219);
			Static.proto.sendConfig(owner, 448, -1);
			Static.proto.sendConfig(owner, 1174, -1);
			Static.proto.sendConfig(owner, 1175, 182986);
			Static.proto.sendConfig(owner, 1176, 0);
			Static.proto.sendConfig(owner, 1160, -1);
			Static.proto.sendAccessMask(owner, 0, 0, 747, 17, 0, 0);
			Static.proto.sendInterfaceScript(owner, 2471);
			Static.proto.sendInterfaceScript(owner, 655);
		}
		Static.world.unregister(this);
	}

	public void showDetails() {
		Static.proto.sendInterface(owner, 662, owner.getDisplayMode() == DisplayMode.FIXED ? 548 : 746, owner.getDisplayMode() == DisplayMode.FIXED ? 219 : 104, false);
		Static.proto.sendInterfaceVariable(owner, 168, 95);
		//880
	}

	public void call(boolean requested) {
		int size = NPCDefinition.forId(pouch.getNPCId()).size;
		Tile actual = Tile.locate(getLocation().getX() + (size / 2), getLocation().getY() + (size / 2), getLocation().getZ());
		if (requested && owner.getLocation().distance(actual) < 2) {
			return;
		}
		Tile foundTile = Entity.locationNextTo(owner, size);
		if (foundTile == null) {
			if (requested)
				owner.sendMessage("Your familiar doesn't fit in this area!");
			return;
		}
		performSummoningCircle();
		setTeleportDestination(foundTile);
		registerTick(new Tick(null, 1) { // TODO Is this even needed?
			@Override
			public boolean execute() {
				faceEntity(owner);
				return false;
			}
		});
	}

	public void performSummoningCircle() {
		int size = NPCDefinition.forId(pouch.getNPCId()).size;
		doGraphics(size > 1 ? LARGE_SUMMON_GRAPHIC : SMALL_SUMMON_GRAPHIC);
	}

	public void doSpecial(FamiliarSpecial type, Object context) {
		if (type != specialType())
			return;
		if (!owner.getInventory().contains(pouch.getScrollId())) {
			owner.sendMessage("You do not have enough scrolls to do that.");
			return;
		}
		if (owner.getLevels().getCurrentLevel(Levels.SUMMONING) - specialCost() < 1) {
			owner.sendMessage("You don't have enough summoning points to cast this!");
			return;
		}
		if (specialPoints - specialCost() < 1) {
			owner.sendMessage("Your familiar has run out of special points!");
			return;
		}
		if (type == FamiliarSpecial.ENTITY_TARGET) {
			if (!(context instanceof Entity)) {
				owner.sendMessage("Your target has to be either a player or NPC.");
				return;
			}
			Entity entity = (Entity) context;
			boolean isPlayer = entity instanceof Player;
			String failMessage = null;

			if (entity.isDead())
				failMessage = "That " + (isPlayer ? "player" : "npc") + " is already dead!";
			else if (!owner.isInMulti())
				failMessage = "Your familiar cannot fight unless you are in a multi-way combat area.";
			else if (!entity.isInMulti())
				failMessage = "Your target cannot be targeted unless it's in a multi-way combat area.";
			if (failMessage != null) {
				owner.sendMessage(failMessage);
				return;
			}
		} else if (type == FamiliarSpecial.ITEM_TARGET) {
			if (!(context instanceof Integer)) {
				return; // no message ;s ?
			}
		}
		if (performSpecial(type, context)) {
			incrementSpecialPoints(-specialCost());
			owner.getInventory().remove(pouch.getScrollId());
		}
	}

	public int getTicks() {
		return ticks;
	}

	public SummoningPouch pouch() {
		return pouch;
	}

	public String getSpecialAttackName() {
		return ItemDefinition.forId(pouch.getScrollId()).name.replace("scroll", "");
	}

	public BeastOfBurden beastOfBurden() {
		return null;
	}

	public Player getOwner() {
		return owner;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public void incrementSpecialPoints(int specialPoints) {
		this.specialPoints += specialPoints;
		if (this.specialPoints > 60) {
			this.specialPoints = 60;
		}
		Static.proto.sendConfig(owner, 1177, this.specialPoints);
	}

	protected abstract boolean performSpecial(FamiliarSpecial type, Object context);

	public abstract int specialCost();

	public abstract String getSpecialAttackDescription();

	public abstract int getMinutes();

	public abstract FamiliarSpecial specialType();

}
