package com.runescape.content.misc;

import com.runescape.Static;
import com.runescape.content.handler.ActionHandler;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ButtonHandler;
import com.runescape.content.handler.ObjectOptionHandler;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.map.PathProcessor;
import com.runescape.logic.map.Tile;
import com.runescape.logic.mask.Movement;
import com.runescape.logic.object.GameObject;
import com.runescape.logic.player.Player;
import com.runescape.logic.utility.GameInterface;
import com.runescape.logic.utility.NodeRunnable;
import com.runescape.utility.Logging;

import org.apache.log4j.Logger;

/**
 * @author Lazaro
 */
public class Wilderness implements ActionHandler, ObjectOptionHandler, ButtonHandler {
    private static final Logger logger = Logging.log();

    public static final int[] WILDERNESS_WALL_IDS = new int[]{
            1440, 1441, 1442, 1443, 1444
    };

    public static final GameInterface WILDERNESS_WARNING_SCREEN = new GameInterface(382, new NodeRunnable<Player>() {
        @Override
        public void run(Player player) {
            Static.proto.sendConfig(player, 1045, 3146752);
        }
    });

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        system.registerObjectOptionHandler(WILDERNESS_WALL_IDS, this);
        system.registerButtonHandler(new int[]{WILDERNESS_WARNING_SCREEN.getId()}, this);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleObjectOption1(final Player player, GameObject obj) {
        // TODO Disabled until proper warning frames are found
        //if(!player.getAttributes().is("wilderness_warned")) {
          //  Static.proto.sendInterface(player, WILDERNESS_WARNING_SCREEN);
        //} else {
        handleJump(player);
       // }
    }

    @Override
    public void handleObjectOption2(Player player, GameObject obj) {
    }

    @Override
    public void handleObjectOption3(Player player, GameObject obj) {
    }
    
    
    private void handleJump(final Player player) {
        player.getPathProcessor().reset(true);
        player.getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_WALK);
        final int yOffset = player.getY() >= 3523 ? -3 : 3;
        final Tile dest = player.getLocation().translate(0, yOffset, 0);
        final int direction = yOffset < 0 ? 2 : 0;
        player.faceDirection(dest);
        player.registerTick(new Tick("wilderness_wall") {
            @Override
            public boolean execute() {
                player.doAnimation(6703);
                player.getMasks().setMovement(new Movement(0, 0, 0, player.getY() >= 3523 ? -3 : 3, 30, 60, direction));
                player.registerTick(new Tick("wilderness_wall2") {
                    @Override
                    public boolean execute() {
                        player.getPathProcessor().reset(true);
                        player.setTeleportDestination(dest);
                        return false;
                    }
                });
                return false;
            }
        });
    }
    @Override
    public void handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3) {
        logger.debug("Unhandled button [interface=" + interfaceId + ", button=" + b + ", button2=" + b2 + ", button3=" + b3 + ", opcode=" + opcode + "]");
    }
}
