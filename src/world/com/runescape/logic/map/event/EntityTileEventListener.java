package com.runescape.logic.map.event;

import com.runescape.logic.Entity;
import com.runescape.logic.map.Tile;

public interface EntityTileEventListener extends TileEventListener {
    void onEntityEvent(Entity entity, Tile tile);
}
