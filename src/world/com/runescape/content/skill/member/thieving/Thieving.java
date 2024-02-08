package com.runescape.content.skill.member.thieving;

import com.runescape.Static;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.NPCOptionHandler;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.map.PathProcessor;
import com.runescape.logic.mask.Splat;
import com.runescape.logic.mask.SplatNode;
import com.runescape.logic.mask.Splat.SplatCause;
import com.runescape.logic.npc.NPC;
import com.runescape.logic.player.Levels;
import com.runescape.logic.player.Player;

import java.util.HashMap;
import java.util.Random;

public class Thieving implements NPCOptionHandler {

    private static Random random = new Random();

    public enum Robbed {

        MAN(new int[]{1, 2, 3, 16}, "man", 422, 8D, 3, 1, 5, 10,
                995, 3),
        WOMAN(new int[]{4, 5, 6}, "woman", 422, 8D, 3, 1, 5, 10,
                995, 3),
        FARMER(new int[]{7, 1377}, "farmer", 386, 14.5D, 3, 10, 5, 10,
                995, 9, 5318, 3),
        HAM(new int[]{4327}, "ham member", 386, 18.5D, 3, 15, 5, 30,
                995, 15, 590, 1, 1512, 3),
        WARRIOR(new int[]{15, 18}, "warrior", 386, 26D, 3, 25, 5, 20,
                995, 18),
        ROUGE(new int[]{187, 2267, 2268, 2269}, "rouge", 386, 35.5D, 3, 32, 5, 20,
                995, 40, 995, 40, 995, 40, 995, 40, 556, 20),
        MASTER_FARMER(new int[]{2234, 2235}, "master farmer", 386, 43D, 3, 38, 5, 30,
                5318, 3, 5319, 2, 5324, 4, 5322, 2, 5320, 3, 5323, 2, 5321, 1, 5297, 1, 5298, 1, 5299, 1, 5300, 1),
        GAURD(new int[]{32, 9, 206, 2699, 296, 297, 298, 299, 344, 368}, "guard", 386, 46.5D, 3, 40, 5, 20,
                995, 30),
        KNIGHT(new int[]{23, 26}, "knight", 386, 84.3D, 3, 55, 5, 30,
                995, 50),
        PALADIN(new int[]{2256, 365, 20}, "paladin", 386, 151.75D, 3, 70, 5, 30,
                995, 80);

        public int[] getIds() {
            return npcIds;
        }

        public double getXp() {
            return xp;
        }

        public double getFactor() {
            return factor;
        }

        public int getReq() {
            return req;
        }

        public int[] getPlunder() {
            return plunder;
        }

        public int getStunTime() {
            return stunTime;
        }

        public int getStunDamage() {
            return stunDamage;
        }

        public String getName() {
            return name;
        }

        public int getAttackAnim() {
            return attackAnim;
        }

        private Robbed(int[] npcIds, String name, int attackAnim, double xp, double factor, int req, int stunTime, int stunDamage, int... plunder) {
            this.npcIds = npcIds;
            this.name = name;
            this.xp = xp;
            this.factor = factor;
            this.attackAnim = attackAnim;
            this.req = req;
            this.plunder = plunder;
            this.stunTime = stunTime;
            this.stunDamage = stunDamage;
        }

        private final int[] npcIds;
        private final double xp;
        private final int attackAnim;
        private final double factor;
        private final String name;
        private final int req;
        private final int stunTime;
        private final int stunDamage;
        private final int[] plunder;

        public static final HashMap<Integer, Robbed> ROBBED = new HashMap<Integer, Robbed>();

        static {
            for (Robbed robbed : values()) {
                for (int id : robbed.getIds()) {
                    ROBBED.put(id, robbed);
                }
            }
        }

    }

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        system.registerNPCOptionHandler((Integer[]) Robbed.ROBBED.keySet().toArray(new Integer[0]), this);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleNPCOption2(final Player player, final NPC npc) {
        final Robbed robbed = Robbed.ROBBED.get(npc.getId());
        if (player.getPathProcessor().getMoveSpeed() == PathProcessor.MOVE_SPEED_WALK) {
            return;
        }
        if (player.getLevels().getLevel(Levels.THIEVING) < robbed.getReq()) {
            Static.proto.sendMessage(player, "You need a thieving level of " + robbed.getReq() + ".");
            return;
        }
        player.doAnimation(881);
        player.getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_WALK);
        Static.proto.sendMessage(player, "You attempt to pick the " + robbed.getName().toLowerCase() + "'s pocket.");
        player.registerTick(new Tick("attempt", 1, Tick.TickPolicy.STRICT) {
            @Override
            public boolean execute() {
                if (!sucess(player, robbed)) {
                    player.doAnimation(15074);
                    player.doGraphics(245, 0, 100);
                    player.resetFaceDirection();
                    player.getMasks().submitSplat(new SplatNode(new Splat(player, npc, random.nextInt(robbed.getStunDamage()) + 1, Splat.SplatType.DAMAGE, SplatCause.NONE, false, (int) 0)));
                    player.registerTick(new Tick(null, robbed.getStunTime(), Tick.TickPolicy.STRICT) {
                        @Override
                        public boolean execute() {
                            player.getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_ANY);
                            player.resetCurrentGraphics();
                            return false;
                        }
                    });
                    Static.proto.sendMessage(player, "You've failed to pick the " + robbed.getName().toLowerCase() + "'s pocket.");
                    Static.proto.sendMessage(player, "You've been stunned.");
                    npc.doForceChat("What do you think you're doing?");
                    npc.faceEntity(player);
                    npc.doAnimation(robbed.getAttackAnim());
                } else {
                    Static.proto.sendMessage(player, "You pick the " + robbed.getName().toLowerCase() + "'s pocket.");
                    player.getPathProcessor().setMoveSpeed(PathProcessor.MOVE_SPEED_ANY);
                    player.getLevels().addXP(Levels.THIEVING, robbed.getXp());
                    player.updateXPCounter();
                    int reward = random.nextInt(robbed.getPlunder().length / 2);
                    player.getInventory().add(robbed.getPlunder()[reward], robbed.getPlunder()[reward + 1]);
                }
                return false;
            }
        });
        npc.registerTick(new Tick(null, 2, Tick.TickPolicy.STRICT) {
            @Override
            public boolean execute() {
                npc.resetFaceDirection();
                return false;
            }
        });
    }

    public boolean sucess(Player player, Robbed robbed) {
        int def = Math.max(robbed.getReq() - player.getLevels().getLevel(Levels.THIEVING), 0);
        return def == 0 ? random.nextInt(6) != 0 : random.nextInt((int) ((double) def * .7D)) != 0;
    }

    @Override
    public void handleNPCOption1(Player player, NPC npc) {
    }

}
