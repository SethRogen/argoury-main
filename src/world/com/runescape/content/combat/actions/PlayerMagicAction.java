package com.runescape.content.combat.actions;

import com.runescape.content.combat.CombatAction;
import com.runescape.content.combat.Magic;
import com.runescape.content.combat.Combat.ActionType;
import com.runescape.content.combat.definitions.SpellDefinition;
import com.runescape.logic.Entity;
import com.runescape.logic.player.Player;

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
