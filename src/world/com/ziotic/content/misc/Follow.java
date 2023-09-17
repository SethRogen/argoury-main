package com.ziotic.content.misc;

import com.ziotic.Static;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.Entity;
import com.ziotic.logic.map.PathProcessor;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.map.pf.astar.AStarPathFinder;
import com.ziotic.logic.player.Player;

/**
 * @author Lazaro
 */
public class Follow extends Tick {
    private static final int LOCAL_FOLLOWING_DISTANCE = 6;

    private Entity entity;
    private Entity other;

    public Follow(Entity entity, Entity other) {
        super("following", 1, TickPolicy.STRICT);
        this.entity = entity;
        this.other = other;
    }

    @Override
    public void onStart() {
        entity.faceEntity(other);
        entity.getPathProcessor().reset();
    }

    @Override
    public void onStop() {
        other.getPathProcessor().unhookEntity(entity);
    }

    @Override
    public boolean execute() {
        if (entity.getCombat().isFrozen())
            return true;
        if (entity.getLocation().distance(other.getLocation()) >= 16) {
            return true;
        }

        Tile origDest = entity.getPathProcessor().getDestination();
        if (origDest == null) {
            origDest = entity.getLocation();
        }

        Tile newDest = other.getPathProcessor().getHistory()[1];
        boolean calcDest = false;
        if (newDest == null) {
            newDest = other.getLocation();
            calcDest = true;
        }

        if (origDest.equals(newDest) || (calcDest && origDest.distance(newDest) == 1)) {
            if (!entity.getPathProcessor().moving()) {
                entity.getPathProcessor().reset();
                other.getPathProcessor().hookEntity(entity);
            }
            return true;
        }


        if (origDest.distance(other.getLocation()) > LOCAL_FOLLOWING_DISTANCE) {
            other.getPathProcessor().unhookEntity(entity);
        }

        if (!other.getPathProcessor().isHooked(entity)) {
            if (entity instanceof Player) {
                Static.world.submitPath(new AStarPathFinder(), entity, newDest.getX(), newDest.getY(), calcDest ? other : null, PathProcessor.MOVE_SPEED_ANY, true, new Runnable() {
                    @Override
                    public void run() {
                        entity.getPathProcessor().reset();
                    }

                });
                if (interval() != 3)
                    setInterval(3);
            }
        } else {
            if (interval() != 0)
                setInterval(0);
        }

        return true;
    }
}
