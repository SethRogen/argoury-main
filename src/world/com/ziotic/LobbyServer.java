package com.ziotic;

import com.ziotic.adapter.protocol.HandshakeCodec;
import com.ziotic.adapter.protocol.ProtocolAdapter;
import com.ziotic.adapter.protocol.cache.RS2CacheAdapter;
import com.ziotic.engine.Engine;
import com.ziotic.logic.World;
import com.ziotic.network.ConnectionHandler;
import com.ziotic.network.handler.FrameHandlerManager;
import com.ziotic.network.handler.FrameWorker;
import com.ziotic.utility.Logging;
import com.ziotic.utility.script.JavaScriptManager;
import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.util.AvailablePortFinder;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Lazaro
 */
public class LobbyServer implements Application {
    private static Logger logger = Logging.log();

    private IoAcceptor acceptor;

    public void main(String[] args) throws Throwable {
        Static.appType = AppType.LOBBY;
        Static.engine = new Engine();
        Static.js = new JavaScriptManager();
        Static.frameManager = new FrameHandlerManager();
        Static.rs2Cache = new RS2CacheAdapter();
        Static.rs2Cache.load(new File(Static.parseString("%WORK_DIR%/rs2cache")));
        Static.world = new World(Integer.parseInt(args[0]));
        Static.world.load();
        Static.proto = new ProtocolAdapter();
        Static.engine.getAsyncLogicExecutor().scheduleAtFixedRate(new FrameWorker(), 50, Constants.FAST_GAME_TICK_INTERVAL, TimeUnit.MILLISECONDS);
        Static.engine.start();
        logger.info("Started engine");
        initNetworking();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                acceptor.unbind();
                logger.info("Unbound all ports");
            }
        });
    }

    public void initNetworking() throws Throwable {
        int[] lobbyPorts = Static.conf.getIntArray("lobby_ports");

        acceptor = new NioSocketAcceptor();
        acceptor.setHandler(new ConnectionHandler());
        acceptor.setFilterChainBuilder(new IoFilterChainBuilder() {
            @Override
            public void buildFilterChain(IoFilterChain chain) throws Exception {
                chain.addLast("codec", HandshakeCodec.FILTER);
            }
        });

        SocketSessionConfig config = (SocketSessionConfig) acceptor.getSessionConfig();
        config.setIdleTime(IdleStatus.BOTH_IDLE, 30);
        config.setTcpNoDelay(true);
        config.setKeepAlive(true);

        if(Static.isLobby()) {
            for (int p : lobbyPorts) {
                bind(p);
            }
            logger.info("Bound lobby port(s) " + Arrays.toString(lobbyPorts));
        }
    }

    private void bind(int port) {
        while(true) {
            try {
                acceptor.bind(new InetSocketAddress(port));
                break;
            } catch(IOException e) {
                logger.debug("Reattempting to bind port " + port + " in 5 seconds");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e2) {
                }
            }
        }
    }
}
