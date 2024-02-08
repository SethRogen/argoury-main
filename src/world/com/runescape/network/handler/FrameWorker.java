/**
 *
 */
package com.runescape.network.handler;

import com.runescape.Static;
import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.utility.Logging;

import org.apache.log4j.Logger;

/**
 * @author Lazaro
 */
public class FrameWorker implements Runnable {
    private static final Logger logger = Logging.log();
    /* (non-Javadoc)
      * @see java.lang.Runnable#run()
      */

    @Override
    public void run() {
        try {
            if (Static.isGame()) {
                for (Object playerObj : Static.world.getPlayers().toArray()) {
                    if (playerObj != null) handlePlayer((Player) playerObj);
                }
            }
            if (Static.isLobby()) {
                for (Object playerObj : Static.world.getLobbyPlayers().toArray()) {
                    if (playerObj != null) handlePlayer((Player) playerObj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePlayer(Player player) {
        boolean[] frameFlag = player.getFrameHistory();
        if (player.isConnected() && !player.getFrameQueue().isEmpty()) {
            for (Frame f = player.getFrameQueue().poll(); f != null; f = player.getFrameQueue().poll()) {
                int opcode = f.getOpcode();
                if (frameFlag[opcode]) {
                    continue;
                }

                try {
                    Static.frameManager.getHandler(opcode).handleFrame(player.getSession(), f);
                } catch (Exception e) {
                    logger.error("Error handling frame [opcode:" + f.getOpcode() + "]", e);
                }
            }
        }
    }
}
