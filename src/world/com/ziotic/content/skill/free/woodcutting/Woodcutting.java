package com.ziotic.content.skill.free.woodcutting;

import com.ziotic.Static;
import com.ziotic.content.handler.ActionHandler;
import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.content.handler.ObjectOptionHandler;
import com.ziotic.content.skill.HarvestingTick;
import com.ziotic.engine.event.DelayedEvent;
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
public class Woodcutting implements ActionHandler, ObjectOptionHandler {
    private static Random random = new Random();

    public static enum Hatchet {
        ADAMANT(1357, 31, 869, 874, .3),
        BLACK(1361, 6, 873, 878, .2),
        BRONZE(1351, 1, 879, 12322, .1),
        DRAGON(6739, 61, 2846, 870, .45),
        IRON(1349, 1, 877, 2847, .15),
        MITHRIL(1355, 21, 871, 876, .25),
        RUNE(1359, 41, 867, 872, .35),
        STEEL(1353, 6, 875, 880, .18);

        private static Map<Integer, Hatchet> hatchets = new HashMap<Integer, Hatchet>();

        static {
            for (Hatchet hatchet : Hatchet.values()) {
                hatchets.put(hatchet.id, hatchet);
            }
        }

        public static Hatchet forId(int object) {
            return hatchets.get(object);
        }

        private int animation;
        private int ivyAnimation;
        private int id;
        private int level;
        private double rewardFactor;

        private Hatchet(int id, int level, int animation, int ivyAnimation, double rewardFactor) {
            this.id = id;
            this.level = level;
            this.animation = animation;
            this.ivyAnimation = ivyAnimation;
            this.rewardFactor = rewardFactor;
        }

        public int getAnimation() {
            return animation;
        }

        public int getIvyAnimation() {
            return ivyAnimation;
        }

        public int getId() {
            return id;
        }

        public int getRequiredLevel() {
            return level;
        }

        public double getRewardFactor() {
            return rewardFactor;
        }
    }

    public static enum Tree {
        ACHEY(2862, 1, 25, 1342, 30, 60, new int[]{2023}),                                         // TODO Stump id
        MAGIC(1513, 75, 250, 7401, 120, 190, new int[]{1292, 1306}),
        MAHOGANY(6332, 50, 125, 1342, 10, 10, new int[]{9034}),    // TODO stump id
        MAPLE(1517, 45, 100, 7400, 35, 60, new int[]{1307, 4677}),
        NORMAL(1511, 1, 25, 1342, 30, 60, new int[]{1276, 1277, 1278, 1279, 1280, 1282, 1283, 1284, 1285, 1286, 1289, 1290, 1291, 1315, 1316, 1318, 1319, 1330, 1331, 1332, 1365, 1383, 1384, 2409, 3033, 3034, 3035, 3036, 3881, 3882, 3883, 5902, 5903, 5904}),
        OAK(1521, 15, 37.5, 1356, 8, 13, new int[]{1281, 3037}),
        TEAK(6333, 35, 85, 1342, 10, 10, new int[]{9036}),   // TODO stump id
        WILLOW(1519, 30, 70, 5554, 8, 13, new int[]{1308, 5551, 5552, 5553}),
        YEW(1515, 60, 175, 7402, 60, 97, new int[]{1309}),

        IVY(-1, 68, 332.5, 46319, 40, 40, new int[]{46324, 46320, 46318, 46322});

        private static Map<Integer, Tree> trees = new HashMap<Integer, Tree>();

        static {
            for (Tree tree : Tree.values()) {
                for (int object : tree.objects) {
                    trees.put(object, tree);
                }
            }
        }

        public static Tree forId(int object) {
            return trees.get(object);
        }

        private double experience;
        private int level;
        private int reward;
        private int expiredId;
        private int minSpawnTime;
        private int maxSpawnTime;
        private int[] objects;

        private Tree(int reward, int level, double experience, int expiredId, int minSpawnTime, int maxSpawnTime, int[] objects) {
            this.objects = objects;
            this.level = level;
            this.experience = experience;
            this.expiredId = expiredId;
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

        public int getExpiredId() {
            return expiredId;
        }

        public int getMinSpawnTime() {
            return minSpawnTime;
        }

        public int getMaxSpawnTime() {
            return maxSpawnTime;
        }
    }

    public static class WoodcuttingTick extends HarvestingTick {
        private Hatchet hatchet;
        private Tree tree;
        private PossesedItem reward = null;

