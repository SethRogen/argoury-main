package com.runescape.logic.map.event;

import com.runescape.logic.item.GroundItem;
import com.runescape.logic.map.Tile;

public interface GroundItemTileEventListener extends TileEventListener {
    void onGroundItemEvent(GroundItem item, Tile tile);
}
