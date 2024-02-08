package com.runescape.content.combat.actions;

import com.runescape.Static;
import com.runescape.content.combat.CombatAction;
import com.runescape.content.combat.Combat.ActionType;
import com.runescape.logic.Entity;
import com.runescape.logic.item.EquipmentDefinition;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.map.Directions;
import com.runescape.logic.map.Directions.NormalDirection;
import com.runescape.logic.npc.NPC;
import com.runescape.logic.npc.NPCDefinition;
import com.runescape.logic.player.Player;

import java.awt.*;

public class PlayerMeleeAction extends CombatAction {

    public PlayerMeleeAction(Entity attacker, Entity victim, boolean recurring,
                             int attackDelay, int spellDelay) {
        super(attacker, victim, recurring, attackDelay, spellDelay);
        type = ActionType.MELEE;
    }

    @Override
    public boolean executeAction() {
        Player attacker = (Player) this.attacker;
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
        PossesedItem attack = attacker.getEquipment().get(EquipmentDefinition.SLOT_WEAPON);
        int attackAnim;
        EquipmentDefinition wepDef = null;
        if (attack == null)
            attackAnim = EquipmentDefinition.DEFAULT_ANIMATIONS.attackAnimations[attacker.getCombat().weapon.index];
        else {
            wepDef = attack.getDefinition().getEquipmentDefinition();
            attackAnim = wepDef.getEquipmentAnimations().attackAnimations[attacker.getCombat().weapon.index];
        }
        boolean special = false;
        if (attack != null && attack.getDefinition().weaponSpecial && attacker.getCombat().specialOn) {
            attacker.getCombat().specialOn = false;
            Static.proto.sendConfig(attacker, 301, 0);
            special = true;
        }
        NormalDirection direction = Directions.directionFor(new Point(attacker.getX(), attacker.getY()), new Point(victim.getX(), victim.getY()));
        if (direction != null) {
            if (attacker.getLocation().canMove(direction, attacker.getSize(), false)) {
                if ((!special || !Static.<Boolean>callScript("specialattacks.handleSpecial", attacker, victim, attacker.getCombat(), attack, type, defenceAnim))) {
                    victim.getCombat().executeAnimation(defenceAnim, 0, false, false);
                    attacker.getCombat().executeAnimation(attackAnim, 0, true, false);
                    attacker.getCombat().hit(victim, type, false, 1.0, 1.0, null, 1, null, new int[]{20}, new int[]{0});
                }
                return true;
            } else
                return false;
        } else
            return false;
    }

}
