/**
 *
 */
package com.ziotic.logic.map;

import com.ziotic.Static;
import com.ziotic.adapter.protocol.cache.format.MapLoaderAdapter;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.object.ObjectDefinition;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

/**
 * @author Lazaro
 */
public final class Region {
    private static final Logger logger = Logging.log();

    private static final MapLoader MAP_LOADER = new MapLoaderAdapter();

    public static final int MAX_MAP_X = 16383, MAX_MAP_Y = 16383;

    public static final int MAX_MAP_Z = 3;

    private static Region[][] regions = new Region[(MAX_MAP_X + 1) / 64][(MAX_MAP_Y + 1) / 64];

    public static Region forRegionCoordinates(int x, int y) {
        Region r = regions[x][y];
        if (r == null) {
            r = regions[x][y] = new Region(x, y);
        }
        return r;
    }

    public static Region forAbsoluteCoordinates(int x, int y) {
        return forRegionCoordinates(x >> 6, y >> 6);
    }

    public static Region forTile(Tile tile) {
        return forRegionCoordinates(tile.getRegionX(), tile.getRegionY());
    }

    public static Tile getAbsoluteTile(int x, int y, int z) {
        return forAbsoluteCoordinates(x, y).getTile(x & 0x3f, y & 0x3f, z);
    }

    public static int getAbsoluteClipping(int x, int y, int z) {
        return forAbsoluteCoordinates(x, y).getClipping(x & 0x3f, y & 0x3f, z);
    }

    public static GameObject getObject(Tile tile) {
        return forTile(tile).getObject(tile.getX() & 0x3f, tile.getY() & 0x3f, tile.getZ());
    }
    
    public static GameObject getWallObject(Tile tile) {
    	return forTile(tile).getWallObject(tile.getX() & 0x3f, tile.getY() & 0x3f, tile.getZ());
    }

    private int x;
    private int y;

    private Tile[][][] tiles = null;

    private GameObject[][][] objects = null;
    private GameObject[][][] walls = null;
    private int[][][] clipping = null;

    private transient boolean loaded = false;

