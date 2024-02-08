/**
 *
 */
package com.runescape.logic.map;

import java.util.LinkedList;

/**
 * @author Lazaro
 */
public interface PathFinder {
    public boolean isSuccessful();

    public boolean movedNear();

    public LinkedList<Tile> findPath(Tile src, int srcX, int srcY, int dstX, int dstY, boolean moveNear, int type, int direction, int sizeX, int sizeY, int walkToData, boolean noClip, int size);
}

