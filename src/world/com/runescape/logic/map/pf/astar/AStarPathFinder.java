package com.runescape.logic.map.pf.astar;

import com.runescape.logic.map.PathFinder;
import com.runescape.logic.map.Region;
import com.runescape.logic.map.Tile;
import com.runescape.logic.map.pf.TileNode;
import com.runescape.logic.map.pf.TileNodeContainer;
import com.runescape.utility.Pool;

import java.util.LinkedList;

/**
 * @author Lazaro
 */
public final class AStarPathFinder implements PathFinder {
    private static final int MAX_LOOPS = 10817;

    private static Pool<TileNodeContainer> nodeContainerPool = new Pool<TileNodeContainer>(TileNodeContainer.class, null, 4);

    private TileNode current;
    private TileNode[][] nodes;
    private LinkedList<TileNode> open = new LinkedList<TileNode>();

    private boolean success = false;
    private boolean movedNear = false;

    private int z = -1;

    private Tile base = null;

    private int baseX = -1;
    private int baseY = -1;

    private int baseRegionX = -1;
    private int baseRegionY = -1;

    private Region[][] regions = null;

    public boolean isSuccessful() {
        return success;
    }

    @Override
    public boolean movedNear() {
        return movedNear;
    }

    public LinkedList<Tile> findPath(Tile absSrc, int srcX, int srcY, int dstX, int dstY, boolean moveNear, int type, int direction, int sizeX, int sizeY, int walkToData, boolean noClip, int size) {
        if (srcX < 0 || srcY < 0 || srcX >= 104 || srcY >= 104 || dstX < 0 || dstY < 0 || dstX >= 104 || dstY >= 104) {
            return null;
        }

        TileNodeContainer nodeContainer = null;
        try {
            boolean varSized = size > 1;

            z = absSrc.getZ();

            base = Tile.locate((absSrc.getPartX() - 6) << 3, (absSrc.getPartY() - 6) << 3, z);

            baseX = base.getX();
            baseY = base.getY();

            baseRegionX = base.getRegionX();
            baseRegionY = base.getRegionY();

            regions = new Region[3][3];
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    Region r = regions[x][y] = Region.forRegionCoordinates(baseRegionX + x, baseRegionY + y);
                    r.load();
                }
            }

            nodeContainer = nodeContainerPool.acquire();

            nodes = nodeContainer.nodes;
            for (int x = 0; x < 104; x++) {
                for (int y = 0; y < 104; y++) {
                    nodes[x][y].clipping = regions[((baseX + x) >> 6) - baseRegionX][((baseY + y) >> 6) - baseRegionY].getClipping((baseX + x) & 0x3f, (baseY + y) & 0x3f, z);
                }
            }

            TileNode src = nodes[srcX][srcY];
            TileNode dest = nodes[dstX][dstY];
            src.cost = 0;
            src.via = 99;
            open.add(src);

            int curX = dstX;
            int curY = dstY;

            boolean foundPath = false;

