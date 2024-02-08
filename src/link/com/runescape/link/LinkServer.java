package com.runescape.link;

import com.runescape.Application;
import com.runescape.Static;
import com.runescape.adapter.DatabaseLoader;
import com.runescape.adapter.db.DatabaseLoaderAdapter;
import com.runescape.adapter.protocol.HandshakeCodec;
import com.runescape.adapter.protocol.ProtocolAdapter;
import com.runescape.content.cc.Clan;
import com.runescape.content.cc.ClanManager;
import com.runescape.engine.Engine;
import com.runescape.io.sql.SQLInitiator;
import com.runescape.io.sql.SQLSession;
import com.runescape.link.network.LConnectionHandler;
import com.runescape.link.network.LFrameDispatcher;
import com.runescape.link.network.LinkCodec;
import com.runescape.link.network.WorldListEvent;
import com.runescape.logic.player.PlayerType;
import com.runescape.logic.player.RemotePlayer;
import com.runescape.network.Frame;
import com.runescape.network.FrameBuilder;
import com.runescape.network.Frame.FrameType;
import com.runescape.network.handler.FrameHandlerManager;
import com.runescape.utility.Logging;
import com.runescape.utility.Pool;
import com.runescape.utility.Text;
import com.runescape.utility.script.JavaScriptManager;

import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.util.AvailablePortFinder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * @author Lazaro
 */
public class LinkServer implements Application {
    private static final Logger logger = Logging.log();

    public static final int OK_RESP = 1;
    public static final int INVALID_PASSWORD_RESP = 2;

    private IoAcceptor acceptor;

    private Map<Integer, WorldEntry> worldList = null;

    private Map<Integer, WorldServerSession> lobbies = new HashMap<Integer, WorldServerSession>();
    private Map<Integer, WorldServerSession> games = new HashMap<Integer, WorldServerSession>();
    private Map<String, RemotePlayer> players = new HashMap<String, RemotePlayer>();

    private Pool<SQLSession> sqlPool = null;
    private DatabaseLoader databaseLoader = new DatabaseLoaderAdapter();

