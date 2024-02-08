package com.runescape.logic.map.event;

import com.runescape.logic.map.Tile;
import com.runescape.logic.npc.NPC;

public interface NPCTileEventListener extends TileEventListener {
    void onNPCEvent(NPC npc, Tile tile);
}
