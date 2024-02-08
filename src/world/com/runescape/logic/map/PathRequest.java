package com.runescape.logic.map;

import com.runescape.logic.Locatable;

/**
 * @author Lazaro
 */
public class PathRequest {
    public PathFinder pathFinder;
    public int x;
    public int y;
    public int moveSpeed;
    public boolean automated;
    public Locatable target;
    public Runnable future;

    public PathRequest(PathFinder pathFinder, int x, int y, Locatable target, int moveSpeed, boolean automated, Runnable future) {
        this.pathFinder = pathFinder;
        this.x = x;
        this.y = y;
        this.moveSpeed = moveSpeed;
        this.automated = automated;
        this.target = target;
        this.future = future;
    }
}