    public void main(String[] args) throws Throwable {
        Static.appType = AppType.LINK;
        Static.engine = new Engine();
        Static.js = new JavaScriptManager();

        sqlPool = new Pool<SQLSession>(SQLSession.class, new SQLInitiator(Static.conf), 4);
        logger.info("Database connections pooled");
        databaseLoader.reload();
        logger.info("Database loaded");
        worldList = Static.xml.readObject(Static.parseString("%WORK_DIR%/world/worlds.xml"));
        logger.info("World-list loaded");
        Static.proto = new ProtocolAdapter();
        Static.frameManager = new FrameHandlerManager();
        Static.engine.submit(new WorldListEvent());
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
        int port = Static.conf.getInt("link_port");

        assert AvailablePortFinder.available(port) : "Link ports are not available!";

        acceptor = new NioSocketAcceptor();
        acceptor.setHandler(new LConnectionHandler());
        acceptor.setFilterChainBuilder(new IoFilterChainBuilder() {
            @Override
            public void buildFilterChain(IoFilterChain chain) throws Exception {
                chain.addLast("codec", LinkCodec.FILTER);
                chain.addLast("dispatcher", LFrameDispatcher.INSTANCE);
            }
        });

        SocketSessionConfig config = (SocketSessionConfig) acceptor.getSessionConfig();
        config.setIdleTime(IdleStatus.BOTH_IDLE, 30);
        config.setTcpNoDelay(true);
        config.setKeepAlive(true);

        bind(port);
        logger.info("Bound link port " + port);
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

    public Map<Integer, WorldEntry> getWorldList() {
        return worldList;
    }

    public WorldServerSession getLobby(int worldId) {
        synchronized (lobbies) {
            WorldServerSession world = lobbies.get(worldId);
            if (world == null) {
                synchronized (games) {
                    world = games.get(worldId);
                    if (world == null || world.getServerType() != 2) {
                        return null;
                    }
                }
            }
            return world;
        }
    }

    public WorldServerSession getGame(int worldId) {
        synchronized (games) {
            return games.get(worldId);
        }
    }

    public List<WorldServerSession> getWorlds() {
        List<WorldServerSession> worlds = new ArrayList<WorldServerSession>(lobbies.size() + games.size());
        worlds.addAll(lobbies.values());
        worlds.addAll(games.values());
        return worlds;
    }

    public Collection<WorldServerSession> getLobbies() {
        return lobbies.values();
    }

    public Collection<WorldServerSession> getGames() {
        return games.values();
    }

    public Map<String, RemotePlayer> getPlayers() {
        return players;
    }

    public Pool<SQLSession> getSQLPool() {
        return sqlPool;
    }

    public DatabaseLoader getDBLoader() {
        return databaseLoader;
    }

    public RemotePlayer findPlayer(String name) {
        synchronized (players) {
            return players.get(name);
        }
    }

    public void registerWorld(WorldServerSession world) {
        if (world.isLobby()) {
            lobbies.put(world.getId(), world);
        } else {
            games.put(world.getId(), world);
        }
        logger.info("Registered world <" + world.toString() + ">");
    }

    public void removeWorld(WorldServerSession world) {
        for (RemotePlayer player : new ArrayList<RemotePlayer>(world.getPlayers())) {
            if (player.getClanOwner() != null) {
                ClanManager.unregisterPlayer(player.getProtocolName(), player.getClanOwner());
            }

            unregisterPlayer(world, player.getProtocolName());
        }
        if (world.isLobby()) {
            lobbies.remove(world.getId());
        } else {
            games.remove(world.getId());
        }
        logger.info("Removed world <" + world.toString() + ">");
    }

    public void registerPlayer(WorldServerSession world, RemotePlayer player) {
        RemotePlayer oldPlayer = findPlayer(player.getProtocolName());
        if (oldPlayer != null) {
            unregisterPlayer(world, player.getProtocolName());
        }
        players.put(player.getProtocolName(), player);
        world.registerPlayer(player);
        Frame frame = generateRegisterPlayerFrame(player);
        for (WorldServerSession w2 : getGames()) {
            if (world != w2) {
                IoSession session = w2.getSession();
                if (session != null) {
                    session.write(frame);
                }
            }
        }
        for (WorldServerSession w2 : getLobbies()) {
            if (world != w2) {
                IoSession session = w2.getSession();
                if (session != null) {
                    session.write(frame);
                }
            }
        }
        logger.info("Registered remote player [" + player + "]");
    }

    public void unregisterPlayer(WorldServerSession world, String name) {
        RemotePlayer player = players.get(name);
        if (player == null) {
            return;
        }

        unregisterPlayer(world, player);
    }

    public void unregisterPlayer(WorldServerSession world, RemotePlayer player) {
        RemotePlayer player2 = players.get(player.getProtocolName());
        if (player == player2) {
            players.remove(player.getProtocolName());
        }
        world.unregisterPlayer(player);
        Frame frame = generateUnregisterPlayerFrame(player);
        for (WorldServerSession w2 : getGames()) {
            if (world != w2) {
                IoSession session = w2.getSession();
                if (session != null) {
                    session.write(frame);
                }
            }
        }
        for (WorldServerSession w2 : getLobbies()) {
            if (world != w2) {
                IoSession session = w2.getSession();
                if (session != null) {
                    session.write(frame);
                }
            }
        }
        logger.info("Un-registered remote player [" + player + "]");
    }

    public Frame generateRegisterPlayerFrame(RemotePlayer player) {
        FrameBuilder fb = new FrameBuilder(2, Frame.FrameType.VAR_BYTE, 64);
        fb.writeString(player.getProtocolName()).writeByte(player.getWorld()).writeByte(player.inLobby() ? 1 : 0);
        return fb.toFrame();
    }

    private Frame generateUnregisterPlayerFrame(RemotePlayer player) {
        FrameBuilder fb = new FrameBuilder(3, Frame.FrameType.VAR_BYTE, 64);
        fb.writeString(player.getProtocolName());
        return fb.toFrame();
    }

    public void sendPM(String sender, int rights, String recipient, String message) {
        RemotePlayer player = findPlayer(recipient);
        if (player == null) {
            return;
        }
        WorldServerSession world;
        if (player.inGame()) {
            synchronized (games) {
                world = games.get(player.getWorld());
            }
        } else {
            synchronized (lobbies) {
                world = lobbies.get(player.getWorld());
            }
        }
        FrameBuilder fb = new FrameBuilder(5, FrameType.VAR_BYTE, 64);
        fb.writeString(recipient);
        fb.writeString(sender);
        fb.writeByte(rights);
        fb.writeString(message);
        IoSession session = world.getSession();
        if (session != null) {
            session.write(fb.toFrame());
        }
    }

    public void sendCCMessage(String sender, int rights, String owner, String message) {
        Clan clan = ClanManager.getClan(owner);
        if (clan == null) {
            return;
        }
        Set<Integer> worlds = new HashSet<Integer>();
        for (PlayerType player : clan.getPlayers().values()) {
            int worldHash = player.getWorld();
            if (player.inLobby()) {
                WorldServerSession world = getLobby(worldHash);
                if (world != null && world.getServerType() != 2) {
                    worldHash |= 0x8000;
                }
            }
            worlds.add(worldHash);
        }
        Frame frame = generateSendCCMessageFrame(sender, rights, owner, message);
        for (int worldHash : worlds) {
            int id = worldHash & 0x7fff;
            boolean lobby = (worldHash & 0x8000) != 0;
            WorldServerSession world = lobby ? getLobby(id) : getGame(id);
            IoSession session = world.getSession();
            if (session != null) {
                session.write(frame);
            }
        }
    }

    private Frame generateSendCCMessageFrame(String sender, int rights, String owner, String message) {
        FrameBuilder fb = new FrameBuilder(13, FrameType.VAR_BYTE, 64);
        fb.writeString(owner);
        fb.writeString(sender);
        fb.writeByte(rights);
        fb.writeString(message);
        return fb.toFrame();
    }

    public void registerClan(Clan clan) {
        Frame frame = generateRegisterClanFrame(clan);
        for (WorldServerSession world : getGames()) {
            IoSession session = world.getSession();
            if (session != null) {
                session.write(frame);
            }
        }
        for (WorldServerSession world : getLobbies()) {
            IoSession session = world.getSession();
            if (session != null) {
                session.write(frame);
            }
        }
    }

    public void unregisterClan(Clan clan) {
        Frame frame = generateUnregisterClanFrame(clan);
        for (WorldServerSession world : getGames()) {
            IoSession session = world.getSession();
            if (session != null) {
                session.write(frame);
            }
        }
        for (WorldServerSession world : getLobbies()) {
            IoSession session = world.getSession();
            if (session != null) {
                session.write(frame);
            }
        }
    }

    public Frame generateRegisterClanFrame(Clan clan) {
        FrameBuilder fb = new FrameBuilder(8, FrameType.VAR_BYTE, 512);
        fb.write(clan.toByteArray());
        return fb.toFrame();
    }

    private Frame generateUnregisterClanFrame(Clan clan) {
        FrameBuilder fb = new FrameBuilder(9, FrameType.VAR_BYTE, 512);
        fb.writeString(clan.getOwner());
        return fb.toFrame();
    }

    public void registerPlayerToClan(RemotePlayer player, Clan clan) {
        player.setClanOwner(clan.getOwner());

        WorldServerSession world = player.inLobby() ? getLobby(player.getWorld()) : getGame(player.getWorld());
        Frame frame = generateRegisterPlayerToClanFrame(player, clan);
        for (WorldServerSession w2 : getGames()) {
            if (world != w2) {
                IoSession session = w2.getSession();
                if (session != null) {
                    session.write(frame);
                }
            }
        }
        for (WorldServerSession w2 : getLobbies()) {
            if (world != w2) {
                IoSession session = w2.getSession();
                if (session != null) {
                    session.write(frame);
                }
            }
        }
    }

    public void unregisterPlayerFromClan(RemotePlayer player, Clan clan) {
        player.setClanOwner(null);

        WorldServerSession world = player.inLobby() ? getLobby(player.getWorld()) : getGame(player.getWorld());
        Frame frame = generateUnregisterPlayerFromClanFrame(player, clan);
        for (WorldServerSession w2 : getGames()) {
            if (world != w2) {
                IoSession session = w2.getSession();
                if (session != null) {
                    session.write(frame);
                }
            }
        }
        for (WorldServerSession w2 : getLobbies()) {
            if (world != w2) {
                IoSession session = w2.getSession();
                if (session != null) {
                    session.write(frame);
                }
            }
        }
    }

    public Frame generateRegisterPlayerToClanFrame(PlayerType player, Clan clan) {
        FrameBuilder fb = new FrameBuilder(10, FrameType.VAR_BYTE, 64);
        fb.writeString(player.getProtocolName());
        fb.writeString(clan.getOwner());
        return fb.toFrame();
    }

    private Frame generateUnregisterPlayerFromClanFrame(PlayerType player, Clan clan) {
        FrameBuilder fb = new FrameBuilder(11, FrameType.VAR_BYTE, 64);
        fb.writeString(player.getProtocolName());
        fb.writeString(clan.getOwner());
        return fb.toFrame();
    }

    public void broadcastClanUpdate(Clan clan) {
        Frame frame = generateBroadCastClanUpdateFrame(clan);
        for (WorldServerSession world : getGames()) {
            IoSession session = world.getSession();
            if (session != null) {
                session.write(frame);
            }
        }
        for (WorldServerSession world : getLobbies()) {
            IoSession session = world.getSession();
            if (session != null) {
                session.write(frame);
            }
        }
    }

    private Frame generateBroadCastClanUpdateFrame(Clan clan) {
        FrameBuilder fb = new FrameBuilder(12, FrameType.VAR_BYTE, 512);
        fb.write(clan.toByteArray());
        return fb.toFrame();
    }

    public void kickPlayerFromClan(String name) {
        PlayerType player = findPlayer(name);
        if (player == null) {
            return;
        }
        WorldServerSession world = player.inLobby() ? getLobby(player.getWorld()) : getGame(player.getWorld());
        FrameBuilder fb = new FrameBuilder(14, FrameType.VAR_BYTE, 64);
        fb.writeString(name);
        IoSession session = world.getSession();
        if (session != null) {
            session.write(fb.toFrame());
        }
    }

    public void handlePlayerMuting(String moderator, boolean mute, String userName) {
        userName = Text.formatNameForProtocol(userName);
        Pool<SQLSession> pool = Static.currentLink().getSQLPool();
        SQLSession sql = null;
        try {
            sql = pool.acquire();
            Statement st = sql.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM forummembers WHERE " + "members_seo_name='" + userName.replace("_","-") + "' LIMIT 1");
            if (!rs.next()) {
                rs.close();
                st.close();
                writeMutingResponse(moderator, userName, mute, false, false);
                return;
            }

            int userId = rs.getInt("member_id");
            rs.close();

            rs = st.executeQuery("SELECT * FROM playersave WHERE id='" + userId + "' LIMIT 1");
            boolean currentState = false;
            if (!rs.next()) {
                rs.close();
                st.close();
                writeMutingResponse(moderator, userName, mute, false, false);
                return;
            } else {
                currentState = rs.getInt("muted") == 1;
            }

            StringBuilder query = new StringBuilder();
            query.append("UPDATE playersave SET");
            query.append(" muted='").append(mute ? 1 : 0).append("'");
            query.append(" WHERE id='").append(userId).append("'");

            st = sql.createStatement();
            st.executeUpdate(query.toString());
            st.close();

            writeMutingResponse(moderator, userName, mute, true, mute != currentState);
        } catch (Exception e) {
            logger.error("Error setting mute settings for [name=" + userName + "]", e);
        } finally {
            if (sql != null) {
                pool.release(sql);
            }
        }
    }

    public void writeMutingResponse(String moderator, String adjustedUser, boolean mute, boolean succesful, boolean changedState) {
        PlayerType player = findPlayer(moderator);
        if (player == null)
            return;
        WorldServerSession world = player.inLobby() ? getLobby(player.getWorld()) : getGame(player.getWorld());
        if (!world.isLobby()) {
            FrameBuilder fb = new FrameBuilder(20, FrameType.VAR_BYTE, 64);
            fb.writeString(moderator).writeString(adjustedUser);
            fb.write((byte) (mute ? 1 : 0));
            fb.write((byte) (succesful ? 1 : 0));
            fb.write((byte) (changedState ? 1 : 0));
            IoSession session = world.getSession();
            if (session != null)
                session.write(fb.toFrame());
        }
    }
}
