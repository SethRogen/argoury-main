package com.ziotic.adapter.protocol.handler;

import com.ziotic.Static;
import com.ziotic.logic.item.GroundItem;
import com.ziotic.logic.map.CoordinateFuture;
import com.ziotic.logic.map.PathProcessor;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.map.pf.astar.AStarPathFinder;
import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.handler.PlayerFrameHandler;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class GroundItemOptionHandler extends PlayerFrameHandler {

    @Override
    public void handleFrame(final Player player, IoSession session, Frame frame) {
        boolean ctrl = frame.readUnsigned() == 1;

        int y = frame.readUnsignedShortA();
        int x = frame.readUnsignedShortA();

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
                        handleItem(player, id, loc);
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
                handleItem(player, id, loc);
                player.faceDirection(Tile.locate(x, y, player.getZ()));
                // TODO animation player.doAnimation(id)
            } else {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        handleItem(player, id, loc);
                    }
                };
                Static.world.submitPath(new AStarPathFinder(), player, x, y, null, ctrl ? PathProcessor.MOVE_SPEED_RUN : PathProcessor.MOVE_SPEED_ANY, false, null);
                player.getPathProcessor().setCoordinateFuture(new CoordinateFuture(player, r));
            }
        } else {
            handleItem(player, id, loc);
        }
    }

    private void handleItem(Player player, int id, Tile loc) {
        player.resetEvents();

        GroundItem item = Static.world.getGroundItemManager().get(id, loc);
        if (item != null) {
            Static.ahs.handleGroundItemOption(player, item);
        }
    }
}
