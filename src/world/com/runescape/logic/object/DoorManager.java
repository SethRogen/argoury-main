package com.runescape.logic.object;

import com.runescape.Constants;
import com.runescape.Static;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ObjectOptionHandler;
import com.runescape.engine.event.DelayedEvent;
import com.runescape.logic.Locatable;
import com.runescape.logic.map.PathProcessor;
import com.runescape.logic.map.Region;
import com.runescape.logic.map.Tile;
import com.runescape.logic.object.DoorManager.DoorDefinition.SpecificDoorDefinition;
import com.runescape.logic.player.Player;
import com.runescape.utility.ArrayUtilities;
import com.runescape.utility.Logging;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lazaro
 */
public class DoorManager implements ObjectOptionHandler {
    private static final Logger logger = Logging.log();

    public static enum DoorState {
        CLOSED, OPENED
    }

    public static class DoorDefinition {
        public static class SpecificDoorDefinition {
            private int changedDir;
            private int xOffset;
            private int yOffset;

            public SpecificDoorDefinition(int changedDir, int xOffset, int yOffset) {
                this.changedDir = changedDir;
                this.xOffset = xOffset;
                this.yOffset = yOffset;
            }

            public int getChangedDir() {
                return changedDir;
            }

            public int getXOffset() {
                return xOffset;
            }

            public int getYOffset() {
                return yOffset;
            }
        }

        private int defaultId;
        private int changedId;
        private DoorState defaultState;
        private boolean instantClose;
        private int dirType;
        private int tiedId;
        private Map<Tile, SpecificDoorDefinition> sDefs;
        private boolean lockPick;

        public DoorDefinition(int defaultId, int changedId, DoorState defaultState, boolean lockPick, boolean instantClose, int dirType, int tiedId, Map<Tile, SpecificDoorDefinition> sDefs) {
            this.defaultId = defaultId;
            this.changedId = changedId;
            this.defaultState = defaultState;
            this.instantClose = instantClose;
            this.dirType = dirType;
            this.tiedId = tiedId;
            this.sDefs = sDefs;
            this.lockPick = lockPick;
        }

        public int getDefaultId() {
            return defaultId;
        }

        public int getChangedId() {
            return changedId;
        }

        public DoorState getDefaultState() {
            return defaultState;
        }

        public boolean isInstantClose() {
            return instantClose;
        }

        public int getDirType() {
            return dirType;
        }

        public int getTiedId() {
            return tiedId;
        }

        public Map<Tile, SpecificDoorDefinition> getSDefs() {
            return sDefs;
        }

        public SpecificDoorDefinition getSDef(Tile loc) {
            if (sDefs == null) {
                return null;
            }
            return sDefs.get(loc);
        }
    }

    public static class Door extends Locatable {
        public final DoorDefinition def;
        public DoorState state;

        public int dirOffset = 0;
        public int xOffset = 0;
        public int yOffset = 0;

        public Door tiedDoor = null;
        public Tile tiedLoc = null;

        public long lastChange = 0;

        public Door(DoorDefinition def, GameObject obj) {
            this.def = def;
            this.state = def.getDefaultState();

            /*
             * normal doors (no large, no gate)
             * 0-knob south, aligned west
             * 1-knob west, aligned north
             * 2-knob north, aligned east
             * 3-knob east, aligned south
             */

            SpecificDoorDefinition sDef = def.getSDef(obj.getLocation());
            if (sDef == null) {
                dirOffset = 1;
                switch (obj.getDirection()) {
                    case 0:
                        xOffset = -1; // 0 is aligned west
                        break;
                    case 1:
                        yOffset = 1; // 1 is aligned north
                        break;
                    case 2:
                        xOffset = 1; // 2 is aligned east
                        break;
                    case 3:
                        yOffset = -1; // 3 is aligned south
                        break;
                }
                if (def.getDefaultState() == DoorManager.DoorState.OPENED || def.getDirType() == 1) {
                    dirOffset = -dirOffset;

                    if (def.getDefaultState() == DoorManager.DoorState.OPENED) {
                        xOffset = -xOffset;
                        yOffset = -yOffset;
                    }
                }
            } else {
                dirOffset = sDef.getChangedDir() - obj.getDirection();
                xOffset = sDef.getXOffset();
                yOffset = sDef.getYOffset();
            }

            setLocation(obj.getLocation());
        }

