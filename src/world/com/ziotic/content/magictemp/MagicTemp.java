package com.ziotic.content.magictemp;

import com.ziotic.Static;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.mask.Graphic;
import com.ziotic.logic.mask.Mask;
import com.ziotic.logic.player.Player;

/**
 * @author Lazaro
 *
 */
public class MagicTemp {
    public static boolean teleport(final Player player, final Tile location, int delay, int startAnim, final int endAnim, Graphic startGraphic, final Graphic endGraphic, final int exp, boolean lever) {
        if (player.isTeleporting()) {
            return false;
        }
        if (player.teleBlocked) {
        	Static.proto.sendMessage(player, "You are teleblocked!");
        	return false;
        }
        if (player.getLocation().wildernessLevel() > 20 && !lever) {
            Static.proto.sendMessage(player, "You cannot teleport in over 20 wilderness.");
            return false;
        }
        
//        player.resetEvents();
        
        if(startAnim != Mask.MASK_NULL) player.doAnimation(startAnim);
        if(startGraphic != null) player.doGraphics(startGraphic);

        player.registerTick(new Tick("teleport", delay) {
            @Override
            public boolean execute() {
                player.setTeleportDestination(location);
                if(endAnim != Mask.MASK_NULL) player.doAnimation(endAnim);
                if(endGraphic != null) player.doGraphics(endGraphic);
                return false;
            }
        });
        return true;
    }
}
