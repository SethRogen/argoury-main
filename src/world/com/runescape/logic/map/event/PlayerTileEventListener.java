package com.runescape.logic.map.event;

import com.runescape.logic.map.Tile;
import com.runescape.logic.player.Player;

public interface PlayerTileEventListener extends TileEventListener {
    void onPlayerEvent(Player player, Tile tile);
}
