package com.ziotic.logic.map.pf;

import com.ziotic.utility.Poolable;

/**
 * @author Lazaro
 */
public class TileNodeContainer implements Poolable {
    public TileNode[][] nodes = null;

    public TileNodeContainer() {
        nodes = new TileNode[104][104];
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                nodes[x][y] = new TileNode(x, y);
            }
        }
    }

    @Override
    public boolean expired() {
        return false;
    }

    @Override
    public void recycle() {
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                TileNode n = nodes[x][y];
                n.cost = 1000;
                n.via = 0;
                n.clipping = 0;
                n.open = false;
                n.closed = false;
            }
        }
    }
}