        public void change(Tile loc, boolean repeated) {
            state = state == DoorManager.DoorState.CLOSED ? DoorManager.DoorState.OPENED : DoorManager.DoorState.CLOSED;
            lastChange = System.currentTimeMillis();

            if (tiedDoor != null && tiedLoc != null) {
                if (!repeated) {
                    GameObject obj2 = Region.getObject(tiedLoc);
                    if (obj2 != null) {
                        Static.world.getDoorManager().changeDoor(tiedDoor, obj2, true);
                    }
                }

                tiedDoor.tiedLoc = loc;
            }

            setLocation(loc);
        }
    }

    private Map<Integer, DoorDefinition> doorDefinitionMap = new HashMap<Integer, DoorDefinition>();
    private Map<Tile, Door> doorMap = new HashMap<Tile, Door>();

    public void load() {
        try {
            int doorCount = 0;

            BufferedReader cfg = new BufferedReader(new FileReader(Static.parseString("%WORK_DIR%/map/doors.ini")));
            int lineNumber = 0;
            String line;
            while ((line = cfg.readLine()) != null) {
                if (line.startsWith("#") || line.trim().length() == 0) {
                    continue;
                }

                String[] args = line.split("(\t| )");

                int defaultId = Integer.parseInt(args[0]);
                int changedId = Integer.parseInt(args[1]);
                DoorState defaultState = args[2].equalsIgnoreCase("opened") ? DoorState.OPENED : DoorState.CLOSED;
                boolean lockPick = false;
                boolean instantClose = false;
                int tiedId = -1;
                Map<Tile, SpecificDoorDefinition> sDefs = null;
                int dirType = 0;
                if (args.length > 3) {
                	lockPick = args[3].equalsIgnoreCase("true");
                }
                if (args.length > 4) {
                    instantClose = args[4].equalsIgnoreCase("true");
                }
                if (args.length > 5) {
                    dirType = Integer.parseInt(args[5]);
                }
                if (args.length > 6) {
                    tiedId = Integer.parseInt(args[6]);
                }
                if (args.length > 9) {
                    String[] locStr = args[7].split(",");
                    Tile loc = Tile.locate(Integer.parseInt(locStr[0]), Integer.parseInt(locStr[1]), Integer.parseInt(locStr[2]));

                    int changedDir = Integer.parseInt(args[8]);
                    int xOffset = Integer.parseInt(args[9]);
                    int yOffset = Integer.parseInt(args[10]);

                    SpecificDoorDefinition sDef = new SpecificDoorDefinition(changedDir, xOffset, yOffset);
                    if (doorDefinitionMap.containsKey(defaultId)) {
                        DoorDefinition def = doorDefinitionMap.get(defaultId);
                        if (def.getSDefs() != null) {
                            def.getSDefs().put(loc, sDef);

                            lineNumber++;
                            continue;
                        }
                    } else {
                        sDefs = new HashMap<Tile, SpecificDoorDefinition>();
                        sDefs.put(loc, sDef);
                    }
                }

                doorDefinitionMap.put(defaultId, new DoorDefinition(defaultId, changedId, defaultState, lockPick, instantClose, dirType, tiedId, sDefs));
                doorCount++;

                lineNumber++;
            }

            load(Static.ahs);

            logger.info("Loaded " + doorCount + " doors");
        } catch (Exception e) {
            logger.error("Error loading doors!", e);
        }
    }

