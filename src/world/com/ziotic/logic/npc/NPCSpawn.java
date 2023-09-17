package com.ziotic.logic.npc;

import com.ziotic.logic.map.Directions.NormalDirection;
import com.ziotic.logic.map.Tile;

import java.io.ObjectStreamException;

/**
 * @author Lazaro
 */
public class NPCSpawn {
    public int id;
    public Tile location;
    public NormalDirection direction = null;
    public int range = 0;
    public boolean respawns = true;

    public NPCSpawn(int id, Tile location, NormalDirection direction, int range, boolean respawns) {
        this.id = id;
        this.location = location;
        this.direction = direction;
        this.range = range;
        this.respawns = respawns;
        if (this.direction == null) {
            this.direction = NormalDirection.NORTH;
        }
    }

    private Object readResolve() throws ObjectStreamException {
        location = location.translate(0, 0, 0);
        if (direction == null) {
            direction = NormalDirection.NORTH;
        }
        return this;
    }
}
