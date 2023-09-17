package com.ziotic.content.combat.ticks;

import com.ziotic.content.combat.Combat;
import com.ziotic.content.combat.Combat.ActionType;
import com.ziotic.content.combat.CombatAction;
import com.ziotic.content.combat.actions.PlayerMagicAction;
import com.ziotic.content.combat.misc.CombatUtilities;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.player.Player;

public class PlayerCombatHandlerTick extends Tick {

    private Combat combat;

    public PlayerCombatHandlerTick(Combat combat) {
        super("CombatTick", 0, TickPolicy.STRICT);
        this.combat = combat;
    }

    public int runtime = 0;
    
    @Override
    public boolean execute() {
        try {
        	runtime++;
            CombatAction ca = combat.scheduledAction;
            if (ca != null) {
                if (ca.victim instanceof Player)
                    if (((Player) ca.victim).isDestroyed()) {
                        return false;
                    }
                if (ca.victim != null && !ca.victim.isDead() && ca.victim.inGame() && ca.attacker != null && !ca.attacker.isDead() && ca.attacker.inGame()) {
                    if (CombatUtilities.canAttackLevelBased(ca.attacker, ca.victim, true)) {
                        boolean correctPosition;
                        boolean finalCorrectPosition = true;
                        if (!(correctPosition = CombatUtilities.inCorrectPosition(ca.attacker, ca.victim, ca.type))) {
                            if (!combat.isFrozen() && runtime >= 4) {
                                CombatUtilities.moveToCorrectPosition(ca.attacker, ca.victim, ca.type, true, false);
                            }
                            finalCorrectPosition = CombatUtilities.inCorrectFollowPosition(ca.attacker, ca.victim, ca.type);
                        }
                        combat.attackFromInitiateDistance = !correctPosition;
                        if ((finalCorrectPosition && !combat.isFrozen()) || correctPosition) {
                            if (combat.playerCanAttack(ca.victim)) {
                                if (combat.isReadyToAttack(ca.attackDelay, ca.spellDelay, ca.type)) {
                                    if (correctPosition) {
                                        ca.attacker.getPathProcessor().reset();
                                    }
                                    boolean succesful = ca.executeAction();
                                    if (succesful) {
                                        if (ca.recurring) {
                                            if (ca.type == ActionType.MAGIC) {
                                                PlayerMagicAction pma = (PlayerMagicAction) ca;
                                                combat.scheduledAction = combat.getNextCombatAction((Player) ca.attacker, ca.victim, true, false, pma.currentSpell);
                                            } else {
                                                combat.scheduledAction = combat.getNextCombatAction((Player) ca.attacker, ca.victim, true, false, null);
                                            }
                                        } else if (ca.type == ActionType.MAGIC) {
                                            PlayerMagicAction pma = (PlayerMagicAction) ca;
                                            if (pma.queuedSpell != null) {
                                                combat.scheduledAction = combat.getNextCombatAction((Player) ca.attacker, ca.victim, true, true, pma.queuedSpell);
                                            } else {
                                                combat.scheduledAction = null;
                                            }
                                        }
                                        switch (ca.type) {
                                            case MAGIC:
                                                combat.setNextSpellTime(ca.spellDelay);
                                                break;
                                            case MELEE:
                                            case RANGED:
                                                combat.setNextSpellTime(4);
                                                break;
                                        }
                                        combat.appendAttackTime();
                                        ca.victim.getCombat().appendDefenceTime();
                                        combat.setNextAttackTime(ca.attackDelay);
                                        combat.lastVictim = ca.victim;
                                        ca.victim.getCombat().setLastAttacker(ca.attacker);
                                    } else {
                                        return true;
                                    }
                                }
                            } else {
                                combat.stop(true);
                                return false;
                            }
                        }
                        return true;
                    } else {
                        combat.stop(true);
                        return false;
                    }
                } else {
                    combat.stop(true);
                    return false;
                }
            } else {
                combat.stop(true);
                return false;
            }
        } catch (Exception e) {
        	combat.stop(true);
            e.printStackTrace();
            return false;
        }
    }

}
