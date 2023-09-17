package com.ziotic.logic.map.event;

import com.ziotic.logic.map.Tile;
import com.ziotic.logic.object.GameObject;

public interface GameObjectTileEventListener extends TileEventListener {
    void onGameObjectEvent(GameObject object, Tile tile);
}
