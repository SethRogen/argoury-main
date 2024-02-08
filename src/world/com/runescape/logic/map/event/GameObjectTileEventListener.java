package com.runescape.logic.map.event;

import com.runescape.logic.map.Tile;
import com.runescape.logic.object.GameObject;

public interface GameObjectTileEventListener extends TileEventListener {
    void onGameObjectEvent(GameObject object, Tile tile);
}
