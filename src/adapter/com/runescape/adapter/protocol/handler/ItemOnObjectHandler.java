package com.runescape.adapter.protocol.handler;

import org.apache.log4j.Logger;

import com.runescape.Static;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.map.CoordinateFuture;
import com.runescape.logic.map.PathProcessor;
import com.runescape.logic.map.Region;
import com.runescape.logic.map.Tile;
import com.runescape.logic.map.pf.astar.AStarPathFinder;
import com.runescape.logic.object.GameObject;
import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;
import com.runescape.utility.Logging;

import org.apache.mina.core.session.IoSession;

/**
 * @author Maxi
 */
public class ItemOnObjectHandler extends PlayerFrameHandler {
	
	private final static Logger LOGGER = Logging.log();
	
	@Override
	public void handleFrame(final Player player, IoSession session, Frame frame) {
		final int y = frame.readShort();
		final int x = frame.readLEShortA();
		final int itemId = frame.readShort();
		final boolean ctrl = frame.readC() == 1;
		final int objId = frame.readShortA();
		final int interfaceBitset = frame.readLEInt();
		final int interfaceId = interfaceBitset >> 16;
		@SuppressWarnings("unused")
		final int interfaceChildId = interfaceBitset & 0xffff;
		final int itemSlot = frame.readLEShort();
		
        if (player.getPathProcessor().getMoveSpeed() == PathProcessor.MOVE_SPEED_WALK) {
            return;
        }
        player.getCombat().stop(false);
        
        Runnable r = new Runnable() {
            @Override
            public void run() {
                final Tile loc = Tile.locate(x, y, player.getZ());
                final GameObject obj = Region.getObject(loc);
                final PossesedItem item = player.getInventory().get(itemSlot);
                if (interfaceId != 149) {
                	LOGGER.warn("ItemOnObject performed with an item from a different interface than the inventory.");
                	return;
                }
                if (item == null || itemId != item.getId()) {
                	LOGGER.warn("ItemOnObject - Possible cheat client active item wise.");
                	return;
                }
                if (obj == null || objId != obj.getId()) {
                	LOGGER.warn("ItemOnObject - Possible cheat client active object wise.");
                	return;
                }
                Runnable r2 = new Runnable() {
                    @Override
                    public void run() {
                        if (!Static.ahs.handleItemOnObject(player, item, itemSlot, obj)) {
                        	LOGGER.info("Unhandled ItemOnObject [" + item.getId() + ", " + obj.getId() + "]");
                        }
                    }
                };
                CoordinateFuture cF = new CoordinateFuture(player, obj, r2);
                player.getPathProcessor().setCoordinateFuture(cF);
                player.getPathProcessor().updateCoordinateFuture();
                if (player.getPathProcessor().getCoordinateFuture() != null) {
                	if (!player.getCombat().isFrozen()) {
	                    Static.world.submitPath(new AStarPathFinder(), player, x, y, obj, ctrl ? PathProcessor.MOVE_SPEED_RUN : PathProcessor.MOVE_SPEED_ANY, false, null);
	                    player.getPathProcessor().setCoordinateFuture(cF);
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
