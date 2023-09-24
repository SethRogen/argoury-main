package com.ziotic.content.combat.ticks;

import com.ziotic.content.combat.Combat;
import com.ziotic.content.combat.CombatAction;
import com.ziotic.content.combat.misc.CombatUtilities;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.npc.NPC.MovementType;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

public class NPCCombatHandlerTick extends Tick {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logging.log();

    private Combat combat;

    public NPCCombatHandlerTick(Combat combat) {
        super("CombatTick", 1, TickPolicy.PERSISTENT);
        this.combat = combat;
    }
    
    private int underAttackCounter = 0;

    @Override
    public boolean execute() {
        try {
        	CombatAction ca = combat.scheduledAction;
        	if (ca != null) {
        		if (ca.victim instanceof Player)
                    if (((Player) ca.victim).isDestroyed()) {
                        return false;
                    }
        		if (ca.victim != null && !ca.victim.isDead() && ca.victim.inGame() && ca.attacker != null && !ca.attacker.isDead() && ca.attacker.inGame()) {
                	NPC npc = (NPC) ca.attacker;
                	npc.setMovementType(MovementType.CONTROLLED);
        			boolean correctPosition;
        			boolean finalCorrectPosition = true;
                    if (!(correctPosition = CombatUtilities.inCorrectPosition(ca.attacker, ca.victim, ca.type))) {
                        if (!combat.isFrozen())
                            CombatUtilities.moveToCorrectPosition(ca.attacker, ca.victim, ca.type, true, true);
                        finalCorrectPosition = CombatUtilities.inCorrectFollowPosition(ca.attacker, ca.victim, ca.type);
                    }
                    combat.attackFromInitiateDistance = !correctPosition;
                    if ((finalCorrectPosition && !combat.isFrozen()) || correctPosition) {
                    	if (combat.npcCanAttack(ca.victim)) {
                    		underAttackCounter = 0;
                    		if (combat.isReadyToAttack(ca.attackDelay, ca.spellDelay, ca.type)) {
                    			if (correctPosition) {
                                    ca.attacker.getPathProcessor().reset();
                                }
                    			boolean succesful = ca.executeAction();
                    			if (succesful) {
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
	                                return true;
                    			} else {
                    				combat.stop(false);
                    				return false;
                    			}
                    		} else {
                    			return true;
                    		}
                    	} else {
                    		if (underAttackCounter++ > 10) {
	                    		combat.stop(true);
	                    		return false;
                    		} else
                    			return true;
                    	}
                    } else {
                    	if (npc.getDefinition().aggressiveRange < npc.getLocation().distance(ca.victim.getLocation())) {
                    		combat.stop(true);
                    		return false;
                    	} else if (!npc.getLocation().withinRange(npc.getSpawn().location, npc.getSpawn().range)) { 
                    		combat.stop(true);
                    		return false;
                    	}
                    	return true;
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
        	e.printStackTrace();
        	combat.stop(true);
        	return false;
        }
    }

}
