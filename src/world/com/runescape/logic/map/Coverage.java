package com.runescape.logic.map;

import java.awt.Point;

import com.runescape.content.combat.Combat.ActionType;
import com.runescape.content.combat.misc.CombatUtilities;
import com.runescape.logic.Entity;
import com.runescape.logic.map.Directions.NormalDirection;

public class Coverage {
		
	private Point lowerBound;
	private Point upperBound;
    private int z;
	private int size;
	
	public Coverage(Tile loc, int size) {
		this.lowerBound = new Point(loc.getX(), loc.getY());
		this.upperBound = new Point(lowerBound.x + size - 1, lowerBound.y + size - 1);
        this.z = loc.getZ();
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getLowerBoundX() {
		return lowerBound.x;
	}
	
	public int getLowerBoundY() {
		return lowerBound.y;
	}
	
	public int getUpperBoundX() {
		return upperBound.x;
	}
	
	public int getUpperBoundY() {
		return upperBound.y;
	}
	
	public Tile center() {
		return Tile.locate(lowerBound.x + (int) Math.floor(size / 2), lowerBound.y + (int) Math.floor(size / 2), z);
	}

	public void update(NormalDirection direction, int size) {
		this.size = size;
		int dx = Directions.DIRECTION_DELTA_X[direction.intValue()];
		int dy = Directions.DIRECTION_DELTA_Y[direction.intValue()];
		lowerBound.setLocation(lowerBound.x + dx, lowerBound.y + dy);
		upperBound.setLocation(upperBound.x + dx, upperBound.y + dy);
	}
	
	public void update(NormalDirection direction) {
		int dx = Directions.DIRECTION_DELTA_X[direction.intValue()];
		int dy = Directions.DIRECTION_DELTA_Y[direction.intValue()];
		lowerBound.setLocation(lowerBound.x + dx, lowerBound.y + dy);
		upperBound.setLocation(upperBound.x + dx, upperBound.y + dy);
	}
	
	public void update(Tile loc, int size) {
		this.lowerBound = new Point(loc.getX(), loc.getY());
		this.upperBound = new Point(lowerBound.x + size - 1, lowerBound.y + size - 1);
		this.size = size;
	}
	
	public boolean intersect(Coverage c) {
		return !right(c) && !left(c) && !above(c) && !under(c);
	}
	
	public boolean within(Tile t) {
		return t.getX() >= lowerBound.x && t.getX() <= upperBound.x
					&& t.getY() >= lowerBound.y && t.getY() <= upperBound.y;
	}
	
	public boolean right(Coverage c) {
		return lowerBound.x > c.upperBound.x;
	}
	
	public boolean left(Coverage c) {
		return upperBound.x < c.lowerBound.x;
	}
	
	public boolean above(Coverage c) {
		return lowerBound.y > c.upperBound.y;
	}
	
	public boolean under(Coverage c) {
		return upperBound.y < c.lowerBound.y;
	}
	
	public boolean touch(Coverage c) {
		if (!intersect(c)) {
			if (right(c)) {
				if (above(c)) {
					return c.lowerBound.x + c.size == lowerBound.x && c.lowerBound.y + c.size == lowerBound.y;
				} else if (under(c)) {
					return c.lowerBound.x + c.size == lowerBound.x && c.lowerBound.y - 1 == lowerBound.y;
				} else {
					return c.lowerBound.x + c.size == lowerBound.x;
				}
			} else if (left(c)) {
				if (above(c)) {
					return lowerBound.x + size == c.lowerBound.x && lowerBound.y == c.lowerBound.y + c.size;
				} else if (under(c)) {
					return lowerBound.x + size == c.lowerBound.x && lowerBound.y == c.lowerBound.y - 1;
				} else {
					return lowerBound.x + size == c.lowerBound.x;
				}
			} else {
				if (above(c)) {
					return lowerBound.y - 1 == c.upperBound.y;
				} else if (under(c)) {
					return c.lowerBound.y - 1 == upperBound.y;
				}
			}
		}
		return false;
	}
	
	public boolean correctCombatPosition(Entity entity, Entity partner, Coverage c, ActionType type, int distance) {
		if (intersect(c)) {
			return false;
		}
		switch (type) {
		case MELEE:
			if (size == 1 && c.size == 1) {
				int absDX = Math.abs(lowerBound.x - c.lowerBound.x);
				int absDY = Math.abs(lowerBound.y - c.lowerBound.y);
				return (absDX == 0 && absDY == 1) || (absDX == 1 && absDY == 0);
			} else {
				return touch(c);
			}
		case MAGIC:
		case RANGED:
			return center().distance(c.center()) <= distance && CombatUtilities.clippedProjectile(entity, partner);
		}
		return false;		
	}
	
	public boolean correctFinalFollowPosition(Coverage c) {
		return touch(c);
	}
    
	/**
	 * Returns a 2D array of the covering tiles.
	 * NOTE: this array begins with the lower y values (counting from above, comparable with the map) however
	 * this is different to the coverage system considering the coverage system start counting from under.
	 * @return
	 */
    public Tile[][] tiles() {
    	Tile[][] tiles = new Tile[size][size];
    	for (int x = (int) lowerBound.getX(); x <= upperBound.getX(); x++) {
    		for (int y = (int) lowerBound.getY(); y <= upperBound.getY(); y++) {
    			tiles[x - (int) lowerBound.getX()][y - (int) lowerBound.getY()] = Tile.locate(x, y, z);
    		}
    	}
    	return tiles;
    }
	
}
