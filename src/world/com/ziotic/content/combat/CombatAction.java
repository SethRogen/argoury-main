package com.ziotic.content.combat;

import com.ziotic.content.combat.Combat.ActionType;
import com.ziotic.logic.Entity;

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
