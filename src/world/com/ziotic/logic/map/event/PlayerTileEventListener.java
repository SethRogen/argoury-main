package com.ziotic.logic.map.event;

import com.ziotic.logic.map.Tile;
import com.ziotic.logic.player.Player;

public interface PlayerTileEventListener extends TileEventListener {
    void onPlayerEvent(Player player, Tile tile);
}