            int loop = 0;
            while (open.size() > 0 && loop++ <= MAX_LOOPS) {
                current = nextNode(current);

                int x = current.x, y = current.y;

                if (type == -2 && current == dest) {
                    curX = x;
                    curY = y;
                    foundPath = true;
                    break;
                }
                if (type != -1) {
                    if (type == 0 || type == 1 || type == 2 || type == 3 || type == 9) {
                        if (reachedObject(dstX, dstY, x, y, type, direction)) {
                            curX = x;
                            curY = y;
                            foundPath = true;
                            break;
                        }
                    } else {
                        if (reachedObject2(dstX, dstY, x, y, type, direction)) {
                            curX = x;
                            curY = y;
                            foundPath = true;
                            break;
                        }
                    }
                } else {
                    if (reachedObject3(dstX, dstY, x, y, sizeX, sizeY, walkToData)) {
                        curX = x;
                        curY = y;
                        foundPath = true;
                        break;
                    }
                }

                current.open = false;
                current.closed = true;
                open.remove(current);

                if (varSized) {
                    westCheck:
                    do {
                        if (x > size - 1) {
                            TileNode n = nodes[x - 1][y];
                            if (noClip) {
                                examineNode(n, 2);
                            } else if (n.via == 0 && (n.clipping & 0x43a40000) == 0 && (nodes[x - 1][y + size - 1].clipping & 0x4e240000) == 0) {
                                for (int offset = 1; offset < size - 1; offset++) {
                                    if ((nodes[x - 1][y + offset].clipping & 0x4fa40000) != 0)
                                        break westCheck;
                                }
                                examineNode(n, 2);
                            }
                        }
                    } while (false);
                    eastCheck:
                    do {
                        if (x < 104 - size) {
                            TileNode n = nodes[x + 1][y];
                            if (noClip) {
                                examineNode(n, 8);
                            } else if (n.via == 0 && (nodes[x + size][y].clipping & 0x60e40000) == 0 && (nodes[x + size][y + size - 1].clipping & 0x78240000) == 0) {
                                for (int offset = 1; offset < size - 1; offset++) {
                                    if ((nodes[x + size][y + offset].clipping & 0x78e40000) != 0)
                                        break eastCheck;
                                }
                                examineNode(n, 8);
                            }
                        }
                    } while (false);
                    southCheck:
                    do {
                        if (y > size - 1) {
                            TileNode n = nodes[x][y - 1];
                            if (noClip) {
                                examineNode(n, 1);
                            } else if (n.via == 0 && (n.clipping & 0x43a40000) == 0 && (nodes[x + size - 1][y - 1].clipping & 0x60e40000) == 0) {
                                for (int offset = 1; offset < size - 1; offset++) {
                                    if ((nodes[x + offset][y - 1].clipping & 0x63e40000) != 0)
                                        break southCheck;
                                }
                                examineNode(n, 1);
                            }
                        }
                    } while (false);
                    northCheck:
                    do {
                        if (y < 104 - size) {
                            TileNode n = nodes[x][y + 1];
                            if (noClip) {
                                examineNode(n, 4);
                            } else if (n.via == 0 && (nodes[x][y + size].clipping & 0x4e240000) == 0 && (nodes[x + size - 1][y + size].clipping & 0x78240000) == 0) {
                                for (int offset = 1; offset < size - 1; offset++) {
                                    if ((nodes[x + offset][y + size].clipping & 0x7e240000) != 0)
                                        break northCheck;
                                }
                                examineNode(n, 4);
                            }
                        }
                    } while (false);
                    southWestCheck:
                    do {
                        if (x > size - 1 && y > size - 1) {
                            TileNode n = nodes[x - 1][y - 1];
                            if (noClip) {
                                examineNode(n, 3);
                            } else if (n.via == 0 && (n.clipping & 0x43a40000) == 0) {
                                for (int offset = 1; offset < size - 1; offset++) {
                                    if ((nodes[x - 1][y + offset - 1].clipping & 0x4fa40000) != 0 || (nodes[x + offset - 1][y - 1].clipping & 0x63e40000) != 0)
                                        break southWestCheck;
                                }
                                examineNode(n, 3);
                            }
                        }
                    } while (false);
                    southEastCheck:
                    do {
                        if (x < 104 - size && y > size - 1) {
                            TileNode n = nodes[x + 1][y - 1];
                            if (noClip) {
                                examineNode(n, 9);
                            } else if (n.via == 0 && (nodes[x + size][y - 1].clipping & 0x60e40000) == 0) {
                                for (int offset = 1; offset < size - 1; offset++) {
                                    if ((nodes[x + size][y + offset - 1].clipping & 0x78e40000) != 0 || (nodes[x + offset][y - 1].clipping & 0x63e40000) != 0)
                                        break southEastCheck;
                                }
                                examineNode(n, 9);
                            }
                        }
                    } while (false);
                    northWestCheck:
                    do {
                        if (x > size - 1 && y < 104 - size) {
                            TileNode n = nodes[x - 1][y + 1];
                            if (noClip) {
                                examineNode(n, 6);
                            } else if (n.via == 0 && (nodes[x - 1][y + size].clipping & 0x4e240000) == 0) {
                                for (int offset = 1; offset < size - 1; offset++) {
                                    if ((nodes[x - 1][y + offset].clipping & 0x4fa40000) != 0 || (nodes[x + offset - 1][y + size].clipping & 0x7e240000) != 0)
                                        break northWestCheck;
                                }
                                examineNode(n, 6);
                            }
                        }
                    } while (false);
                    northEastCheck:
                    do {
                        if (x < 104 - size && y < 104 - size) {
                            TileNode n = nodes[x + 1][y + 1];
                            if (noClip) {
                                examineNode(n, 12);
                            } else if (n.via == 0 && (nodes[x + size][y + size].clipping & 0x78240000) == 0) {
                                for (int offset = 1; offset < size - 1; offset++) {
                                    if ((nodes[x + size][y + offset].clipping & 0x7e240000) != 0 || (nodes[x + offset][y + size].clipping & 0x78e40000) != 0)
                                        break northEastCheck;
                                }
                                examineNode(n, 12);
                            }
                        }
                    } while (false);
                } else {
                    // west
                    if (x > 0) {
                        TileNode n = nodes[x - 1][y];
                        if (noClip || (n.clipping & 0x42240000) == 0) {
                            examineNode(n, 2);
                        }
                    }
                    // east
                    if (x < 104 - 1) {
                        TileNode n = nodes[x + 1][y];
                        if (noClip || (n.clipping & 0x60240000) == 0) {
                            examineNode(n, 8);
                        }
                    }
                    // south
                    if (y > 0) {
                        TileNode n = nodes[x][y - 1];
                        if (noClip || (n.clipping & 0x40a40000) == 0) {
                            examineNode(n, 1);
                        }
                    }
                    // north
                    if (y < 104 - 1) {
                        TileNode n = nodes[x][y + 1];
                        if (noClip || (n.clipping & 0x48240000) == 0) {
                            examineNode(n, 4);
                        }
                    }
                    // south-west
                    if (x > 0 && y > 0) {
                        TileNode n = nodes[x - 1][y - 1];
                        if (noClip || ((n.clipping & 0x43a40000) == 0 && (nodes[x - 1][y].clipping & 0x42240000) == 0 && (nodes[x][y - 1].clipping & 0x40a40000) == 0)) {
                            examineNode(n, 3);
                        }
                    }
                    // south-east
                    if (x < 104 - 1 && y > 0) {
                        TileNode n = nodes[x + 1][y - 1];
                        if (noClip || ((n.clipping & 0x60e40000) == 0 && (nodes[x + 1][y].clipping & 0x60240000) == 0 && (nodes[x][y - 1].clipping & 0x40a40000) == 0)) {
                            examineNode(n, 9);
                        }
                    }
                    // north-west
                    if (x > 0 && y < 104 - 1) {
                        TileNode n = nodes[x - 1][y + 1];
                        if (noClip || ((n.clipping & 0x4e240000) == 0 && (nodes[x - 1][y].clipping & 0x42240000) == 0 && (nodes[x][y + 1].clipping & 0x48240000) == 0)) {
                            examineNode(n, 6);
                        }
                    }
                    // north-east
                    if (x < 104 - 1 && y < 104 - 1) {
                        TileNode n = nodes[x + 1][y + 1];
                        if (noClip || ((n.clipping & 0x78240000) == 0 && (nodes[x + 1][y].clipping & 0x60240000) == 0 && (nodes[x][y + 1].clipping & 0x48240000) == 0)) {
                            examineNode(n, 12);
                        }
                    }
                }
            }

