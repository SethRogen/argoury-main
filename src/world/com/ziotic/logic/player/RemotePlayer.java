package com.ziotic.logic.player;

import com.ziotic.utility.Text;

public class RemotePlayer implements PlayerType {
    private String name;
    private String protocolName;
    private int world;
    private boolean lobby;

    private String clanOwner = null;

    public RemotePlayer(String name, int world, boolean lobby) {
        this.name = Text.formatNameForDisplay(name);
        this.protocolName = name;
        this.world = world;
        this.lobby = lobby;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getProtocolName() {
        return protocolName;
    }

    @Override
    public int getWorld() {
        return world;
    }

    @Override
    public boolean inGame() {
        return !lobby;
    }

    @Override
    public boolean inLobby() {
        return lobby;
    }

    public String getClanOwner() {
        return clanOwner;
    }

    public void setClanOwner(String clanOwner) {
        this.clanOwner = clanOwner;
    }

    @Override
    public String toString() {
        return "name=" + protocolName + ", world=" + world + ", lobby=" + lobby;
    }
}
