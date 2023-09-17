package com.ziotic.adapter.protocol.handler;

import com.ziotic.Static;
import com.ziotic.content.grotto.ClanWars;
import com.ziotic.content.misc.Follow;
import com.ziotic.content.trading.TradingManager;
import com.ziotic.logic.map.Areas;
import com.ziotic.logic.map.CoordinateFuture;
import com.ziotic.logic.map.PathProcessor;
import com.ziotic.logic.map.pf.astar.AStarPathFinder;
import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.handler.PlayerFrameHandler;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class PlayerOptionHandler extends PlayerFrameHandler {
    @Override
    public void handleFrame(final Player player, IoSession session, Frame frame) {
        final int index;
        final int playerIndex;
        final boolean ctrl;

        switch (frame.getOpcode()) {
            case 7:
                index = 1;
                ctrl = frame.readUnsignedC() == 1;
                playerIndex = frame.readUnsignedShortA();
                break;
            case 19:
                index = 2;
                ctrl = frame.readUnsigned() == 1;
                playerIndex = frame.readShortA();
                break;
            case 32:
                index = 0;
                playerIndex = frame.readUnsignedShort();
                ctrl = frame.readUnsignedS() == 1;
                break;
            case 59:
                index = 2;
                playerIndex = frame.readShortA();
                ctrl = frame.readUnsignedC() == 1;
                break;
            default:
                return;
        }

        final Player player2 = Static.world.getPlayers().get(playerIndex);
        if (player2 == null || !player2.isConnected()) {
            return;
        }

        if (player.getPathProcessor().getMoveSpeed() == PathProcessor.MOVE_SPEED_WALK) {
            return;
        }

        if (index == 0) {
            //player.resetEvents();
            player.faceEntity(player2);
            if (Areas.GROTTO.inArea(player.getLocation())) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        CoordinateFuture cF = new CoordinateFuture(player, player2, new Runnable() {
                            public void run() {
                                ClanWars.initateChallange(player, player2);
                            }
                        });
                        player.getPathProcessor().setCoordinateFuture(cF);
                        player.getPathProcessor().updateCoordinateFuture();
                        if (player.getPathProcessor().getCoordinateFuture() != null) {
                            Static.world.submitPath(new AStarPathFinder(), player, player2.getX(), player2.getY(), player, ctrl ? PathProcessor.MOVE_SPEED_RUN : PathProcessor.MOVE_SPEED_ANY, false, null);
                            player.getPathProcessor().setCoordinateFuture(cF);
                        }
                    }
                };
                Static.engine.dispatchToMapWorker(r);
            } else
                player.getCombat().createNewCombatAction(player, player2, false, -1, -1);
        } else if (index == 1) {
            if (!player.getCombat().isFrozen())
                player.registerTick(new Follow(player, player2));
            else {
                player.sendMessage("A magical force stops you from moving.");
                player.faceEntity(player2);
            }
        } else {
            if (!player.getCombat().isFrozen()) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Runnable r2 = new Runnable() {
                            @Override
                            public void run() {
                                switch (index) {
                                    case 2:
                                        TradingManager.requestTrade(player2, player);
                                        break;
                                }
                            }
                        };
                        CoordinateFuture cF = new CoordinateFuture(player, player2, r2);
                        player.getPathProcessor().setCoordinateFuture(cF);
                        player.getPathProcessor().updateCoordinateFuture();
                        if (player.getPathProcessor().getCoordinateFuture() != null) {
                            Static.world.submitPath(new AStarPathFinder(), player, player2.getX(), player2.getY(), player, ctrl ? PathProcessor.MOVE_SPEED_RUN : PathProcessor.MOVE_SPEED_ANY, false, null);
                            player.getPathProcessor().setCoordinateFuture(cF);
                        }
                    }
                };
                Static.engine.dispatchToMapWorker(r);
            } else {
                player.sendMessage("A magical force stops you from moving.");
                player.faceEntity(player2);
            }
        }
    }
}
