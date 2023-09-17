package com.ziotic.content.combat.actions;

import java.awt.Point;

import org.apache.log4j.Logger;

import com.ziotic.content.combat.Combat.ActionType;
import com.ziotic.content.combat.CombatAction;
import com.ziotic.logic.Entity;
import com.ziotic.logic.item.EquipmentDefinition;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.map.Directions;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.map.Directions.NormalDirection;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.npc.NPCDefinition;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;

public class NPCCombatAction extends CombatAction {
	
	private static final Logger LOGGER = Logging.log();

	public NPCCombatAction(NPC attacker, Entity victim,
			int attackDelay, ActionType type) {
		super(attacker, victim, true, attackDelay, attackDelay);
		this.type = type;
	}

	@Override
	public boolean executeAction() {
		NPC attacker = (NPC) this.attacker;
		switch (type) {
		case MELEE:
			LOGGER.info("executing melee action");
			boolean canAttack = false;
			Tile[][] tiles = attacker.getCoverage().tiles();
			for (int x = 0; x < tiles.length; x++) {
				for (int y = 0; y < tiles[x].length; y++) {
					NormalDirection direction = Directions.directionFor(new Point(tiles[x][y].getX(), tiles[x][y].getY()), new Point(victim.getX(), victim.getY()));
					if (direction != null)
						if (tiles[x][y].canMove(direction, 1, true))
							canAttack = true;
				}
			}
			if (canAttack) {
				int defenceAnim;
		        if (victim instanceof Player) {
		        	PossesedItem defence = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_WEAPON);
		        	PossesedItem shield = ((Player) victim).getEquipment().get(EquipmentDefinition.SLOT_SHIELD);
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
		        } else {
		        	NPC npc = (NPC) victim;
		        	NPCDefinition def = npc.getDefinition();
		        	if (def != null) {
		        		defenceAnim = def.defenceEmote;
		        	} else {
		        		defenceAnim = EquipmentDefinition.DEFAULT_ANIMATIONS.defendAnimation;
		        	}
		        }
		        attacker.getCombat().executeAnimation(attacker.getDefinition().attackEmote, 0, true, false);
		        victim.getCombat().executeAnimation(defenceAnim, 0, false, false);
				attacker.getCombat().hit(victim, type, false, 1, 1, null, 1, null, new int[] { 0 }, new int[] { 0 });
				return true;
			}
			return false;
		case MAGIC:
			return true;
		case RANGED:
			return true;
		}
		return false;
	}

}
