package com.ziotic.logic.map.event;

import com.ziotic.logic.map.Tile;
import com.ziotic.logic.npc.NPC;

public interface NPCTileEventListener extends TileEventListener {
    void onNPCEvent(NPC npc, Tile tile);
}
