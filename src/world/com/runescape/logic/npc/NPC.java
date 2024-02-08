package com.runescape.logic.npc;

import com.runescape.Static;
import com.runescape.content.combat.misc.AggressionHandler;
import com.runescape.content.combat.misc.CombatUtilities;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.Entity;
import com.runescape.logic.dialogue.Conversation;
import com.runescape.logic.map.Directions;
import com.runescape.logic.map.PathProcessor;
import com.runescape.logic.map.Tile;
import com.runescape.logic.map.Directions.NormalDirection;
import com.runescape.logic.utility.NodeRunnable;
import com.runescape.utility.Destroyable;

/**
 * @author Lazaro
 */
public class NPC extends Entity implements Destroyable {
    private int id;
    private boolean visible = true;
    private boolean destroyed = false;

    private NPCSpawn spawn;

    private int conversations = 0;

    private int nextLoop = 0;
    
    protected MovementType movementType = MovementType.RANDOM;
    protected AggressionHandler aggressionHandler;
    
    public static enum MovementType {
    	CONTROLLED,
    	RANDOM,
    	OTHER;
    }

    public NPC() {
        //TODO Check if this would effect anything else.
    }

    public NPC(NPCSpawn spawn) {
        this.spawn = spawn;
        this.id = spawn.id;

        setPathProcessor(new NPCPathProcessor(this));
        setLocation(spawn.location);
        NPCDefinition def = NPCDefinition.forId(id);
        if (def != null) {
            if (def.aggressive) {
    	        aggressionHandler = new AggressionHandler(this);
    	        aggressionHandler.register();
            }
        	hp = def.hp;
        	if (def.meleeStyle != null) {
	        	switch (def.meleeStyle) {
	        	case SLASH:
	        		CombatUtilities.Styles.setType(combat.weapon, CombatUtilities.Styles.TYPE_SLASH);
	        		break;
	        	case STAB:
	        		CombatUtilities.Styles.setType(combat.weapon, CombatUtilities.Styles.TYPE_STAB);
	        		break;
	        	case CRUSH:
	        		CombatUtilities.Styles.setType(combat.weapon, CombatUtilities.Styles.TYPE_CRUSH);
	        		break;
	        	}
        	} else
        		CombatUtilities.Styles.setType(combat.weapon, CombatUtilities.Styles.TYPE_SLASH);
        } else {
        	CombatUtilities.Styles.setType(combat.weapon, CombatUtilities.Styles.TYPE_SLASH);
        }
    }

    public int getId() {
        return id;
    }

    public int getActualId() {
        return spawn.id;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public NPCSpawn getSpawn() {
        return spawn;
    }

    public NPCDefinition getDefinition() {
        return NPCDefinition.forId(id);
    }
    
    @Override
    public void onDeath() {
        if (aggressionHandler != null)
        	aggressionHandler.destroy();
    	combat.stop(true);
    	
		for (Tick t : hpTicks) {
			t.stop();
		}
		final Entity thisEntity = this;
		Tick tick = retrieveTick("SetHPDelay");
		int extraDelay = 0;
		if (tick != null)
			if (tick.running())
				extraDelay = tick.getCounter();
		registerTick(new Tick("death_event1", 1 + extraDelay) {
			@Override
			public boolean execute() {
				NodeRunnable<Entity> death1 = getDeathEvent(1);
				if (death1 != null) {
					death1.run(thisEntity);
				}
				return false;
			}
		});
		registerTick(new Tick("death_event2", 4 + extraDelay) {
			@Override
			public boolean execute() {
				NodeRunnable<Entity> death2 = getDeathEvent(2);
				if (death2 != null) {
					death2.run(thisEntity);
				}
				setHP(getMaxHP());
				masks.setUpdateHPBar(true);
				dead = false;
				return false;
			}
		});

		dead = true;
    }
    
    public static final NodeRunnable<Entity> DEATH_EVENT_1 = new NodeRunnable<Entity>() {
        @Override
        public void run(Entity entity) {
        	NPC npc = (NPC) entity;
        	NPCDefinition def = npc.getDefinition();
        	int deathAnim = 0;
        	if (def != null)
        		deathAnim = def.deathEmote;
            entity.getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_WALK);
            entity.doAnimation(deathAnim);
            entity.getCombat().uponDeath1();
        }
    };

