package com.runescape.logic;

import com.runescape.engine.tick.Tick;
import com.runescape.logic.mask.SplatNode;

public abstract class HPHandlerTick extends Tick {

    int amount;
    Entity entity;
    SplatNode splatNode;

    public HPHandlerTick(Entity entity, int amount, int delay, final SplatNode splatNode) {
        super(null, delay, TickPolicy.PERSISTENT);
        this.entity = entity;
        this.amount = amount;
        this.splatNode = splatNode;
    }

    public static class HPRemoval extends HPHandlerTick {

        public HPRemoval(Entity entity, int amount, int delay, final SplatNode splatNode) {
            super(entity, amount, delay, splatNode);
        }

        @Override
        public boolean execute() {
            entity.removeHP(amount);
            if (splatNode != null)
                entity.getMasks().submitSplat(splatNode);
            return false;
        }

    }

    public static class HPAddition extends HPHandlerTick {

        int addition;

        public HPAddition(Entity entity, int amount, int delay, int addition, final SplatNode splatNode) {
            super(entity, amount, delay, splatNode);
            this.addition = addition;
        }

        @Override
        public boolean execute() {
            entity.addHP(amount, addition);
            if (splatNode != null)
                entity.getMasks().submitSplat(splatNode);
            return false;
        }

    }

}
