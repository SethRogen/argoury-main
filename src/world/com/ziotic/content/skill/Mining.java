package com.ziotic.content.skill;

import com.ziotic.Static;
import com.ziotic.content.handler.ActionHandler;
import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.content.handler.ObjectOptionHandler;
import com.ziotic.engine.event.DelayedEvent;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Lazaro
 */
public class Mining implements ActionHandler, ObjectOptionHandler {
    private static Random random = new Random();

    public static enum Pickaxe {
        RUNE(1275, 41, 624),
        ADAMANT(1271, 31, 628),
        MITHRIL(1273, 21, 629),
        STEEL(1269, 11, 627),
        IRON(1267, 5, 626),
        BRONZE(1265, 1, 625);

        private static Map<Integer, Pickaxe> pickaxes = new HashMap<Integer, Pickaxe>();

        static {
            for (Pickaxe pickaxe : Pickaxe.values()) {
                pickaxes.put(pickaxe.id, pickaxe);
            }
        }

        public static Pickaxe forId(int object) {
            return pickaxes.get(object);
        }

        private int animation;
        private int id;
        private int level;

        private Pickaxe(int id, int level, int animation) {
            this.id = id;
            this.level = level;
            this.animation = animation;
        }

        public int getAnimation() {
            return animation;
        }

        public int getId() {
            return id;
        }

        public int getRequiredLevel() {
            return level;
        }
    }

    public static enum Rock {
        COPPER(436, 1, 17.5, 2, 4, new int[]{2090, 2091, 11962, 11960, 11961, 11936}),
        TIN(438, 1, 17.5, 2, 4, new int[]{2094, 2095, 11957, 11959, 11958}),
        BLURITE(668, 10, 17.5, 2, 4, new int[]{2110}), // TODO Respawn time
        IRON(440, 15, 35, 5, 10, new int[]{2092, 2093, 11956, 11954, 11955, 37307, 37309}),
        SILVER(442, 20, 40, 60, 120, new int[]{2100, 2101, 37304, 37305, 37306}),
        GOLD(444, 40, 65, 60, 120, new int[]{2098, 2099, 37310, 37312}),
        COAL(453, 30, 50, 30, 60, new int[]{2096, 2097, 11932, 11930}),
        MITHRIL(447, 55, 80, 120, 240, new int[]{2102, 2103, 11942, 11944}),
        ADAMANTITE(449, 70, 95, 240, 480, new int[]{2104, 2105, 11939, 11941}),
        RUNE(451, 85, 125, 738, 1500, new int[]{2106, 2107}),
        CLAY(434, 1, 5, 1, 2, new int[]{2108, 2109});

        private static Map<Integer, Rock> rocks = new HashMap<Integer, Rock>();

        static {
            for (Rock rock : Rock.values()) {
                for (int object : rock.objects) {
                    rocks.put(object, rock);
                }
            }
        }

        public static Rock forId(int object) {
            return rocks.get(object);
        }

        private double experience;
        private int level;
        private int reward;
        private double minSpawnTime;
        private double maxSpawnTime;
        private int[] objects;

        private Rock(int reward, int level, double experience, double minSpawnTime, double maxSpawnTime, int[] objects) {
            this.objects = objects;
            this.level = level;
            this.experience = experience;
            this.minSpawnTime = minSpawnTime;
            this.maxSpawnTime = maxSpawnTime;
            this.reward = reward;
        }

        public double getExperience() {
            return experience;
        }

        public int getRewardId() {
            return reward;
        }

        public int[] getObjectIds() {
            return objects;
        }

        public int getRequiredLevel() {
            return level;
        }

        public int getExpiredId(int rockId) {
            switch (rockId) {
                case 11957:
                    return 11555;
                case 11956:
                    return 11557;
                case 11954:
                    return 11555;
                case 11955:
                    return 11556;
                case 11962:
                    return 11557;
                case 11960:
                    return 11555;
                case 11959:
                    return 11557;
                case 11961:
                    return 11556;
                case 11958:
                    return 11556;
                case 37310:
                    return 11552;
                case 37312:
                    return 11554;
                case 37307:
                    return 11552;
                case 37309:
                    return 11554;
                case 37304:
                    return 11552;
                case 37305:
                    return 11553;
                case 37306:
                    return 11554;
                case 11932:
                    return 11554;
                case 11930:
                    return 11552;
                case 11942:
                    return 11552;
                case 11944:
                    return 11554;
                case 11939:
                    return 11552;
                case 11941:
                    return 11554;
                case 11936:
                    return 11552;
                default:
                    return 11555;
            }
        }

        public double getMinSpawnTime() {
            return minSpawnTime;
        }

        public double getMaxSpawnTime() {
            return maxSpawnTime;
        }
    }