    public static final NodeRunnable<Entity> DEATH_EVENT_2 = new NodeRunnable<Entity>() {
        @Override
        public void run(Entity entity) {
            final NPC npc = (NPC) entity;
            npc.resetEvents();
            Static.world.unregister(npc);
            npc.getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_ANY);
            Static.world.addGlobalProcess("RESPAWN_" + entity.getIdentifier(), new Runnable() {
            	@Override
            	public void run() {
            		Static.world.register(new NPC(npc.spawn));
            	}
            }, 100);
            if (npc.combat.lastVictim != null) {
        		npc.combat.lastVictim.getCombat().stop(false);
        		if (npc.combat.lastVictim.getLocation().distance(npc.getLocation()) < 16) {
        			npc.combat.lastVictim.setLocation(npc.combat.lastVictim.getLocation());
        		}
        	}
        }
    };

    @Override
    public void subResetEvents() {
    }

    @Override
    public void subPreProcess() {
        NPCDefinition def = getDefinition();
        if ((def.loopScript != null && !def.loopScript.equals("") && !def.loopScript.equals("null")) && Static.world.getTime() >= nextLoop) {
            nextLoop = ((Number) Static.callScript(def.loopScript + ".loop", this)).intValue() + Static.world.getTime();
        } else {
            if (!getPathProcessor().moving() && !getCombat().inCombat() && conversations == 0) {
                randomMovement();
            }
        }
    }

    private void randomMovement() {
        int range = spawn.range;
        if (range > 0 && Static.random.nextInt(5) == 0 && movementType == MovementType.RANDOM) {
            NormalDirection dir = NormalDirection.forIntValue(Static.random.nextInt(8));
            if (getLocation().canMove(dir, getSize(), true)) {
                Tile next = getLocation().translate(Directions.DIRECTION_DELTA_X[dir.intValue()], Directions.DIRECTION_DELTA_Y[dir.intValue()], 0);
                if (next.withinRange(spawn.location, range)) {
                    setLocation(next);
                    getDirections().setDirection(dir);
                }
            }
        }
    }

    @Override
    public void subPostProcess() {
    }

    @Override
    public void onChangedHP() {
    }

    @Override
    public int getSize() {
        return getDefinition().size;
    }

    @Override
    public int getMaxHP() {
    	NPCDefinition def = NPCDefinition.forId(this.id);
    	if (def != null)
    		return def.hp;
        return 100;
    }

    @Override
    public boolean inGame() {
        return true;
    }

    @Override
    public NodeRunnable<Entity> getDeathEvent(int stage) {
        if (stage == 1) {
            return DEATH_EVENT_1;
        } else if (stage == 2) {
            return DEATH_EVENT_2;
        }
        return null;
    }

    @Override
    public double[] getBonuses() {
        return getDefinition().bonuses;
    }

    @Override
    public void run() {
        switch (updateStage) {
            case PRE_UPDATE:
                try {
                    preProcess();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return;
            case MASK_UPDATE:
                try {
                    setCachedMaskBlock(Static.world.getNPCUpdater().doMaskBlock(null, this));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                Static.world.getMaskUpdateLatch().countDown();
                return;
            case POST_UPDATE:
                try {
                    postProcess();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                Static.world.getResetUpdateLatch().countDown();
                return;
        }
    }

    @Override
    public void destroy() {
        destroyed = true;
        visible = false;
        setLocation(null);
    }

    public void startedConversation(Conversation conversation) {
        conversations++;
    }

    public void endedConversation(Conversation conversation) {
        getMasks().resetDirection();
        conversations--;
    }

    public void setSpawn(NPCSpawn spawn) {
        this.spawn = spawn;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void transform(int id) {
        this.id = id;
        this.getMasks().setSwitchId(id);
    }

	public void setMovementType(MovementType movementType) {
		this.movementType = movementType;
	}

	public MovementType getMovementType() {
		return movementType;
	}
}
