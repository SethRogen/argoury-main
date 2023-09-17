package com.ziotic.logic.map.event;

import com.ziotic.logic.item.GroundItem;
import com.ziotic.logic.map.Tile;

public interface GroundItemTileEventListener extends TileEventListener {
    void onGroundItemEvent(GroundItem item, Tile tile);
}