        public WoodcuttingTick(Player player, GameObject obj) {
            super(player, obj);
            this.tree = Tree.forId(obj.getId());
        }

        @Override
        public void init() {
            if (!obj.getAttributes().isSet("rewardsLeft")) {
                obj.getAttributes().set("rewardsLeft", tree != Tree.NORMAL ? Math.round(Math.random() * 10) : 1);
            }
            int level = player.getLevels().getCurrentLevel(Levels.WOODCUTTING);
            Hatchet bestHatchet = null;
            for (Hatchet hatchet : Hatchet.values()) {
                if (player.getEquipment().contains(hatchet.getId()) || player.getInventory().contains(hatchet.getId())) {
                    int hatchetReq = hatchet.getRequiredLevel();
                    if (level < hatchetReq)
                        continue;
                    if (bestHatchet == null || hatchetReq > bestHatchet.getRequiredLevel()) {
                        bestHatchet = hatchet;
                        this.hatchet = hatchet;
                    } else
                        continue;
                }
            }
            if (hatchet == null) {
                Static.proto.sendMessage(player, "You do not have a hatchet which you have the Woodcutting level to use.");
                stop();
                return;
            }
            if (level < tree.getRequiredLevel()) {
                Static.proto.sendMessage(player, "You need a Woodcutting level of " + tree.getRequiredLevel() + " to chop down this tree.");
                stop();
                return;
            }
            if (tree == Tree.IVY) {
                Static.proto.sendMessage(player, "You swing your hatchet at the ivy...");
            } else {
                Static.proto.sendMessage(player, "You swing your hatchet at the tree...");
            }
            player.doAnimation(getAnimation(), 10);
        }

        @Override
        public int getInterval() {
            return 3;
        }

        @Override
        public int getSkill() {
            return Levels.WOODCUTTING;
        }

        @Override
        public double getExperience() {
            return tree.getExperience();
        }

        @Override
        public int getAnimation() {
            if (tree == Tree.IVY) {
                return hatchet.getIvyAnimation();
            } else {
                return hatchet.getAnimation();
            }
        }

        @Override
        public boolean isPeriodicRewards() {
            return true;
        }

        @Override
        public PossesedItem getReward() {
            if (reward == null && tree.getRewardId() != -1) {
                reward = new PossesedItem(tree.getRewardId(), 1);
            }
            return reward;
        }

        @Override
        public double getRewardFactor() {
            double value = hatchet.getRewardFactor() + ((player.getLevels().getCurrentLevel(Levels.WOODCUTTING) - tree.getRequiredLevel()) * .01);
            if (value >= 1.0)
                value = .99;
            return value;
        }

        @Override
        public void onReward() {
            if (tree == Tree.IVY) {
                Static.proto.sendMessage(player, "You successfully chop away some ivy.");
            } else {
                Static.proto.sendMessage(player, "You get some " + getReward().getDefinition().name.toLowerCase() + ".");
            }
            obj.getAttributes().set("rewardsLeft", obj.getAttributes().getInt("rewardsLeft") - 1);
        }

        @Override
        public boolean shouldExpire() {
            return obj.getAttributes().getInt("rewardsLeft") == 0;
        }

        @Override
        public void expire() {
            final int origTreeId = obj.getId();
            final int origTreeType = obj.getType();
            final int origTreeDir = obj.getDirection();
            final Tile origTreeLoc = obj.getLocation();

            Static.world.getObjectManager().add(tree.getExpiredId(), origTreeLoc, origTreeType, origTreeDir);

            int additionalTime = tree.getMaxSpawnTime() - tree.getMinSpawnTime();
            additionalTime = additionalTime != 0 ? random.nextInt(additionalTime) : 0;
            int time = (tree.getMinSpawnTime() + additionalTime) * 1000;

            Static.engine.submit(new DelayedEvent(time) {
                @Override
                public void run() {
                    Static.world.getObjectManager().add(origTreeId, origTreeLoc, origTreeType, origTreeDir);
                }
            });
        }

        @Override
        public void stopped(boolean forceResetMasks) {
            if (forceResetMasks) {
                player.resetAnimation();
            }
        }
    }

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        for (Tree t : Tree.values()) {
            system.registerObjectOptionHandler(t.getObjectIds(), this);
        }
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleObjectOption1(Player player, GameObject obj) {
        player.registerTick(new WoodcuttingTick(player, obj));
    }

    @Override
    public void handleObjectOption2(Player player, GameObject obj) {
    }

    @Override
    public void handleObjectOption3(Player player, GameObject obj) {
    }
}
