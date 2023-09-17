package com.ziotic.content.misc;

import com.ziotic.Static;
import com.ziotic.content.handler.ActionHandler;
import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.content.handler.ObjectOptionHandler;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.utility.DoubleNodeRunnable;
import com.ziotic.utility.ArrayUtilities;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class PlaneObjectHandler implements ActionHandler, ObjectOptionHandler {
    private static Logger logger = Logging.log();

    private static enum PlaneDirection {
        UP, DOWN
    }

    private static DoubleNodeRunnable<Player, GameObject> LADDER_UP_HANDLER = new DoubleNodeRunnable<Player, GameObject>() {
        @Override
        public void run(final Player player, final GameObject obj) {
            player.doAnimation(828, 20);
            player.registerTick(new Tick("teleport") {
                @Override
                public boolean execute() {
                    player.setTeleportDestination(player.getLocation().translate(0, 0, 1));
                    return false;
                }
            });
        }
    };

    private static DoubleNodeRunnable<Player, GameObject> LADDER_DOWN_HANDLER = new DoubleNodeRunnable<Player, GameObject>() {
        @Override
        public void run(final Player player, final GameObject obj) {
            player.doAnimation(828, 20);
            player.registerTick(new Tick("teleport") {
                @Override
                public boolean execute() {
                    player.setTeleportDestination(player.getLocation().translate(0, 0, -1));
                    return false;
                }
            });
        }
    };

    private static DoubleNodeRunnable<Player, GameObject> SPIRAL_STAIR_UP_HANDLER = new DoubleNodeRunnable<Player, GameObject>() {
        @Override
        public void run(final Player player, final GameObject obj) {
            player.setTeleportDestination(player.getLocation().translate(0, 0, 1));
        }
    };

    private static DoubleNodeRunnable<Player, GameObject> SPIRAL_STAIR_DOWN_HANDLER = new DoubleNodeRunnable<Player, GameObject>() {
        @Override
        public void run(final Player player, final GameObject obj) {
            player.setTeleportDestination(player.getLocation().translate(0, 0, -1));
        }
    };

    private static DoubleNodeRunnable<Player, GameObject> VAR_STAIR_UP_HANDLER = new DoubleNodeRunnable<Player, GameObject>() {
        @Override
        public void run(final Player player, final GameObject obj) {
            int xOff = 0;
            int yOff = 0;

            if (obj.getSizeX() > obj.getSizeY()) {
                xOff = obj.getSizeX() + 1;
                if (player.getX() > obj.getX()) {
                    xOff = -xOff;
                }
            } else if (obj.getSizeY() > obj.getSizeX()) {
                yOff = obj.getSizeY() + 1;
                if (player.getY() > obj.getY()) {
                    yOff = -yOff;
                }
            }

            player.setTeleportDestination(player.getLocation().translate(xOff, yOff, 1));
        }
    };

    private static DoubleNodeRunnable<Player, GameObject> VAR_STAIR_DOWN_HANDLER = new DoubleNodeRunnable<Player, GameObject>() {
        @Override
        public void run(final Player player, final GameObject obj) {
            int xOff = 0;
            int yOff = 0;

            if (obj.getSizeX() > obj.getSizeY()) {
                xOff = obj.getSizeX() + 1;
                if (player.getX() > obj.getX()) {
                    xOff = -xOff;
                }
            } else if (obj.getSizeY() > obj.getSizeX()) {
                yOff = obj.getSizeY() + 1;
                if (player.getY() > obj.getY()) {
                    yOff = -yOff;
                }
            }

            player.setTeleportDestination(player.getLocation().translate(xOff, yOff, -1));
        }
    };

    private static class SquareVarStairHandler implements DoubleNodeRunnable<Player, GameObject> {
        public static enum LocationChange {
            X_CHANGE, Y_CHANGE
        }

        private PlaneDirection dir;
        private LocationChange change;

        public SquareVarStairHandler(PlaneDirection dir, LocationChange change) {
            this.dir = dir;
            this.change = change;
        }

        @Override
        public void run(Player player, GameObject obj) {
            int xOff = 0;
            int yOff = 0;

            switch (change) {
                case X_CHANGE:
                    xOff = obj.getSizeX() + 1;
                    if (dir == PlaneDirection.DOWN) {
                        xOff += 2;
                    }
                    if (player.getX() > obj.getX()) {
                        xOff = -xOff;
                    }
                    break;
                case Y_CHANGE:
                    yOff = obj.getSizeY() + 1;
                    if (dir == PlaneDirection.DOWN) {
                        yOff += 2;
                    }
                    if (player.getY() > obj.getY()) {
                        yOff = -yOff;
                    }
                    break;
            }

            player.setTeleportDestination(player.getLocation().translate(xOff, yOff, dir == PlaneObjectHandler.PlaneDirection.UP ? 1 : -1));
        }
    }

    private static class UniquePlaneObjectHandler implements DoubleNodeRunnable<Player, GameObject> {
        private Tile dest;

        public UniquePlaneObjectHandler(Tile dest) {
            this.dest = dest;
        }

        @Override
        public void run(Player player, GameObject obj) {
            player.setTeleportDestination(dest);
        }
    }
    
    private static class OffsetBasedPlaneObjectHandler implements DoubleNodeRunnable<Player, GameObject> {
    	
    	private int xLoc;
    	private int yLoc;
    	private int zLoc;
  
    	private int xOffset;
    	private int yOffset;
    	private int zOffset;
    	
    	public OffsetBasedPlaneObjectHandler(int xLoc, int yLoc, int zLoc, int xOffset, int yOffset, int zOffset) {
    		this.xLoc = xLoc;
    		this.yLoc = yLoc;
    		this.zLoc = zLoc;
    		this.xOffset = xOffset;
    		this.yOffset = yOffset;
    		this.zOffset = zOffset;
    	}
    	
    	@Override
    	public void run(Player player, GameObject obj) {
    		if (obj.getLocation() == Tile.locate(xLoc, yLoc, zLoc))	
    			player.setTeleportDestination(player.getLocation().translate(xOffset, yOffset, zOffset));
    	}
    }
    
    private static class OffsetBasedPlaneObjectHandler2 implements DoubleNodeRunnable<Player, GameObject> {
  
    	private int xOffset;
    	private int yOffset;
    	private int zOffset;
    	
    	public OffsetBasedPlaneObjectHandler2(int xOffset, int yOffset, int zOffset) {
    		this.xOffset = xOffset;
    		this.yOffset = yOffset;
    		this.zOffset = zOffset;
    	}
    	
    	@Override
    	public void run(Player player, GameObject obj) {
    		player.setTeleportDestination(player.getLocation().translate(xOffset, yOffset, zOffset));
    	}
    }

    private Map<Integer, DoubleNodeRunnable<Player, GameObject>> objectHandlerMap = new HashMap<Integer, DoubleNodeRunnable<Player, GameObject>>();

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        BufferedReader cfg = new BufferedReader(new FileReader(Static.parseString("%WORK_DIR%/world/mapData/planeobjects.ini")));
        int lineNumber = 0;
        String line;
        while ((line = cfg.readLine()) != null) {
            if (line.startsWith("#") || line.trim().length() == 0) {
                continue;
            }

            String[] args = line.split("(\t| )");

            int objectId = Integer.parseInt(args[0]);
            int type = Integer.parseInt(args[1]);

            DoubleNodeRunnable<Player, GameObject> handler = null;

            switch (type) {
                case 0:
                    handler = LADDER_UP_HANDLER;
                    break;
                case 1:
                    handler = LADDER_DOWN_HANDLER;
                    break;
                case 2:
                    handler = SPIRAL_STAIR_UP_HANDLER;
                    break;
                case 3:
                    handler = SPIRAL_STAIR_DOWN_HANDLER;
                    break;
                case 4:
                    if (args.length > 2) {
                        int locChange = Integer.parseInt(args[2]);

                        handler = new SquareVarStairHandler(PlaneDirection.UP, locChange == 0 ? SquareVarStairHandler.LocationChange.X_CHANGE : SquareVarStairHandler.LocationChange.Y_CHANGE);
                    } else {
                        handler = VAR_STAIR_UP_HANDLER;
                    }
                    break;
                case 5:
                    if (args.length > 2) {
                        int locChange = Integer.parseInt(args[2]);

                        handler = new SquareVarStairHandler(PlaneDirection.DOWN, locChange == 0 ? SquareVarStairHandler.LocationChange.X_CHANGE : SquareVarStairHandler.LocationChange.Y_CHANGE);
                    } else {
                        handler = VAR_STAIR_DOWN_HANDLER;
                    }
                    break;
                case 6:
                    int x = Integer.parseInt(args[2]);
                    int y = Integer.parseInt(args[3]);
                    int z = Integer.parseInt(args[4]);

                    handler = new UniquePlaneObjectHandler(Tile.locate(x, y, z));
                    break;
                case 7:
                	int xLoc = Integer.parseInt(args[2]);
                	int yLoc = Integer.parseInt(args[3]);
                	int zLoc = Integer.parseInt(args[4]);
                	int xOffset = Integer.parseInt(args[5]);
                    int yOffset = Integer.parseInt(args[6]);
                    int zOffset = Integer.parseInt(args[7]); 
                    handler = new OffsetBasedPlaneObjectHandler(xLoc, yLoc, zLoc, xOffset, yOffset, zOffset);
                    break;
                case 8:
                	xOffset = Integer.parseInt(args[2]);
                	yOffset = Integer.parseInt(args[3]);
                	zOffset = Integer.parseInt(args[4]);
                	handler = new OffsetBasedPlaneObjectHandler2(xOffset, yOffset, zOffset);
                	break;
            }

            if (handler != null) {
                objectHandlerMap.put(objectId, handler);
            }

            lineNumber++;
        }

        system.registerObjectOptionHandler(ArrayUtilities.primitive(objectHandlerMap.keySet().toArray(new Integer[0])), this);

        logger.info("Loaded " + objectHandlerMap.size() + " plane object handler(s)");
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleObjectOption1(Player player, GameObject obj) {
        DoubleNodeRunnable<Player, GameObject> handler = objectHandlerMap.get(obj.getId());
        if (handler != null) {
            handler.run(player, obj);
        }
    }

    @Override
    public void handleObjectOption2(Player player, GameObject obj) {
    }

    @Override
    public void handleObjectOption3(Player player, GameObject obj) {
    }
}
