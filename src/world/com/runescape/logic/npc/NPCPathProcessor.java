package com.runescape.logic.npc;

import com.runescape.logic.map.Directions;
import com.runescape.logic.map.PathProcessor;
import com.runescape.logic.map.Tile;
import com.runescape.logic.map.Directions.NormalDirection;

/**
 * @author Lazaro
 */
public class NPCPathProcessor extends PathProcessor {
    private NPC npc;
    
    public NPCPathProcessor(NPC npc) {
        super(npc);
        this.npc = npc;
    }

    @Override
    protected NormalDirection next() {
        /**
         * Check if we are walking.
         */
        if (moving()) {
            /**
             * Get the first waypoint from the buffer.
             */
            Tile nextPoint = buffer.peek();
            /**
             * Check if we are already on this tile.
             */
            if (npc.getLocation().equals(nextPoint)) {
                /**
                 * Remove the current waypoint and get the next one.
                 */
                buffer.next();
                nextPoint = buffer.peek();
            }
            /**
             * Check if there is a next step.
             */
            if (nextPoint == null) {
                /**
                 * Stop walking.
                 */
            	reset();
                return null;
            }
            /**
             * Calculate the directions of the next waypoint.
             */
            NormalDirection direction = Directions.directionFor(npc.getLocation(), nextPoint);
            /**
             * We want to move.
             */
            if (direction != null) {
            	
                if (!npc.getLocation().canMove(direction, npc.getSize(), false)) {
                    reset();
                    return null;
                }

                /**
                 * Calculate the next tile.
                 */
                Tile next = npc.getLocation().translate(Directions.DIRECTION_DELTA_X[direction.intValue()], Directions.DIRECTION_DELTA_Y[direction.intValue()], 0);

                /**
                 * Check that the NPC will stay within range.
                 */
                int range = npc.getSpawn().range;
                if (!next.withinRange(npc.getSpawn().location, range) && range != -1) {
                	npc.getCombat().stop(true);
                    return null;
                }

                /**
                 * Set the tile and return the directions.
                 */
                npc.setLocation(next);
                npc.updateCoverage(next);

                updateHistory(next);

                return direction;
            } 
        }
        return null;
    }

    @Override
    public void process() {
        Tile oldLocation = npc.getLocation(); // Save the old location.
        if (npc.getTeleportDestination() != null) { // Check if we are teleporting.
            npc.setLocation(npc.getTeleportDestination()); // Set the new location.
            npc.updateCoverage(npc.getTeleportDestination()); // Set the new tile coverage
            npc.setTeleportDestination(null); // Reset the teleport variables.
            npc.setTeleporting(true); // Flag the teleport.
            reset();
        } else if (moving() && !npc.getCombat().isFrozen()) { // Check if we are moving.
            Directions.Direction direction = next(); // Calculate the first direction, for walking.
            Directions.Direction secondDirection = null;
            if (moveSpeed == MOVE_SPEED_RUN) { // Check if we are running.
                npc.getDirections().setDirection(direction); // Set the previous direction for logic reasons
                updateCoordinateFuture(); // Update the coordinate future

                secondDirection = next(); // Calculate the second direction, for running.
            }

            npc.getDirections().setDirection(direction); // Set the final direction.
            npc.getDirections().setSecondDirection(secondDirection);
        } 
        npc.setPreviousLocation(oldLocation);
        updateCoordinateFuture();
    }
}
