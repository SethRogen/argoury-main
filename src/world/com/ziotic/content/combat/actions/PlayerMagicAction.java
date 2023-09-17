package com.ziotic.content.combat.actions;

import com.ziotic.content.combat.Combat.ActionType;
import com.ziotic.content.combat.CombatAction;
import com.ziotic.content.combat.Magic;
import com.ziotic.content.combat.definitions.SpellDefinition;
import com.ziotic.logic.Entity;
import com.ziotic.logic.player.Player;

public class PlayerMagicAction extends CombatAction {

    public SpellDefinition queuedSpell;
    public SpellDefinition currentSpell;

    public PlayerMagicAction(Entity attacker, Entity victim, boolean recurring,
                             int attackDelay, int spellDelay, SpellDefinition currentSpell) {
        super(attacker, victim, recurring, attackDelay, spellDelay);
        this.currentSpell = currentSpell;
        type = ActionType.MAGIC;
    }

    @Override
    public boolean executeAction() {
        return Magic.castSpell((Player) attacker, victim, type, currentSpell);
    }

    public void queueSpell(SpellDefinition queuedSpell) {
        this.queuedSpell = queuedSpell;
    }

}
