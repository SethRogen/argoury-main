package com.runescape.adapter.protocol.handler;

import com.runescape.Static;
import com.runescape.logic.item.GroundItem;
import com.runescape.logic.map.CoordinateFuture;
import com.runescape.logic.map.PathProcessor;
import com.runescape.logic.map.Tile;
import com.runescape.logic.map.pf.astar.AStarPathFinder;
import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;

import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class PickupItemHandler extends PlayerFrameHandler {
    @Override
    public void handleFrame(final Player player, IoSession session, Frame frame) {
        boolean ctrl = frame.readUnsignedA() == 1;
        int x = frame.readUnsignedShort();
        int y = frame.readUnsignedLEShort();
        final int id = frame.readUnsignedLEShort();

        if (player.getPathProcessor().getMoveSpeed() == PathProcessor.MOVE_SPEED_WALK) {
            return;
        }

        Static.proto.sendCloseInterface(player);

        final Tile loc = Tile.locate(x, y, player.getZ());
        final int dx = Math.abs(player.getLocation().getX() - x);
        final int dy = Math.abs(player.getLocation().getY() - y);
        player.getCombat().stop(false);
        if (player.getLocation().distance(loc) > 1) {
            if (!player.getCombat().isFrozen()) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        pickupItem(player, id, loc);
                    }
                };
                Static.world.submitPath(new AStarPathFinder(), player, x, y, null, ctrl ? PathProcessor.MOVE_SPEED_RUN : PathProcessor.MOVE_SPEED_ANY, false, null);
                player.getPathProcessor().setCoordinateFuture(new CoordinateFuture(player, r));
            } else {
                player.sendMessage("A magical force stops you from moving.");
                player.faceDirection(Tile.locate(x, y, player.getZ()));
            }
        } else if ((dx == 1 && dy == 0) || (dx == 0 && dy == 1)) {
            if (player.getCombat().isFrozen()) {
                pickupItem(player, id, loc);
                player.faceDirection(Tile.locate(x, y, player.getZ()));
            } else {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        pickupItem(player, id, loc);
                    }
                };
                Static.world.submitPath(new AStarPathFinder(), player, x, y, null, ctrl ? PathProcessor.MOVE_SPEED_RUN : PathProcessor.MOVE_SPEED_ANY, false, null);
                player.getPathProcessor().setCoordinateFuture(new CoordinateFuture(player, r));
            }
        } else {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    pickupItem(player, id, loc);
                }
            };
            Static.world.submitPath(new AStarPathFinder(), player, x, y, null, ctrl ? PathProcessor.MOVE_SPEED_RUN : PathProcessor.MOVE_SPEED_ANY, false, null);
            player.getPathProcessor().setCoordinateFuture(new CoordinateFuture(player, r));
        }
    }

    private void pickupItem(Player player, int id, Tile loc) {
        player.resetEvents();

        GroundItem item = Static.world.getGroundItemManager().get(id, loc);
        if (item != null) {
            if (player.getInventory().add(item.getId(), item.getDefinition().isStackable() ? item.getAmount() : 1)) {
                Static.world.getGroundItemManager().remove(item);
            }
        }
    }
}
