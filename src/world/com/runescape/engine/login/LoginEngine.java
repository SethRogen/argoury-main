package com.runescape.engine.login;

import com.runescape.Constants;
import com.runescape.Static;
import com.runescape.link.WorldClientSession;
import com.runescape.logic.map.Tile;
import com.runescape.logic.player.Player;
import com.runescape.logic.player.PlayerType;
import com.runescape.utility.Logging;
import com.runescape.utility.Text;

import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Lazaro
 */
public class LoginEngine implements Runnable {
    private static final Logger logger = Logging.log();

    public static final int LOGIN_WORKERS = 8;
    public static final int LOGIN_REQUEST_TIME_OUT = 5000;

    public static class LoginWorker implements Runnable {
        public LoginEngine ctx;
        public int id;
        public Thread thread;
        public LoginRequest currentRequest = null;

        public LoginWorker(LoginEngine ctx) {
            this.ctx = ctx;
        }

        public void run() {
            WorldClientSession session = null;
            while (true) {
                currentRequest = null;
                try {
                    currentRequest = ctx.loginRequests.take();
                    if (System.currentTimeMillis() - currentRequest.time >= LOGIN_REQUEST_TIME_OUT) {
                        continue;
                    }

                    session = Static.world.getWCSPool().acquire();

                    Player player = new Player(currentRequest.session, currentRequest.opcode);
                    player.setName(Text.formatNameForDisplay(currentRequest.name));
                    player.setProtocolName(currentRequest.name);
                    player.setPassword(currentRequest.password);

                    LoginResponse resp = session.loadPlayerDetails(player);

                    // TODO Do checks here (passwords, bans)
                    if (resp == LoginResponse.LOGIN) {
                        PlayerType p2 = Static.world.findPlayer(player.getProtocolName());
                        if (p2 != null) {
                            if (!(p2 instanceof Player) || !((Player) p2).isDestroyed()) {
                                resp = LoginResponse.ALREADY_ONLINE;
                                // TODO Allow for reconnections
                            }
                        }
                    }
                    // Final check
                    if (resp == LoginResponse.LOGIN) {
                        resp = Static.world.register(player);
                        if (resp == LoginResponse.LOGIN) {
                            session.registerPlayer(player);

                            player.getSession().setAttribute("player", player);
                        }
                    }
                    if (player.inGame()) {
                        Static.proto.sendLoginResponse(currentRequest.session, player, resp);
                    } else {
                        Static.proto.sendLobbyResponse(currentRequest.session, player, resp);
                    }
                    if (resp == LoginResponse.LOGIN) {
                        if (player.inGame()) {
                            if (player.getAttributes().isSet("loc")) {
                                player.setLocation(player.getAttributes().<Tile>get("loc"));
                                int depth = Constants.REGION_SIZE[0] >> 4;
                                for (int xCalc = (player.getLocation().getPartX() - depth) / 8; xCalc <= (player.getLocation().getPartX() + depth) / 8; xCalc++) {
                                    for (int yCalc = (player.getLocation().getPartY() - depth) / 8; yCalc <= (player.getLocation().getPartY() + depth) / 8; yCalc++) {
                                        int region = yCalc + (xCalc << 8);
                                        int[] key = Static.mapXTEA.getKey(region);
                                        if (key == null) {
                                            //TODO player.setLocation(player.spawnPoint());
                                            //When you stayed at an area with no encryption you would go back to the spawn point
                                        }
                                    }
                                }
                                player.getAttributes().unSet("loc");
                            } else {
                                player.setNoob(true);
                                player.setLocation(player.spawnPoint());
                            }
                            player.gei.init();

                            Static.proto.sendOnLogin(player);
                        }
                        player.onLogin();
                        player.setLastIP(((InetSocketAddress) player.getSession().getRemoteAddress()).getAddress().getHostAddress());
                    }
                } catch (Exception e) {
                    logger.debug("Error caught in login worker!", e);
                    e.printStackTrace();
                } finally {
                    if (session != null) {
                        Static.world.getWCSPool().release(session);
                        session = null;
                    }
                }
            }
        }
    }

    private Thread loginEngineThread;
    private BlockingQueue<LoginRequest> loginRequests = new LinkedBlockingQueue<LoginRequest>();
    private List<LoginWorker> loginWorkers = new ArrayList<LoginWorker>();

    public LoginEngine() {
        for (int i = 1; i <= LOGIN_WORKERS; i++) {
            LoginWorker worker = new LoginWorker(this);
            loginWorkers.add(worker);
            worker.id = i;
            worker.thread = new Thread(worker, "login-worker-" + i);
            worker.thread.start();
        }
        loginEngineThread = new Thread(this, "login-engine");
        loginEngineThread.start();
    }

    @SuppressWarnings("deprecation")
    public void run() {
        while (true) {
            try {
                for (LoginWorker worker : loginWorkers) {
                    LoginRequest req = worker.currentRequest;
                    if (req == null) {
                        continue;
                    }
                    if (System.currentTimeMillis() - req.time >= LOGIN_REQUEST_TIME_OUT) {
                        worker.thread.stop();
                        Static.proto.sendLoginResponse(req.session, null, LoginResponse.ERROR);
                        worker.currentRequest = null;
                        worker.thread = new Thread(worker, "login-worker-" + worker.id);
                        worker.thread.start();
                        logger.debug("Restarted login worker #" + worker.id);
                    }
                }
            } catch (Exception e) {
                logger.error("Error caught in login engine!", e);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public void submit(LoginRequest req) {
        if (!loginRequests.offer(req)) {
            Static.proto.sendLoginResponse(req.session, null, LoginResponse.ERROR);
        }
    }
}
