package com.ziotic.logic.map.event;

import com.ziotic.logic.Entity;
import com.ziotic.logic.map.Tile;

public interface EntityTileEventListener extends TileEventListener {
    void onEntityEvent(Entity entity, Tile tile);
}
