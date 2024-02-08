package com.runescape.content.misc;

import com.runescape.engine.tick.Tick;
import com.runescape.logic.Entity;

/**
 * @author Lazaro
 */
public class HPRestore extends Tick {
    private Entity entity;

    public HPRestore(Entity entity) {
        super("hp_restore", 10); // 6 seconds = 10 ticks
        this.entity = entity;
    }

    @Override
    public boolean execute() {
        if (entity.getHP() == 0 || entity.isDead())
            return true;
        int hp = entity.getHP();
        if (hp < entity.getMaxHP()) {
            hp++;
            entity.setHP(hp);
        }
        return true;
    }
}
