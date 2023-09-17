package com.ziotic.link;

import com.ziotic.Static;
import com.ziotic.logic.player.RemotePlayer;
import com.ziotic.utility.Destroyable;
import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lazaro
 */
public class WorldServerSession implements Destroyable {
    private int id;
    private int serverType;
    private String address;
    private List<IoSession> sessions = new ArrayList<IoSession>();
    private int currentSession = 0;
    private List<RemotePlayer> players = new ArrayList<RemotePlayer>();
    private List<RemotePlayer> lobbyPlayers = new ArrayList<RemotePlayer>();

    public WorldServerSession(int id, int serverType, String address) {
        this.id = id;
        this.serverType = serverType;
        this.address = address;

        switch (serverType) {
            case 0:
                players = new ArrayList<RemotePlayer>();
                break;
            case 1:
                lobbyPlayers = new ArrayList<RemotePlayer>();
                break;
            case 2:
                players = new ArrayList<RemotePlayer>();
                lobbyPlayers = new ArrayList<RemotePlayer>();
                break;
        }
    }

    public void registerSession(IoSession session) {
        sessions.add(session);
    }

    public void removeSession(IoSession session) {
        sessions.remove(session);
    }

    public IoSession getSession() {
        IoSession session;
        try {
            session = sessions.get(currentSession);
        } catch (Exception e) {
            return null;
        }
        currentSession++;
        if (currentSession >= sessions.size()) {
            currentSession = 0;
        }
        return session;
    }

    public boolean isOnline() {
        return sessions.size() > 0;
    }

    public int getId() {
        return id;
    }

    public int getServerType() {
        return serverType;
    }

    public boolean isLobby() {
        return serverType == 1;
    }

    public void destroy() {
        Static.currentLink().removeWorld(this);
    }

    @Override
    public String toString() {
        return "id=" + id + ", type=" + serverType;
    }

    public String getAddress() {
        return address;
    }

    public List<RemotePlayer> getPlayers() {
        return players;
    }

    public List<RemotePlayer> getLobbyPlayers() {
        return lobbyPlayers;
    }

    public void registerPlayer(RemotePlayer player) {
        if (player.inGame()) {
            synchronized (players) {
                players.add(player);
            }
        } else {
            synchronized (lobbyPlayers) {
                lobbyPlayers.add(player);
            }
        }
    }

    public void unregisterPlayer(RemotePlayer player) {
        if (player.inGame()) {
            synchronized (players) {
                players.remove(player);
            }
        } else {
            synchronized (lobbyPlayers) {
                lobbyPlayers.remove(player);
            }
        }
    }
}
