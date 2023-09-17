package com.ziotic.engine.misc;

import com.ziotic.Static;
import com.ziotic.link.WorldClientSession;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Lazaro
 */
public class ProgressivePlayerSaver implements Runnable {
    private static final Logger logger = Logging.log();

    private Thread thread;

    public ProgressivePlayerSaver() {
        thread = new Thread(this, "auto-save");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    @Override
    public void run() {
        WorldClientSession session = null;
        while (true) {
            try {
                session = Static.world.getWCSPool().acquire();

                List<Player> cachedPlayerList = new LinkedList<Player>(Static.world.getPlayers());
                for (Player player : cachedPlayerList) {
                    if (player != null && player.isConnected() && !player.isOnLogin()) {
                        if (!session.savePlayerDetails(player)) {
                            logger.warn("Failed to auto-save player [name=" + player.getName() + "]!");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error caught in player saver!", e);
            } finally {
                if (session != null) {
                    Static.world.getWCSPool().release(session);
                    session = null;
                }

                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
