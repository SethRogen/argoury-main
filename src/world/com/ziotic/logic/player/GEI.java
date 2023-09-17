package com.ziotic.logic.player;

import com.ziotic.Static;
import com.ziotic.logic.npc.NPC;

import java.util.LinkedList;
import java.util.List;

/**
 * GEI stands for Global Entity Information.
 * <p/>
 * Here we store information about the player's viewport of players and NPCs.
 *
 * @author Lazaro
 */
public final class GEI {
    public static final int IN_VIEWPORT_MASK = 0x1, CROSSED_VIEWPORT_MASK = 0x2, SKIPPED_LAST_CYCLE = 0x4, SKIPPED_THIS_CYCLE = 0x8;
    public static final int MAX_VIEWPORT_SIZE = 16;

    private Player player;

    public byte[] playerData = new byte[2048];
    public short[] localPlayers = new short[2048];
    public int localPlayerCount = 0;
    public short[] nonLocalPlayers = new short[2048];
    public int nonLocalPlayerCount = 0;
    public short[] playerUpdatesRequired = new short[2048];
    public int playerUpdatesRequiredCount = 0;
    public int playerViewportSize = -1;
    public int[] playerLocations = new int[2048];

    public List<NPC> localNPCs = new LinkedList<NPC>();

    public GEI(Player player) {
        this.player = player;
    }

    public void init() {
        playerData[player.getIndex()] |= IN_VIEWPORT_MASK;
        localPlayerCount = 1;
        localPlayers[0] = (short) player.getIndex();
        int[] initialPlayerLocations = Static.world.getPlayerUpdater().getPlayerLocations();
        for (short i = 1; i < 2048; i++) {
            playerLocations[i] = initialPlayerLocations[i];
            if (i != player.getIndex()) {
                nonLocalPlayers[nonLocalPlayerCount++] = i;
            }
        }
    }

    public void reset() {
        localPlayerCount = 0;
        nonLocalPlayerCount = 0;
        playerUpdatesRequiredCount = 0;
        if (playerViewportSize < MAX_VIEWPORT_SIZE) {
            playerViewportSize++;
        }
        for (short i = 1; i < 2048; i++) {
            playerData[i] &= ~CROSSED_VIEWPORT_MASK;
            if ((playerData[i] & SKIPPED_THIS_CYCLE) != 0) {
                playerData[i] |= SKIPPED_LAST_CYCLE;
            } else {
                playerData[i] &= ~SKIPPED_LAST_CYCLE;
            }
            playerData[i] &= ~SKIPPED_THIS_CYCLE;

            if ((playerData[i] & IN_VIEWPORT_MASK) != 0) {
                localPlayers[localPlayerCount++] = i;
            } else {
                nonLocalPlayers[nonLocalPlayerCount++] = i;
            }
        }
    }

    public int updateLocation(Player player) {
        return playerLocations[player.getIndex()] = player.getZ() << 16 | player.getLocation().getRegionX() << 8 | player.getLocation().getRegionY() & 0xff;
    }
}
