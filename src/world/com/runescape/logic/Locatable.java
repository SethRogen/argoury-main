package com.runescape.logic;

import com.runescape.logic.map.Tile;

/**
 * @author Lazaro
 */
public class Locatable extends Node {
    private Tile location = null;

    public final Tile getLocation() {
        return location;
    }

    public final int getX() {
        return location.getX();
    }

    public final int getY() {
        return location.getY();
    }

    public final int getZ() {
        return location.getZ();
    }

    public final void setLocation(Tile location) {
        if (this.location != null)
            this.location.remove(this);
        if (location != null) {
            this.location = location;
            if (this instanceof Entity) {
                ((Entity) this).setCoverage();
            }
            location.add(this);
        }
    }
}