            if (!foundPath) {
                if (moveNear) {
                    int curDistance = 1000;
                    int curCost = 100;
                    int range = 10;
                    for (int x = dstX - range; x <= dstX + range; x++) {
                        for (int y = dstY - range; y <= dstY + range; y++) {
                            if (x >= 0 && y >= 0 && x < 104 && y < 104) {
                                TileNode n = nodes[x][y];
                                int cost = n.cost;
                                if (cost < 100) {
                                    int diffX = 0;
                                    if (x < dstX)
                                        diffX = dstX - x;
                                    else if (x > dstX + sizeX - 1)
                                        diffX = x - (dstX + sizeX - 1);
                                    int diffY = 0;
                                    if (y < dstY)
                                        diffY = dstY - y;
                                    else if (y > dstY + sizeY - 1)
                                        diffY = y - (dstY + sizeY - 1);
                                    int distance = diffX * diffX + diffY * diffY;
                                    if (distance < curDistance || (distance == curDistance && (cost < curCost))) {
                                        curDistance = distance;
                                        curCost = cost;
                                        curX = x;
                                        curY = y;
                                        foundPath = true;
                                    }
                                }
                            }
                        }
                    }
                    if (!foundPath) {
                        return null;
                    } else {
                        movedNear = true;
                    }
                } else {
                    return null;
                }
            }

