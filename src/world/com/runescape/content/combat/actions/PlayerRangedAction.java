package com.runescape.content.combat.actions;

import com.runescape.Static;
import com.runescape.content.combat.CombatAction;
import com.runescape.content.combat.Ranged;
import com.runescape.content.combat.Combat.ActionType;
import com.runescape.logic.Entity;
import com.runescape.logic.item.EquipmentDefinition;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.player.Player;

public class PlayerRangedAction extends CombatAction {

    public PlayerRangedAction(Entity attacker, Entity victim,
                              boolean recurring, int attackDelay, int spellDelay) {
        super(attacker, victim, recurring, attackDelay, spellDelay);
        type = ActionType.RANGED;
    }

    @Override
    public boolean executeAction() {
        Player attacker = (Player) this.attacker;
        PossesedItem attack = attacker.getEquipment().get(EquipmentDefinition.SLOT_WEAPON);
        boolean special = false;
        if (attack != null && attack.getDefinition().weaponSpecial && attacker.getCombat().specialOn) {
            attacker.getCombat().specialOn = false;
            Static.proto.sendConfig(attacker, 301, 0);
            special = true;
        }
        return Ranged.shoot(attacker, victim, type, special);
    }

}