    public static class MiningTick extends HarvestingTick {
        private Pickaxe pickaxe;
        private Rock rock;
        private PossesedItem reward = null;
        private int cycles = -1;

        public MiningTick(Player player, GameObject obj) {
            super(player, obj);
            this.rock = Rock.forId(obj.getId());
        }

        @Override
        public void init() {
            int level = player.getLevels().getCurrentLevel(Levels.MINING);
            Pickaxe bestPickaxe = null;
            for (Pickaxe pickaxe : Pickaxe.values()) {
                if (player.getEquipment().contains(pickaxe.getId()) || player.getInventory().contains(pickaxe.getId())) {
                    int pickaxeReq = pickaxe.getRequiredLevel();
                    if (level < pickaxeReq)
                        continue;
                    if (bestPickaxe == null || pickaxeReq > bestPickaxe.getRequiredLevel()) {
                        bestPickaxe = pickaxe;
                        this.pickaxe = pickaxe;
                    } else
                        continue;
                }
            }
            if (pickaxe == null) {
                Static.proto.sendMessage(player, "You do not have a pickaxe which you have the Mining level to use.");
                stop();
                return;
            }
            if (level < rock.getRequiredLevel()) {
                Static.proto.sendMessage(player, "You need a Mining level of " + rock.getRequiredLevel() + " to mine this rock.");
                stop();
                return;
            }
            Static.proto.sendMessage(player, "You swing your pick at the rock...");
            player.doAnimation(getAnimation(), 50);
            cycles = calculateCycles();
        }

        @Override
        public int getInterval() {
            return 1;
        }

        @Override
        public int getSkill() {
            return Levels.MINING;
        }

        @Override
        public double getExperience() {
            return rock.getExperience();
        }

        @Override
        public int getAnimation() {
            return pickaxe.getAnimation();
        }

        @Override
        public boolean isPeriodicRewards() {
            return false;
        }

        @Override
        public PossesedItem getReward() {
            if (reward == null && rock.getRewardId() != -1) {
                reward = new PossesedItem(rock.getRewardId(), 1);
            }
            return reward;
        }

        @Override
        public double getRewardFactor() {
            return 0;
        }

        @Override
        public void onReward() {
            Static.proto.sendMessage(player, "You manage to mine some " + rock.name().toLowerCase() + ".");
        }

        @Override
        public boolean shouldExpire() {
            return cycles-- == 0;
        }

        @Override
        public void expire() {
            final int origRockId = obj.getId();
            final int origRockType = obj.getType();
            final int origRockDir = obj.getDirection();
            final Tile origRockLoc = obj.getLocation();

            Static.world.getObjectManager().add(rock.getExpiredId(origRockId), origRockLoc, origRockType, origRockDir);

            int additionalTime = (int) (rock.getMaxSpawnTime() - rock.getMinSpawnTime());
            additionalTime = additionalTime != 0 ? random.nextInt(additionalTime) : 0;
            int time = (int) ((rock.getMinSpawnTime() + additionalTime) * 1000.0);

            Static.engine.submit(new DelayedEvent(time) {
                @Override
                public void run() {
                    Static.world.getObjectManager().add(origRockId, origRockLoc, origRockType, origRockDir);
                }
            });
        }

        @Override
        public void stopped(boolean forceResetMasks) {
            player.resetAnimation();
        }

        private int calculateCycles() {
            int mining = player.getLevels().getCurrentLevel(Levels.MINING);
            int difficulty = rock.getRequiredLevel();
            int pickModifier = pickaxe.getRequiredLevel();
            int randomModifier = random.nextInt(3);
            double cycleCount = 1;
            cycleCount = Math.ceil(((((difficulty * 64) - (mining * 20)) / pickModifier) * 0.25) - (randomModifier * 4));
            if (cycleCount < 1) {
                cycleCount = 1;
            }

            // Static.proto.sendMessage(player, "You have to wait " + cycleCount + " cycles till you finish mining this rock.");

            return (int) cycleCount;
        }
    }

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        for (Rock t : Rock.values()) {
            system.registerObjectOptionHandler(t.getObjectIds(), this);
        }
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleObjectOption1(Player player, GameObject obj) {
        player.registerTick(new MiningTick(player, obj));
    }

    @Override
    public void handleObjectOption2(final Player player, GameObject obj) {
        final Rock rock = Rock.forId(obj.getId());
        if (rock != null) {
            Static.proto.sendMessage(player, "You examine the rock for ores...");
            player.registerTick(new Tick("event", 4, Tick.TickPolicy.STRICT) {
                @Override
                public boolean execute() {
                    Static.proto.sendMessage(player, "This rock contains " + rock.name().toLowerCase() + ".");
                    return false;
                }
            });
        }
    }

    @Override
    public void handleObjectOption3(Player player, GameObject obj) {
    }
}