    @Override
    public void load(ActionHandlerSystem system) {
        int[] objectIds = ArrayUtilities.primitive(doorDefinitionMap.keySet().toArray(new Integer[0]));
        system.registerObjectOptionHandler(objectIds, this);
        List<Integer> changedObjectsIds = new ArrayList<Integer>();
        for (DoorDefinition door : doorDefinitionMap.values()) {
        	if (!door.instantClose)
        		changedObjectsIds.add(door.getChangedId());
        }
        system.registerObjectOptionHandler(ArrayUtilities.primitive(changedObjectsIds.toArray(new Integer[0])), this);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleObjectOption1(Player player, GameObject obj) {
        useDoor(player, obj, false);
    }

    @Override
    public void handleObjectOption2(Player player, GameObject obj) {
    }

    @Override
    public void handleObjectOption3(Player player, GameObject obj) {
    }

    public void loadRegion(Region region) {
        if (region.getObjects() != null) {
            for (int z = 0; z < 4; z++) {
                if (region.getObjects()[z] != null) {
                    for (int x = 0; x < 64; x++) {
                        for (int y = 0; y < 64; y++) {
                            GameObject obj = region.getObjects()[z][x][y];
                            if (obj != null) {
                                if (obj != null) {
                                    DoorDefinition doorDef = doorDefinitionMap.get(obj.getId());
                                    if (doorDef != null) {
                                        Door door = new Door(doorDef, obj);

                                        doorMap.put(obj.getLocation(), door);
                                        if (door.xOffset != 0 || door.yOffset != 0) {
                                            doorMap.put(obj.getLocation().translate(door.xOffset, door.yOffset, 0), door);
                                        }

                                        if (doorDef.tiedId != -1) {
                                            for (int x2 = obj.getX() - 2; x2 < obj.getX() + 2; x2++) {
                                                for (int y2 = obj.getY() - 2; y2 < obj.getY() + 2; y2++) {
                                                    GameObject obj2 = Region.getObject(Tile.locate(x2, y2, z));
                                                    if (obj2 != null && obj2.getId() == doorDef.getTiedId()) {
                                                        Door door2 = doorMap.get(obj2.getLocation());
                                                        if (door2 != null) {
                                                            door.tiedDoor = door2;
                                                            door.tiedLoc = obj2.getLocation();

                                                            door2.tiedDoor = door;
                                                            door2.tiedLoc = obj.getLocation();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void useDoor(final Player player, final GameObject obj, boolean lockPick) {
        final Door door = doorMap.get(obj.getLocation());
        if (door == null) {
            logger.warn("Encountered nulled door [id=" + obj.getId() + ", loc=[" + obj.getLocation() + "]]");
            return;
        }
        logger.warn("Door [id=" + obj.getId() + ", loc=[" + obj.getLocation() + "], dir=" + obj.getDirection() + "]");
        if (door.def.lockPick && !lockPick && door.state == DoorManager.DoorState.CLOSED) {
        	player.sendMessage("This door is locked.");
        	return;
        }
        
        if (!door.def.isInstantClose()) {
            if (System.currentTimeMillis() - door.lastChange >= Constants.GAME_TICK_INTERVAL) {
                changeDoor(door, obj);
            }
        } else {
            int offX = 0;
            int offY = 0;
            switch (obj.getDirection()) {
                case 0:
                    offX = -1;
                    if (player.getX() < obj.getX()) {
                        offX = -offX;
                    }
                    break;
                case 1:
                    offY = 1;
                    if (player.getY() > obj.getY()) {
                        offY = -offY;
                    }
                    break;
                case 2:
                    offX = 1;
                    if (player.getX() > obj.getX()) {
                        offX = -offX;
                    }
                    break;
                case 3:
                    offY = -1;
                    if (player.getY() < obj.getY()) {
                        offY = -offY;
                    }
                    break;
            }

            Tile dest = player.getLocation().translate(offX, offY, 0);

            player.getPathProcessor().reset(true);
            player.getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_WALK);
            player.getPathProcessor().add(dest);

            if (door.state == DoorManager.DoorState.CLOSED) {
                changeDoor(door, obj);
            }

            Static.engine.submit(new DelayedEvent(1200) {
                @Override
                public void run() {
                    if (door.state == DoorManager.DoorState.OPENED) {
                        changeDoor(door, Region.getObject(door.getLocation()));

                        player.getPathProcessor().reset(true);
                    }
                }
            });
        }
    }

    private void changeDoor(Door door, GameObject currentObj) {
        changeDoor(door, currentObj, false);
    }

    private void changeDoor(Door door, GameObject currentObj, boolean repeated) {
        int newId = door.state == door.def.getDefaultState() ? door.def.getChangedId() : door.def.getDefaultId();
        int newDir = door.state == door.def.getDefaultState() ? ((currentObj.getDirection() + door.dirOffset) & 0x3) : ((currentObj.getDirection() - door.dirOffset) & 0x3);
        int newX = door.state == door.def.getDefaultState() ? (currentObj.getX() + door.xOffset) : (currentObj.getX() - door.xOffset);
        int newY = door.state == door.def.getDefaultState() ? (currentObj.getY() + door.yOffset) : (currentObj.getY() - door.yOffset);

        Tile newLoc = Tile.locate(newX, newY, currentObj.getZ());

        door.change(newLoc, repeated);

        Static.world.getObjectManager().remove(currentObj.getLocation());
        Static.world.getObjectManager().add(newId, newLoc, currentObj.getType(), newDir);
    }
}
