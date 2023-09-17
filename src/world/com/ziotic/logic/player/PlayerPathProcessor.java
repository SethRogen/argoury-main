/**
 *
 */
package com.ziotic.logic.player;

import java.awt.Point;

import com.ziotic.Static;
import com.ziotic.content.combat.Combat;
import com.ziotic.logic.map.Areas;
import com.ziotic.logic.map.Directions;
import com.ziotic.logic.map.Directions.NormalDirection;
import com.ziotic.logic.map.PathProcessor;
import com.ziotic.logic.map.Tile;

/**
 * @author Lazaro
 */
public class PlayerPathProcessor extends PathProcessor {
    private Player player;
    
    public PlayerPathProcessor(Player player) {
        super(player);
        this.player = player;
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
             * Check if this tile is a place holder (used for delays).
             */
            if (nextPoint != null && nextPoint.equals(Tile.PLACE_HOLDER)) {
                /**
                 * Remove the place holder.
                 */
                buffer.next();
                return null;
            }

            /**
             * Check if we are already on this tile.
             */
            if (player.getLocation().equals(nextPoint)) {
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
                return null;
            }

            /**
             * Calculate the directions of the next waypoint.
             */
            NormalDirection direction = Directions.directionFor(player.getLocation(), nextPoint);
            /**
             * We want to move.
             */
            if (direction != null) {
                if (player.isClipping() && !player.getLocation().canMove(direction, player.getSize(), false)) {
                    reset();
                    return null;
                }

                /**
                 * Calculate the next tile.
                 */
                Tile next = player.getLocation().translate(Directions.DIRECTION_DELTA_X[direction.intValue()], Directions.DIRECTION_DELTA_Y[direction.intValue()], 0);
                /**
                 * Check if the map region is going to change.
                 */
                if (player.getMapRegionUpdatePosition().differentMap(next)) {
                    /**
                     * Flag the map changing.
                     */
                    player.setMapRegionUpdate(true);
                }
                if (next.getRegionX() != player.getLocation().getRegionX() || next.getRegionY() != player.getLocation().getRegionY()) {
                    player.setMapRegionDirection(Directions.directionFor(new Point(player.getLocation().getRegionX(), player.getLocation().getRegionY()), new Point(next.getRegionX(), next.getRegionY())));
                }
                /**
                 * Set the tile and return the directions.
                 */
                player.setLocation(next);
                player.updateCoverage(next);

                updateHistory(next);

                return direction;
            }
        }
        return null;
    }

    @Override
    public void process() {
        Tile oldLocation = player.getLocation(); // Save the old location.
        if (player.getTeleportDestination() == null && moving() && !player.getCombat().isFrozen()) { // Check if we are moving.
            Directions.Direction direction = next(); // Calculate the first direction, for walking.
            Directions.Direction secondDirection = null;
            if (moveSpeed == MOVE_SPEED_RUN || (moveSpeed != MOVE_SPEED_WALK && player.isRunning())) { // Check if we are running.
                if (player.getRunningEnergy() > 0) { // Check if the player has enough energy to run.
                    player.getDirections().setDirection(direction); // Set the previous direction for logic reasons
                    updateCoordinateFuture(); // Update the coordinate future

                    secondDirection = next(); // Calculate the second direction, for running.
                    if (secondDirection != null) {
                        player.setRunningEnergy(player.getRunningEnergy() - 1); // Decrease energy.
                        Static.proto.sendRunEnergy(player);
                    }
                } else {
                    moveSpeed = MOVE_SPEED_ANY; // Stop running this buffer.
                    player.setRunning(false); // Stop the player from running.
                    Static.proto.sendConfig(player, 173, 0);
                }
            }
            if (secondDirection != null) {
                direction = Directions.runningDirectionFor(new Point(oldLocation.getX(), oldLocation.getY()), new Point(player.getLocation().getX(), player.getLocation().getY()));
                if (direction == null) {
                    direction = Directions.directionFor(new Point(oldLocation.getX(), oldLocation.getY()), new Point(player.getLocation().getX(), player.getLocation().getY()));
                }
            }
            int newMovementMode = secondDirection != null ? 2 : 1;
            if (newMovementMode != player.getMovementMode()) {
                player.setMovementMode(newMovementMode);
                player.getMasks().setMovementModeUpdate(true);
            }
            player.getDirections().setDirection(direction); // Set the final direction.
            player.getDirections().setSecondDirection(secondDirection);
        }
        updateCoordinateFuture();

        if (player.getDirections().getDirection() == null && player.getTeleportDestination() != null) {
            teleport();
        }

        if (!player.isInPVP() && Combat.isInPVPZone(player)) {
            player.setInPVP(true);
            Combat.updatePVPStatus(player);

            if (player.getAppearance().isNPC()) {
                player.getAppearance().toPlayer();
                player.getAppearance().refresh();
            }
        } else if (player.isInPVP() && !Combat.isInPVPZone(player)) {
            player.setInPVP(false);
            Combat.updatePVPStatus(player);
        }
        if (!player.isInGrotto() && Areas.GROTTO.inArea(player.getLocation())) {
            Static.proto.sendPlayerOption(player, "Challenge", 1, true);
            player.setInGrotto(true);
        } else if (player.isInGrotto() && !Areas.GROTTO.inArea(player.getLocation())) {
            Static.proto.sendPlayerOption(player, "null", 1, true);
            player.setInGrotto(false);
        }
        if (!player.isInMulti() && Combat.isInMultiZone(player)) {
            Static.proto.sendInterfaceShowConfig(player, 745, 1, false);
            player.setInMulti(true);
        } else if (player.isInMulti() && !Combat.isInMultiZone(player)) {
            Static.proto.sendInterfaceShowConfig(player, 745, 1, true);
            player.setInMulti(false);
        }


        if (!player.getCombat().isInWilderness() && player.getLocation().wildernessLevel() > 0) {
            Static.proto.sendInterfaceShowConfig(player, 381, 0, false);
            player.getCombat().setInWilderness(true);
        } else if (player.getCombat().isInWilderness() && player.getLocation().wildernessLevel() <= 0) {
            Static.proto.sendInterfaceShowConfig(player, 381, 0, true);
            player.getCombat().setInWilderness(false);
        }

        player.setPreviousLocation(oldLocation);
    }

    private void teleport() {
        Tile oldLocation = player.getLocation(); // Save the old location.
        player.setLocation(player.getTeleportDestination()); // Set the new location.
        player.updateCoverage(player.getTeleportDestination()); // Set the new tile coverage
        player.setTeleportDestination(null); // Reset the teleport variables.
        player.setTeleporting(true); // Flag the teleport.
        player.setMapRegionUpdate(!player.isForcedTeleporting() && oldLocation.differentMap(player.getLocation())); // Flag if the map has changed.
        if (player.getLocation().getZ() != oldLocation.getZ()) {
            player.setHeightUpdate(true);
        }
        if (!player.isForcedTeleporting())
            reset(); // Reset the waypoint buffer.
        player.getMasks().setForcedMovementMode(127);
    }
}
