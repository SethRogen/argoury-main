package com.runescape.content.combat;

import com.runescape.content.combat.Combat.ActionType;
import com.runescape.logic.Entity;

public abstract class CombatAction {

    public Entity attacker;
    public Entity victim;

    public boolean recurring;
    public int attackDelay;
    public int spellDelay;

    public ActionType type;

    public CombatAction(Entity attacker, Entity victim, boolean recurring, int attackDelay, int spellDelay) {
        this.attacker = attacker;
        this.victim = victim;
        this.recurring = recurring;
        this.attackDelay = attackDelay;
        this.spellDelay = spellDelay;
    }

    public abstract boolean executeAction();

}
