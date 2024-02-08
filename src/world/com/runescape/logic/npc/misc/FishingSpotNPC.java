package com.runescape.logic.npc.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.runescape.Static;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.Entity;
import com.runescape.logic.World;
import com.runescape.logic.map.Tile;
import com.runescape.logic.npc.NPC;
import com.runescape.logic.npc.NPCSpawn;

/**
 * 
 * @author 'Mystic Flow
 */
public class FishingSpotNPC extends NPC {

	public static void load() throws IOException {
		List<FishingSpotNPC> spots = Static.xml.readObject("data/world/npc/fishing_spots.xml");
		for (FishingSpotNPC spot : spots) { // XXX I don't think I designed this very good .
			List<Tile> tiles = new ArrayList<Tile>();
			for (Tile tile : spot.tiles) {
				tiles.add(Tile.locate(tile.getX(), tile.getY(), tile.getZ()));
			}
			Static.world.register(new FishingSpotNPC(spot.getSpawn(), tiles.toArray(new Tile[0])));

			tiles.clear();
			tiles = null;
			spot.tiles = null;
		}
	}

	public FishingSpotNPC(NPCSpawn spawn, Tile[] tiles) {
		super(spawn);
		this.tiles = tiles;
	}

	private int nextMove;
	private Tile[] tiles;

	@Override
	public void subPreProcess() {
		if (tiles == null || tiles.length == 0) // SHOULD NOT HAPPEN 
			return;
		nextMove++;
		// 1 minute + a random 2 minutes?
		if (nextMove >= 100 + World.getRandom(200)) {
			nextMove = 0;
			Tile nextTile = tiles[World.getRandom(tiles.length)];
			if (tiles.length > 1) // prevent deadlock's
				while (nextTile == getLocation())
					nextTile = tiles[World.getRandom(tiles.length)];
			setTeleportDestination(nextTile);
			Iterator<Entity> it = getPathProcessor().getHookedEntities().iterator();
			while (it.hasNext()) {
				Entity fishingPlayer = it.next();
				if (fishingPlayer != null) {
					Tick tick = fishingPlayer.retrieveTick("fishing_tick");
					if (tick != null)
						tick.stop();
					it.remove();
				}
			}
		}
	}

}
