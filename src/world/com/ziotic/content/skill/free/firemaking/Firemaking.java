package com.ziotic.content.skill.free.firemaking;

import com.ziotic.Static;
import com.ziotic.content.handler.ActionHandler;
import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.content.handler.GroundItemOptionHandler;
import com.ziotic.content.handler.ItemOnItemHandler;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.item.GroundItem;
import com.ziotic.logic.item.Item;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.map.Directions;
import com.ziotic.logic.map.PathProcessor;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class Firemaking implements ActionHandler, ItemOnItemHandler, GroundItemOptionHandler {

    public static enum Medium {
        NORMAL_LOGS(1511, 1, 40, Fire.NORMAL),
        ACHEY_LOGS(2862, 1, 40, Fire.NORMAL),
        OAK_LOGS(1521, 15, 60, Fire.NORMAL),
        WILLOW_LOGS(1519, 30, 90, Fire.NORMAL),
        TEAK_LOGS(6333, 35, 105, Fire.NORMAL),
        ARTIC_PINE_LOGS(10810, 42, 125, Fire.NORMAL),
        MAPLE_LOGS(1517, 45, 135, Fire.NORMAL),
        MAHOGANY(6332, 50, 157.5, Fire.NORMAL),
        EUCALYPTUS(12581, 58, 193.5, Fire.NORMAL),
        YEW_LOGS(1515, 60, 202.5, Fire.NORMAL),
        MAGIC_LOGS(1513, 75, 303.8, Fire.NORMAL),
        CURSED_MAGIC_LOGS(13567, 82, 303.8, Fire.NORMAL),
        RED_LOGS(7404, 1, 50, Fire.RED),
        GREEN_LOGS(7405, 1, 50, Fire.GREEN),
        BLUE_LOGS(7406, 1, 50, Fire.BLUE),
        PURPLE_LOGS(10329, 1, 50, Fire.PURPLE),
        WHITE_LOGS(10328, 1, 50, Fire.WHITE);

        private static Map<Integer, Medium> mediums = new HashMap<Integer, Medium>();

        static {
            for (Medium medium : Medium.values()) {
                mediums.put(medium.id, medium);
            }
        }

        public static Medium forId(int id) {
            return mediums.get(id);
        }

        private int id;
        private int level;
        private double experience;
        private Fire fire;

        private Medium(int id, int level, double experience, Fire fire) {
            this.id = id;
            this.level = level;
            this.experience = experience;
            this.fire = fire;
        }

        public int getId() {
            return id;
        }

        public int getLevel() {
            return level;
        }

        public double getExperience() {
            return experience;
        }

        public Fire getFire() {
            return fire;
        }
    }

    public static enum Lighter {
        TINDERBOX(590);

        private int id;
        private int level;

        private static Map<Integer, Lighter> lighters = new HashMap<Integer, Lighter>();

        static {
            for (Lighter lighter : Lighter.values()) {
                lighters.put(lighter.id, lighter);
            }
        }

        public static Lighter forId(int id) {
            return lighters.get(id);
        }

        private Lighter(int id) {
            this(id, 1);
        }

        private Lighter(int id, int level) {
            this.id = id;
            this.level = level;
        }

        public int getId() {
            return id;
        }

        public int getLevel() {
            return level;
        }
    }


    public static enum Firelighter {
        RED(7329, Medium.RED_LOGS),
        GREEN(7330, Medium.GREEN_LOGS),
        BLUE(7331, Medium.BLUE_LOGS),
        PURPLE(10326, Medium.PURPLE_LOGS),
        WHITE(10327, Medium.WHITE_LOGS);

        private static Map<Integer, Firelighter> firelighters = new HashMap<Integer, Firelighter>();

        static {
            for (Firelighter firelighter : Firelighter.values()) {
                firelighters.put(firelighter.id, firelighter);
            }
        }

        public static Firelighter forId(int id) {
            return firelighters.get(id);
        }

        private int id;
        private Medium convert;

        private Firelighter(int id, Medium convert) {
            this.id = id;
            this.convert = convert;
        }


        public int getId() {
            return id;
        }

        public Medium getConvert() {
            return convert;
        }
    }

    public static enum Fire {
        NORMAL(2732),
        RED(11404),
        GREEN(11405),
        BLUE(11406),
        PURPLE(20001),
        WHITE(20000);

        private static Map<Integer, Fire> fires = new HashMap<Integer, Fire>();

        static {
            for (Fire fire : Fire.values()) {
                fires.put(fire.id, fire);
            }
        }

        public static Fire forId(int id) {
            return fires.get(id);
        }

        private int id;

        private Fire(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public static final PossesedItem ASHES = new PossesedItem(592);

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        int[] ids = new int[Lighter.values().length + Firelighter.values().length + Medium.values().length];
        for (int i = 0; i < Lighter.values().length; i++) {
            ids[i] = Lighter.values()[i].getId();
        }
        for (int i = 0; i < Firelighter.values().length; i++) {
            ids[i + Lighter.values().length] = Firelighter.values()[i].getId();
        }
        for (int i = 0; i < Medium.values().length; i++) {
            ids[i + Lighter.values().length + Firelighter.values().length] = Medium.values()[i].getId();
        }
        system.registerItemOnItemHandler(ids, this);

        ids = new int[Medium.values().length];
        for (int i = 0; i < Medium.values().length; i++) {
            ids[i] = Medium.values()[i].getId();
        }
        system.registerGroundItemOptionHandler(ids, this);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleItemOnItem(Player player, Item item1, int index1, Item item2, int index2) {
        Lighter lighter = null;
        Firelighter firelighter = null;

        Item toolItem;
        int toolIndex;

        Medium medium;
        Item mediumItem;
        int mediumIndex;

        if ((lighter = Lighter.forId(item1.getId())) != null || (firelighter = Firelighter.forId(item1.getId())) != null) {
            toolItem = item1;
            toolIndex = index1;
            if ((medium = Medium.forId(item2.getId())) == null) {
                Static.proto.sendMessage(player, "Nothing interesting happens.");
                return;
            }
            mediumItem = item2;
            mediumIndex = index2;
        } else if ((medium = Medium.forId(item1.getId())) != null) {
            mediumItem = item1;
            mediumIndex = index1;
            if ((lighter = Lighter.forId(item2.getId())) == null) {
                if ((firelighter = Firelighter.forId(item2.getId())) == null) {
                    Static.proto.sendMessage(player, "Nothing interesting happens.");
                    return;
                }
            }
            toolItem = item2;
            toolIndex = index2;
        } else {
            Static.proto.sendMessage(player, "Nothing interesting happens.");
            return;
        }

        if (firelighter != null) {
            if (medium != Medium.NORMAL_LOGS) {
                Static.proto.sendMessage(player, "Nothing interesting happens.");
                return;
            }

            if (player.getInventory().remove((PossesedItem) toolItem, toolIndex, false) && player.getInventory().remove((PossesedItem) mediumItem, mediumIndex, false)) {
                player.getInventory().add(firelighter.getConvert().getId(), 1, mediumIndex);
                Static.proto.sendMessage(player, "You cover the log in the strange goo..it changes colour.");
            }
        } else {
            switch (medium.getFire()) {
                default:
                    lightLogs(player, lighter, medium, mediumItem, mediumIndex);
                    break;
            }
        }
    }


    @Override
    public void handleGroundItemOption(Player player, GroundItem item) {
        Medium medium = Medium.forId(item.getId());

        Lighter lighter = null;
        for (Lighter l : Lighter.values()) {
            if (player.getInventory().contains(l.getId())) {
                lighter = l;
                break;
            }
        }
        if (lighter == null) {
            Static.proto.sendMessage(player, "You need a tinderbox to light a fire.");
            return;
        }

        lightLogs(player, lighter, medium, item, -1);
    }

    private void lightLogs(final Player player, Lighter lighter,
                           final Medium medium,
                           Item mediumItem, int mediumIndex) {

        if (player.getLocation().getSpawnedObject() != null) {
            Static.proto.sendMessage(player, "You cannot light a fire here.");
            return;
        }

        final Tile src = player.getLocation();
        final Tile dst;

        if (src.canMove(Directions.NormalDirection.WEST, player.getSize(), false)) {
            dst = src.translate(-1, 0, 0);
        } else if (src.canMove(Directions.NormalDirection.EAST, player.getSize(), false)) {
            dst = src.translate(1, 0, 0);
        } else if (src.canMove(Directions.NormalDirection.SOUTH, player.getSize(), false)) {
            dst = src.translate(0, -1, 0);
        } else if (src.canMove(Directions.NormalDirection.NORTH, player.getSize(), false)) {
            dst = src.translate(0, 1, 0);
        } else {
            Static.proto.sendMessage(player, "You cannot light a fire here.");
            return;
        }

        do {
            if (mediumItem instanceof PossesedItem) {
                player.getInventory().remove(medium.getId(), 1, mediumIndex);
            } else {
                GroundItem gItem = (GroundItem) mediumItem;
                if (gItem.getOwner() != null) {
                    break;
                }
                Static.world.getGroundItemManager().remove((GroundItem) mediumItem);
            }
            Static.world.getGroundItemManager().add(medium.getId(), 1, src, player.getProtocolName(),
                    false);
        } while (false);

        player.doAnimation(733);
        Static.proto.sendMessage(player, "You attempt to light the logs.");

        final int delay = lightDelay(player, medium);
        Tick tick = new Tick("event", delay, Tick.TickPolicy.STRICT) {
            @Override
            public boolean execute() {
                player.resetAnimation();

                player.getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_WALK);
                player.getPathProcessor().add(dst);

                Tick tick2 = new Tick() {
                    @Override
                    public boolean execute() {
                        GroundItem item = Static.world.getGroundItemManager().get(medium.getId(), src);

                        Static.world.getGroundItemManager().remove(item);
                        Static.world.getObjectManager().add(medium.getFire().getId(), src, 10, 0);

                        player.faceDirection(src);
                        Static.proto.sendMessage(player, "The fire catches and the logs begin to burn.");
                        player.getLevels().addXP(Levels.FIREMAKING, medium.getExperience());

                        player.getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_ANY);

                        Tick tick3 = new Tick(null, fireTime(player, medium)) {
                            @Override
                            public boolean execute() {
                                Static.world.getObjectManager().remove(src);

                                Static.world.getGroundItemManager().add(ASHES.getId(), 1, src, null,
                                        false);
                                return false;
                            }
                        };
                        player.registerTick(tick3);
                        return false;
                    }
                };
                player.registerTick(tick2);
                return false;
            }
        };
        player.registerTick(tick);
    }

    private int lightDelay(Player player, Medium medium) {
        return 4; // TODO
    }

    private int fireTime(Player player, Medium medium) {
        return 100 + Static.random.nextInt(100);
    }
}
