package com.runescape.logic.map.pf;

/**
 * @author Lazaro
 */
public class TileNode {
    /**
     * The cost.
     */
    public int cost = 1000;

    /**
     * The x coordinate.
     */
    public final int x;

    /**
     * The y coordinate.
     */
    public final int y;

    /**
     * The via code for how we got to this node.
     */
    public int via = 0;

    /**
     * The clipping mask of this tile.
     */
    public int clipping = 0;

    /**
     * Whether this node is open.
     */
    public boolean open = false;

    /**
     * Whether this node is closed.
     */
    public boolean closed = false;

    /**
     * Creates a node.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public TileNode(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