    public Region(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void load() {
        if (!loaded) {
            loaded = true;

            if (Static.isGame()) {
                try {
					Region.MAP_LOADER.loadMap(this, x, y);
                } catch (Throwable e) {
                    //logger.error("Error loading map for region [x= " + x +", y=" + y + "]!", e);
                }
                try {
                    Static.world.getDoorManager().loadRegion(this);
                } catch (Exception e) {
                    logger.error("Error loading doors for region [x= " + x + ", y=" + y + "]!", e);
                }
            }
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Tile[][][] getTiles() {
        return tiles;
    }

    public GameObject[][][] getObjects() {
        return objects;
    }

    public int[][][] getClipping() {
        return clipping;
    }

    @Override
    public int hashCode() {
        return x << 8 | y;
    }

    public Tile getTile(int x, int y, int z) {
        Region r = this;

        int absX = (this.x << 6) + x;
        int absY = (this.y << 6) + y;
        if ((absX >> 6) != this.x || (absY >> 6) != this.y) {
            r = forAbsoluteCoordinates(absX, absY);
            x = absX & 0x3f;
            y = absY & 0x3f;
        }

        if (r.tiles == null) {
            r.tiles = new Tile[4][][];
        }
        if (r.tiles[z] == null) {
            r.tiles[z] = new Tile[64][64];
        }

        Tile t = r.tiles[z][x][y];
        if (t == null) {
            t = r.tiles[z][x][y] = new Tile(absX, absY, z);
        }

        return t;
    }

    public int getClipping(int x, int y, int z) {
        load();

        Region r = this;

        int absX = (this.x << 6) + x;
        int absY = (this.y << 6) + y;
        if ((absX >> 6) != this.x || (absY >> 6) != this.y) {
            r = forAbsoluteCoordinates(absX, absY);
            x = absX & 0x3f;
            y = absY & 0x3f;
        }

        if (r.clipping == null || r.clipping[z] == null) {
            return 0;
        }

        return r.clipping[z][x][y];
    }

    private void registerObject(GameObject obj) {
        Tile loc = obj.getLocation();

        if (objects == null) {
            objects = new GameObject[4][][];
        }
        if (objects[loc.getZ()] == null) {
            objects[loc.getZ()] = new GameObject[64][64];
        } else {
            GameObject oldObj = objects[loc.getZ()][loc.getX() & 0x3f][loc.getY() & 0x3f];            
            if (oldObj != null && obj != null && !obj.getDefinition().hasActions()) {
                return;
            }
        }
        
        objects[loc.getZ()][loc.getX() & 0x3f][loc.getY() & 0x3f] = obj;
    }
    
    private void registerWallObject(GameObject obj) {
    	Tile loc = obj.getLocation();

        if (walls == null) {
            walls = new GameObject[4][][];
        }
        if (walls[loc.getZ()] == null) {
            walls[loc.getZ()] = new GameObject[64][64];
        } else {
            GameObject oldObj = walls[loc.getZ()][loc.getX() & 0x3f][loc.getY() & 0x3f];            
            if (oldObj != null && obj != null && !obj.getDefinition().hasActions()) {
                return;
            }
        }
        
        walls[loc.getZ()][loc.getX() & 0x3f][loc.getY() & 0x3f] = obj;
    }

    private void unregisterObject(GameObject obj) {
        Tile loc = obj.getLocation();

        if (objects == null || objects[loc.getZ()] == null) {
            return;
        }

        objects[loc.getZ()][loc.getX() & 0x3f][loc.getY() & 0x3f] = null;
    }
    
    private void unregisterWallObject(GameObject obj) {
    	Tile loc = obj.getLocation();

        if (walls == null || walls[loc.getZ()] == null) {
            return;
        }

        walls[loc.getZ()][loc.getX() & 0x3f][loc.getY() & 0x3f] = null;
    }

    public GameObject getObject(int x, int y, int z) {
        load();

        if (objects == null || objects[z] == null) {
            return null;
        }

        return objects[z][x][y];
    }
    
    public GameObject getWallObject(int x, int y, int z) {
        load();
        if (walls == null || walls[z] == null) {
            return null;
        }

        return walls[z][x][y];
    }

    public void addObject(int objectId, int x, int y, int z, int type, int direction, boolean ignoreObjects) {
       //load(); //was called too much when loading regions, re-add if it fucks up anything.
        if (objectId == -1) {
            if (!ignoreObjects) {
                removeObject(x, y, z);
            }
            return;
        }

        ObjectDefinition def = ObjectDefinition.forId(objectId);
        if (def == null) {
            return;
        }
        int sizeX;
        int sizeY;
        if (direction != 1 && direction != 3) {
            sizeX = def.sizeX;
            sizeY = def.sizeY;
        } else {
            sizeX = def.sizeY;
            sizeY = def.sizeX;
        }
        boolean objectAdded = false;

        if (type == 22) {
            if (def.actionCount == 1) {
                if (!ignoreObjects) {
                    removeObject(x, y, z);
                }
                clip(x, y, z, 0x200000);
                registerObject(new GameObject(objectId, Tile.locate((this.x << 6) + x, (this.y << 6) + y, z), type, direction, sizeX, sizeY));
                objectAdded = true;
            }
        } else if (type >= 9 && type <= 11) {
            if (def.actionCount != 0) {
                if (!ignoreObjects) {
                    removeObject(x, y, z);
                }
                clipSolidObject(x, y, z, sizeX, sizeY, def.walkable, !def.clippingFlag);
                registerObject(new GameObject(objectId, Tile.locate((this.x << 6) + x, (this.y << 6) + y, z), type, direction, sizeX, sizeY));
                objectAdded = true;
            }
        } else if (type >= 0 && type <= 3) {
            if (def.actionCount != 0) {
                if (!ignoreObjects) {
                    removeObject(x, y, z);
                }
                GameObject wallObject = new GameObject(objectId, Tile.locate((this.x << 6) + x, (this.y << 6) + y, z), type, direction, sizeX, sizeY);
                registerWallObject(wallObject);
                clipVariableObject(x, y, z, type, direction, def.walkable, !def.clippingFlag);
                registerObject(wallObject);
                objectAdded = true;
            }
        }
        if (!objectAdded/* && def.hasActions()*/) {
            registerObject(new GameObject(objectId, Tile.locate((this.x << 6) + x, (this.y << 6) + y, z), type, direction, sizeX, sizeY));
        }
    }

    public void removeObject(int x, int y, int z) {
        load();
        
        GameObject wall = getWallObject(x, y, z);
        if (wall != null) {
        	unregisterWallObject(wall);
        }

        GameObject oldObj = getObject(x, y, z);
        if (oldObj != null) {
            unregisterObject(oldObj);

            ObjectDefinition def = oldObj.getDefinition();

            int sizeX;
            int sizeY;
            if (oldObj.getDirection() != 1 && oldObj.getDirection() != 3) {
                sizeX = def.sizeX;
                sizeY = def.sizeY;
            } else {
                sizeX = def.sizeY;
                sizeY = def.sizeX;
            }

            if (oldObj.getType() == 22) {
                if (def.actionCount == 1) {
                    unClip(x, y, z, 0x200000);
                }
            } else if (oldObj.getType() >= 9) {
                if (def.actionCount != 0) {
                    unClipSolidObject(x, y, z, sizeX, sizeY, def.walkable, !def.clippingFlag);
                }
            } else if (oldObj.getType() >= 0 && oldObj.getType() <= 3) {
                if (def.actionCount != 0) {
                    unClipVariableObject(x, y, z, oldObj.getType(), oldObj.getDirection(), def.walkable, !def.clippingFlag);
                }
            }
        }
    }

    public void clip(int x, int y, int z, int shift) {
        Region r = this;

        int absX = (this.x << 6) + x;
        int absY = (this.y << 6) + y;
        if ((absX >> 6) != this.x || (absY >> 6) != this.y) {
            r = forAbsoluteCoordinates(absX, absY);
            x = absX & 0x3f;
            y = absY & 0x3f;
        }

        if (r.clipping == null) {
            r.clipping = new int[4][][];
        }
        if (r.clipping[z] == null) {
            r.clipping[z] = new int[64][64];
        }

        r.clipping[z][x][y] |= shift;
    }

    private void unClip(int x, int y, int z, int shift) {
        Region r = this;

        int absX = (this.x << 6) + x;
        int absY = (this.y << 6) + y;
        if ((absX >> 6) != this.x || (absY >> 6) != this.y) {
            r = forAbsoluteCoordinates(absX, absY);
            x = absX & 0x3f;
            y = absY & 0x3f;
        }

        if (r.clipping == null || r.clipping[z] == null) {
            return;
        }

        r.clipping[z][x][y] &= ~shift;
    }

    private void clipSolidObject(int x, int y, int z, int sizeX, int sizeY, boolean flag, boolean flag2) {
        int clipping = 256;
        if (flag) {
            clipping |= 0x20000;
        }
        if (flag2) {
            clipping |= 0x40000000;
        }

        int maxX = x + sizeX;
        int maxY = y + sizeY;

        for (int x2 = x; x2 < maxX; x2++) {
            for (int y2 = y; y2 < maxY; y2++) {
                clip(x2, y2, z, clipping);
            }
        }
    }

    private void clipVariableObject(int x, int y, int z, int type, int direction, boolean flag, boolean flag2) {
        if (type == 0) {
            if (direction == 0) {
                clip(x, y, z, 128);
                clip(x - 1, y, z, 8);
            } else if (direction == 1) {
                clip(x, y, z, 2);
                clip(x, y + 1, z, 32);
            } else if (direction == 2) {
                clip(x, y, z, 8);
                clip(x + 1, y, z, 128);
            } else if (direction == 3) {
                clip(x, y, z, 32);
                clip(x, y - 1, z, 2);
            }
        } else if (type == 1 || type == 3) {
            if (direction == 0) {
                clip(x, y, z, 1);
                clip(x - 1, y, z, 16);
            } else if (direction == 1) {
                clip(x, y, z, 4);
                clip(x + 1, y + 1, z, 64);
            } else if (direction == 2) {
                clip(x, y, z, 16);
                clip(x + 1, y - 1, z, 1);
            } else if (direction == 3) {
                clip(x, y, z, 64);
                clip(x - 1, y - 1, z, 4);
            }
        } else if (type == 2) {
            if (direction == 0) {
                clip(x, y, z, 130);
                clip(x - 1, y, z, 8);
                clip(x, y + 1, z, 32);
            } else if (direction == 1) {
                clip(x, y, z, 10);
                clip(x, y + 1, z, 32);
                clip(x + 1, y, z, 128);
            } else if (direction == 2) {
                clip(x, y, z, 40);
                clip(x + 1, y, z, 128);
                clip(x, y - 1, z, 2);
            } else if (direction == 3) {
                clip(x, y, z, 160);
                clip(x, y - 1, z, 2);
                clip(x - 1, y, z, 8);
            }
        }
        if (flag) {
            if (type == 0) {
                if (direction == 0) {
                    clip(x, y, z, 65536);
                    clip(x - 1, y, z, 4096);
                } else if (direction == 1) {
                    clip(x, y, z, 1024);
                    clip(x, y + 1, z, 16384);
                } else if (direction == 2) {
                    clip(x, y, z, 4096);
                    clip(x + 1, y, z, 65536);
                } else if (direction == 3) {
                    clip(x, y, z, 16384);
                    clip(x, y - 1, z, 1024);
                }
            }
            if (type == 1 || type == 3) {
                if (direction == 0) {
                    clip(x, y, z, 512);
                    clip(x - 1, y + 1, z, 8192);
                } else if (direction == 1) {
                    clip(x, y, z, 2048);
                    clip(x + 1, y + 1, z, 32768);
                } else if (direction == 2) {
                    clip(x, y, z, 8192);
                    clip(x + 1, y + 1, z, 512);
                } else if (direction == 3) {
                    clip(x, y, z, 32768);
                    clip(x - 1, y - 1, z, 2048);
                }
            } else if (type == 2) {
                if (direction == 0) {
                    clip(x, y, z, 66560);
                    clip(x - 1, y, z, 4096);
                    clip(x, y + 1, z, 16384);
                } else if (direction == 1) {
                    clip(x, y, z, 5120);
                    clip(x, y + 1, z, 16384);
                    clip(x + 1, y, z, 65536);
                } else if (direction == 2) {
                    clip(x, y, z, 20480);
                    clip(x + 1, y, z, 65536);
                    clip(x, y - 1, z, 1024);
                } else if (direction == 3) {
                    clip(x, y, z, 81920);
                    clip(x, y - 1, z, 1024);
                    clip(x - 1, y, z, 4096);
                }
            }
        }
        if (flag2) {
            if (type == 0) {
                if (direction == 0) {
                    clip(x, y, z, 536870912);
                    clip(x - 1, y, z, 33554432);
                } else if (direction == 1) {
                    clip(x, y, z, 8388608);
                    clip(x, y + 1, z, 134217728);
                } else if (direction == 2) {
                    clip(x, y, z, 33554432);
                    clip(x + 1, y, z, 536870912);
                } else if (direction == 3) {
                    clip(x, y, z, 134217728);
                    clip(x, y - 1, z, 8388608);
                }
            }
            if (type == 1 || type == 3) {
                if (direction == 0) {
                    clip(x, y, z, 4194304);
                    clip(x - 1, y + 1, z, 67108864);
                } else if (direction == 1) {
                    clip(x, y, z, 16777216);
                    clip(x + 1, y + 1, z, 268435456);
                } else if (direction == 2) {
                    clip(x, y, z, 67108864);
                    clip(x + 1, y + 1, z, 4194304);
                } else if (direction == 3) {
                    clip(x, y, z, 268435456);
                    clip(x - 1, y - 1, z, 16777216);
                }
            } else if (type == 2) {
                if (direction == 0) {
                    clip(x, y, z, 545259520);
                    clip(x - 1, y, z, 33554432);
                    clip(x, y + 1, z, 134217728);
                } else if (direction == 1) {
                    clip(x, y, z, 41943040);
                    clip(x, y + 1, z, 134217728);
                    clip(x + 1, y, z, 536870912);
                } else if (direction == 2) {
                    clip(x, y, z, 167772160);
                    clip(x + 1, y, z, 536870912);
                    clip(x, y - 1, z, 8388608);
                } else if (direction == 3) {
                    clip(x, y, z, 671088640);
                    clip(x, y - 1, z, 8388608);
                    clip(x - 1, y, z, 33554432);
                }
            }
        }
    }

    private void unClipSolidObject(int x, int y, int z, int sizeX, int sizeY, boolean flag, boolean flag2) {
        int clipping = 256;
        if (flag) {
            clipping |= 0x20000;
        }
        if (flag2) {
            clipping |= 0x40000000;
        }

        int maxX = x + sizeX;
        int maxY = y + sizeY;

        for (int x2 = x; x2 < maxX; x2++) {
            for (int y2 = y; y2 < maxY; y2++) {
                unClip(x2, y2, z, clipping);
            }
        }
    }

    private void unClipVariableObject(int x, int y, int z, int type, int direction, boolean flag, boolean flag2) {
        if (type == 0) {
            if (direction == 0) {
                unClip(x, y, z, 128);
                unClip(x - 1, y, z, 8);
            } else if (direction == 1) {
                unClip(x, y, z, 2);
                unClip(x, y + 1, z, 32);
            } else if (direction == 2) {
                unClip(x, y, z, 8);
                unClip(x + 1, y, z, 128);
            } else if (direction == 3) {
                unClip(x, y, z, 32);
                unClip(x, y - 1, z, 2);
            }
        } else if (type == 1 || type == 3) {
            if (direction == 0) {
                unClip(x, y, z, 1);
                unClip(x - 1, y, z, 16);
            } else if (direction == 1) {
                unClip(x, y, z, 4);
                unClip(x + 1, y + 1, z, 64);
            } else if (direction == 2) {
                unClip(x, y, z, 16);
                unClip(x + 1, y - 1, z, 1);
            } else if (direction == 3) {
                unClip(x, y, z, 64);
                unClip(x - 1, y - 1, z, 4);
            }
        } else if (type == 2) {
            if (direction == 0) {
                unClip(x, y, z, 130);
                unClip(x - 1, y, z, 8);
                unClip(x, y + 1, z, 32);
            } else if (direction == 1) {
                unClip(x, y, z, 10);
                unClip(x, y + 1, z, 32);
                unClip(x + 1, y, z, 128);
            } else if (direction == 2) {
                unClip(x, y, z, 40);
                unClip(x + 1, y, z, 128);
                unClip(x, y - 1, z, 2);
            } else if (direction == 3) {
                unClip(x, y, z, 160);
                unClip(x, y - 1, z, 2);
                unClip(x - 1, y, z, 8);
            }
        }
        if (flag) {
            if (type == 0) {
                if (direction == 0) {
                    unClip(x, y, z, 65536);
                    unClip(x - 1, y, z, 4096);
                } else if (direction == 1) {
                    unClip(x, y, z, 1024);
                    unClip(x, y + 1, z, 16384);
                } else if (direction == 2) {
                    unClip(x, y, z, 4096);
                    unClip(x + 1, y, z, 65536);
                } else if (direction == 3) {
                    unClip(x, y, z, 16384);
                    unClip(x, y - 1, z, 1024);
                }
            }
            if (type == 1 || type == 3) {
                if (direction == 0) {
                    unClip(x, y, z, 512);
                    unClip(x - 1, y + 1, z, 8192);
                } else if (direction == 1) {
                    unClip(x, y, z, 2048);
                    unClip(x + 1, y + 1, z, 32768);
                } else if (direction == 2) {
                    unClip(x, y, z, 8192);
                    unClip(x + 1, y + 1, z, 512);
                } else if (direction == 3) {
                    unClip(x, y, z, 32768);
                    unClip(x - 1, y - 1, z, 2048);
                }
            } else if (type == 2) {
                if (direction == 0) {
                    unClip(x, y, z, 66560);
                    unClip(x - 1, y, z, 4096);
                    unClip(x, y + 1, z, 16384);
                } else if (direction == 1) {
                    unClip(x, y, z, 5120);
                    unClip(x, y + 1, z, 16384);
                    unClip(x + 1, y, z, 65536);
                } else if (direction == 2) {
                    unClip(x, y, z, 20480);
                    unClip(x + 1, y, z, 65536);
                    unClip(x, y - 1, z, 1024);
                } else if (direction == 3) {
                    unClip(x, y, z, 81920);
                    unClip(x, y - 1, z, 1024);
                    unClip(x - 1, y, z, 4096);
                }
            }
        }
        if (flag2) {
            if (type == 0) {
                if (direction == 0) {
                    unClip(x, y, z, 536870912);
                    unClip(x - 1, y, z, 33554432);
                } else if (direction == 1) {
                    unClip(x, y, z, 8388608);
                    unClip(x, y + 1, z, 134217728);
                } else if (direction == 2) {
                    unClip(x, y, z, 33554432);
                    unClip(x + 1, y, z, 536870912);
                } else if (direction == 3) {
                    unClip(x, y, z, 134217728);
                    unClip(x, y - 1, z, 8388608);
                }
            }
            if (type == 1 || type == 3) {
                if (direction == 0) {
                    unClip(x, y, z, 4194304);
                    unClip(x - 1, y + 1, z, 67108864);
                } else if (direction == 1) {
                    unClip(x, y, z, 16777216);
                    unClip(x + 1, y + 1, z, 268435456);
                } else if (direction == 2) {
                    unClip(x, y, z, 67108864);
                    unClip(x + 1, y + 1, z, 4194304);
                } else if (direction == 3) {
                    unClip(x, y, z, 268435456);
                    unClip(x - 1, y - 1, z, 16777216);
                }
            } else if (type == 2) {
                if (direction == 0) {
                    unClip(x, y, z, 545259520);
                    unClip(x - 1, y, z, 33554432);
                    unClip(x, y + 1, z, 134217728);
                } else if (direction == 1) {
                    unClip(x, y, z, 41943040);
                    unClip(x, y + 1, z, 134217728);
                    unClip(x + 1, y, z, 536870912);
                } else if (direction == 2) {
                    unClip(x, y, z, 167772160);
                    unClip(x + 1, y, z, 536870912);
                    unClip(x, y - 1, z, 8388608);
                } else if (direction == 3) {
                    unClip(x, y, z, 671088640);
                    unClip(x, y - 1, z, 8388608);
                    unClip(x - 1, y, z, 33554432);
                }
            }
        }
    }
}