            if (srcX == curX && srcY == curY) {
                success = true;
                return null;
            }

            LinkedList<Tile> p = new LinkedList<Tile>();
            p.addFirst(Tile.locate(base.getX() + curX, base.getY() + curY, z));
            TileNode lastWaypoint;
            for (TileNode currentNode = lastWaypoint = nodes[curX][curY]; curX != srcX || curY != srcY; currentNode = nodes[curX][curY]) {
                if (currentNode.via != lastWaypoint.via) {
                    lastWaypoint = currentNode;
                    int absX = base.getX() + curX;
                    int absY = base.getY() + curY;
                    p.addFirst(Tile.locate(absX, absY, z));
                }
                if ((currentNode.via & 2) != 0) {
                    curX++;
                } else if ((currentNode.via & 8) != 0) {
                    curX--;
                }
                if ((currentNode.via & 1) != 0) {
                    curY++;
                } else if ((currentNode.via & 4) != 0) {
                    curY--;
                }
            }
            success = true;

            return p;
        } finally {
            if (nodeContainer != null) {
                nodeContainerPool.release(nodeContainer);
            }
            current = null;
            nodes = null;
            open.clear();
        }
    }

    /**
     * Estimates a distance between the two points.
     *
     * @param src The source node.
     * @param dst The distance node.
     * @return The distance.
     */
    private int estimateDistance(TileNode src, TileNode dst) {
        int deltaX = src.x - dst.x;
        int deltaY = src.y - dst.y;
        return Math.abs(deltaX) + Math.abs(deltaY);
    }

    private void examineNode(TileNode n, int via) {
        int heuristic = estimateDistance(current, n);
        int nextStepCost = current.cost + heuristic;
        if (nextStepCost < n.cost) {
            n.open = false;
            n.closed = false;
            open.remove(n);
        }
        if (!n.open && !n.closed) {
            n.cost = nextStepCost;
            n.via = via;
            n.open = true;
            open.add(n);
        }
    }

    private TileNode nextNode(TileNode previous) {
        return open.getFirst();
    }

    public boolean reachedObject(int dstX, int dstY, int curX, int curY, int type, int direction) {
        if (curX == dstX && curY == dstY)
            return true;
        TileNode n = nodes[curX][curY];
        if (type == 0)
            if (direction == 0) {
                if (curX == dstX - 1 && curY == dstY)
                    return true;
                if (curX == dstX && curY == dstY + 1 && (n.clipping & 0x2c0120) == 0)
                    return true;
                if (curX == dstX && curY == dstY - 1 && (n.clipping & 0x2c0102) == 0)
                    return true;
            } else if (direction == 1) {
                if (curX == dstX && curY == dstY + 1)
                    return true;
                if (curX == dstX - 1 && curY == dstY && (n.clipping & 0x2c0108) == 0)
                    return true;
                if (curX == dstX + 1 && curY == dstY && (n.clipping & 0x2c0180) == 0)
                    return true;
            } else if (direction == 2) {
                if (curX == dstX + 1 && curY == dstY)
                    return true;
                if (curX == dstX && curY == dstY + 1 && (n.clipping & 0x2c0120) == 0)
                    return true;
                if (curX == dstX && curY == dstY - 1 && (n.clipping & 0x2c0102) == 0)
                    return true;
            } else if (direction == 3) {
                if (curX == dstX && curY == dstY - 1)
                    return true;
                if (curX == dstX - 1 && curY == dstY && (n.clipping & 0x2c0108) == 0)
                    return true;
                if (curX == dstX + 1 && curY == dstY && (n.clipping & 0x2c0180) == 0)
                    return true;
            }
        if (type == 2)
            if (direction == 0) {
                if (curX == dstX - 1 && curY == dstY)
                    return true;
                if (curX == dstX && curY == dstY + 1)
                    return true;
                if (curX == dstX + 1 && curY == dstY && (n.clipping & 0x2c0180) == 0)
                    return true;
                if (curX == dstX && curY == dstY - 1 && (n.clipping & 0x2c0102) == 0)
                    return true;
            } else if (direction == 1) {
                if (curX == dstX - 1 && curY == dstY && (n.clipping & 0x2c0108) == 0)
                    return true;
                if (curX == dstX && curY == dstY + 1)
                    return true;
                if (curX == dstX + 1 && curY == dstY)
                    return true;
                if (curX == dstX && curY == dstY - 1 && (n.clipping & 0x2c0102) == 0)
                    return true;
            } else if (direction == 2) {
                if (curX == dstX - 1 && curY == dstY && (n.clipping & 0x2c0108) == 0)
                    return true;
                if (curX == dstX && curY == dstY + 1 && (n.clipping & 0x2c0120) == 0)
                    return true;
                if (curX == dstX + 1 && curY == dstY)
                    return true;
                if (curX == dstX && curY == dstY - 1)
                    return true;
            } else if (direction == 3) {
                if (curX == dstX - 1 && curY == dstY)
                    return true;
                if (curX == dstX && curY == dstY + 1 && (n.clipping & 0x2c0120) == 0)
                    return true;
                if (curX == dstX + 1 && curY == dstY && (n.clipping & 0x2c0180) == 0)
                    return true;
                if (curX == dstX && curY == dstY - 1)
                    return true;
            }
        if (type == 9) {
            if (curX == dstX && curY == dstY + 1 && (n.clipping & 0x20) == 0)
                return true;
            if (curX == dstX && curY == dstY - 1 && (n.clipping & 2) == 0)
                return true;
            if (curX == dstX - 1 && curY == dstY && (n.clipping & 8) == 0)
                return true;
            if (curX == dstX + 1 && curY == dstY && (n.clipping & 0x80) == 0)
                return true;
        }
        return false;
    }

    public boolean reachedObject2(int dstX, int dstY, int curX, int curY, int type, int direction) {
        if (curX == dstX && curY == dstY)
            return true;
        TileNode n = nodes[curX][curY];
        if (type == 6 || type == 7) {
            if (type == 7)
                direction = direction + 2 & 3;
            if (direction == 0) {
                if (curX == dstX + 1 && curY == dstY && (n.clipping & 0x80) == 0)
                    return true;
                if (curX == dstX && curY == dstY - 1 && (n.clipping & 2) == 0)
                    return true;
            } else if (direction == 1) {
                if (curX == dstX - 1 && curY == dstY && (n.clipping & 8) == 0)
                    return true;
                if (curX == dstX && curY == dstY - 1 && (n.clipping & 2) == 0)
                    return true;
            } else if (direction == 2) {
                if (curX == dstX - 1 && curY == dstY && (n.clipping & 8) == 0)
                    return true;
                if (curX == dstX && curY == dstY + 1 && (n.clipping & 0x20) == 0)
                    return true;
            } else if (direction == 3) {
                if (curX == dstX + 1 && curY == dstY && (n.clipping & 0x80) == 0)
                    return true;
                if (curX == dstX && curY == dstY + 1 && (n.clipping & 0x20) == 0)
                    return true;
            }
        }
        if (type == 8) {
            if (curX == dstX && curY == dstY + 1 && (n.clipping & 0x20) == 0)
                return true;
            if (curX == dstX && curY == dstY - 1 && (n.clipping & 2) == 0)
                return true;
            if (curX == dstX - 1 && curY == dstY && (n.clipping & 8) == 0)
                return true;
            if (curX == dstX + 1 && curY == dstY && (n.clipping & 0x80) == 0)
                return true;
        }
        return false;
    }

    private boolean reachedObject3(int dstX, int dstY, int curX, int curY, int sizeX, int sizeY, int walkToData) {
        if ((walkToData & 0x80000000) != 0) {
            if (curX == dstX && curY == dstY) {
                return false;
            }
        }

        int maxX = (dstX + sizeX) - 1;
        int maxY = (dstY + sizeY) - 1;
        if (curX >= dstX && curX <= maxX && curY >= dstY && curY <= maxY)
            return true;
        TileNode n = nodes[curX][curY];
        if (curX == dstX - 1 && curY >= dstY && curY <= maxY && (n.clipping & 8) == 0 && (walkToData & 8) == 0)
            return true;
        if (curX == maxX + 1 && curY >= dstY && curY <= maxY && (n.clipping & 0x80) == 0 && (walkToData & 2) == 0)
            return true;
        return curY == dstY - 1 && curX >= dstX && curX <= maxX && (n.clipping & 2) == 0 && (walkToData & 4) == 0 || curY == maxY + 1 && curX >= dstX && curX <= maxX && (n.clipping & 0x20) == 0 && (walkToData & 1) == 0;
    }
}
