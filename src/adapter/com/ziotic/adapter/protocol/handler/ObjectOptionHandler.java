package com.ziotic.adapter.protocol.handler;

import com.ziotic.Static;
import com.ziotic.logic.map.CoordinateFuture;
import com.ziotic.logic.map.PathProcessor;
import com.ziotic.logic.map.Region;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.map.pf.astar.AStarPathFinder;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.handler.PlayerFrameHandler;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class ObjectOptionHandler extends PlayerFrameHandler {
    private static final Logger logger = Logging.log();

    @Override
    public void handleFrame(final Player player, IoSession session, Frame frame) {
        final int x, y;
        final int objId;
        final boolean ctrl;
        final int index;

        switch (frame.getOpcode()) {
            case 5:
                index = 1;
                y = frame.readUnsignedShortA();
                x = frame.readUnsignedLEShort();
                objId = frame.readUnsignedLEShort();
                ctrl = frame.readUnsignedA() == 1;
                break;
            case 12:
                index = 2;
                ctrl = frame.readUnsignedC() == 1;
                x = frame.readUnsignedShortA();
                y = frame.readUnsignedLEShort();
                objId = frame.readUnsignedLEShortA();
                break;
            default:
                return;
        }

        if (player.getPathProcessor().getMoveSpeed() == PathProcessor.MOVE_SPEED_WALK) {
            return;
        }
        player.getCombat().stop(false);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                final Tile loc = Tile.locate(x, y, player.getZ());
                final GameObject obj = Region.getObject(loc);
                if (obj == null || obj.getId() != objId) {
                    // Possible cheat
                    logger.warn("Possible object option cheat [objId=" + objId + ", loc=[" + loc + "]]");
                    return;
                }

                Runnable r2 = new Runnable() {
                    @Override
                    public void run() {
                        if (!Static.ahs.handleObjectOption(player, obj, index)) {
                            switch (index) {
                                case 1:
                                    Static.callScript("objects.handleObjectOption1", player, objId, obj);
                                    break;
                                case 2:
                                    Static.callScript("objects.handleObjectOption2", player, objId, obj);
                                    break;
                            }
                        }
                    }
                };
                CoordinateFuture cF = new CoordinateFuture(player, obj, r2);
                player.getPathProcessor().setCoordinateFuture(cF);
                player.getPathProcessor().updateCoordinateFuture();
                if (player.getPathProcessor().getCoordinateFuture() != null) {
                    if (!player.getCombat().isFrozen()) {
                        Static.world.submitPath(new AStarPathFinder(), player, x, y, obj, ctrl ? PathProcessor.MOVE_SPEED_RUN : PathProcessor.MOVE_SPEED_ANY, false, null);
                    } else {
                        player.sendMessage("A magical force stops you from moving.");
                        player.faceDirection(Tile.locate(x, y, player.getZ()));
                    }
                } else {
                    player.faceDirection(Tile.locate(x, y, player.getZ()));
                }
            }
        };
        Static.engine.dispatchToMapWorker(r);
    }
}
