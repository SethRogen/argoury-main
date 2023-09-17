package com.ziotic.logic.object;

import com.ziotic.Static;
import com.ziotic.logic.map.Region;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Lazaro
 */
public class ObjectManager {
    private Map<Tile, GameObject> previousObjects = new HashMap<Tile, GameObject>();

    public void add(int id, Tile loc, int type, int direction) {
        ObjectDefinition def = ObjectDefinition.forId(id);

        if (def == null) return;

        boolean original = false;
        GameObject originalObject = previousObjects.get(loc);

        GameObject lastObject = loc.getSpawnedObject();
        if (lastObject != null) {
            lastObject.setExists(false);
            lastObject.setLocation(null);
        }

        if (originalObject != null && originalObject.getId() == id) {
            original = true;
            previousObjects.remove(loc);
        } else {
            originalObject = Region.getObject(loc);
            if (originalObject != null) {
                previousObjects.put(loc, originalObject);
            }
        }

        Region region = Region.forTile(loc);

        region.removeObject(loc.getX() & 0x3f, loc.getY() & 0x3f, loc.getZ());

        int sizeX;
        int sizeY;
        if (direction != 1 && direction != 3) {
            sizeX = def.sizeX;
            sizeY = def.sizeY;
        } else {
            sizeX = def.sizeY;
            sizeY = def.sizeX;
        }
        GameObject obj = new GameObject(id, loc, type, direction, sizeX, sizeY, true);

        region.addObject(obj.getId(), loc.getX() & 0x3f, loc.getY() & 0x3f, loc.getZ(), obj.getType(), obj.getDirection(), false);

        refresh(obj);

        if (original) {
            obj.setExists(false);
            obj.setLocation(null);
        }
    }

    public void remove(Tile loc) {
        GameObject oldObj = loc.getSpawnedObject();
        if (oldObj != null) {
            checkNullObject(oldObj);

            oldObj.setExists(false);
            oldObj.setLocation(null);
        } else {
            oldObj = Region.getObject(loc);
            previousObjects.put(loc, oldObj);
        }

        Region region = Region.forTile(loc);

        region.removeObject(loc.getX() & 0x3f, loc.getY() & 0x3f, loc.getZ());

        GameObject obj = new GameObject(-1, loc, oldObj.getType(), oldObj.getDirection(), oldObj.getSizeX(), oldObj.getSizeY(), true);

        refresh(obj);

        if (oldObj.isSpawned()) {
            obj.setExists(false);
            obj.setLocation(null);
        }
    }

    private void checkNullObject(GameObject obj) {
        if (obj.getId() == -1) {
            GameObject originalObject = previousObjects.get(obj.getLocation());
            if (originalObject != null) {
                Tile loc = obj.getLocation();

                Region region = Region.forTile(loc);

                region.addObject(originalObject.getId(), loc.getX() & 0x3f, loc.getY() & 0x3f, loc.getZ(), originalObject.getType(), originalObject.getDirection(), false);

                previousObjects.remove(originalObject.getLocation());
            }
        }
    }

    private void refresh(GameObject obj) {
        for (Player player : Static.world.getLocalPlayers(obj.getLocation(), 48)) {
            refresh(player, obj);
        }
    }

    public void refresh(Player player) {
        for (Iterator<GameObject> it = player.getLocalGameObjects().iterator(); it.hasNext(); ) {
            GameObject obj = it.next();
            if (!obj.exists() || player.getLocation().differentMap(obj.getLocation())) {
                it.remove();
            }
        }
        for (GameObject obj : Static.world.getLocalObjects(player.getLocation())) {
            if (obj != null) {
                if (obj.exists() && !player.getLocalGameObjects().contains(obj)) {
                    refresh(player, obj);
                }
            }
        }
    }

    public void refresh(Player player, GameObject obj) {
        player.getLocalGameObjects().add(obj);

        if (obj.getId() != -1) {
            Static.proto.sendCreateGameObject(player, obj);
        } else {
            Static.proto.sendDestroyGameObject(player, obj);
        }
    }
}
