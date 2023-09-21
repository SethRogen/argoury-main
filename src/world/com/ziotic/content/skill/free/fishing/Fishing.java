package com.ziotic.content.skill.free.fishing;

import com.ziotic.content.handler.ActionHandlerSystem;
import com.ziotic.content.handler.NPCOptionHandler;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.item.ItemDefinition;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author 'Mystic Flow
 */
public class Fishing implements NPCOptionHandler {

    public static FishingHarvest fishingAction(Player player, NPC npc, int option) {
        FishingSpot spot = FishingSpot.forId(npc.getId() | option << 24);
        if (spot != null) {
            return new FishingHarvest(player, spot);
        }
        return null;
    }

    private static class FishingHarvest extends Tick {

        private static final Random RANDOM = new Random();

        private Player player;

        private boolean initiated;
        private final FishingSpot spot;

        private int harvestIndex;
        private int ticks = -1;

        public FishingHarvest(Player player, FishingSpot spot) {
            super("fishing_tick", 1, TickPolicy.STRICT);
            this.player = player;
            this.spot = spot;
        }

        @Override
        public boolean execute() {
            if (!initiated) {
                initiated = true;
                return init();
            }
            if (player.getInventory().remaining() < 1) {
                player.sendMessage("You don't have enough space in your inventory.");
                return false;
            }
            player.doAnimation(spot.getFishAnimation());
            if (ticks == -1) {
                harvestIndex = randomHarvest();
                ticks = nextDelay(harvestIndex);
            } else if (ticks > 0) {
                ticks--;
                if (ticks == 0) {
                    ticks = -1;
                    PossesedItem harvest = new PossesedItem(spot.getFishies()[harvestIndex].getId(), 1);
                    String name = harvest.getDefinition().name.toLowerCase();
                    if (name.endsWith("s")) {
                        name = "some " + name;
                    } else {
                        name = "a " + name;
                    }
                    player.sendMessage("You catch " + name + ".");
                    player.getInventory().add(harvest);
                    player.getLevels().addXP(Levels.FISHING, spot.getFishies()[harvestIndex].getXp());
                    if (spot.getBait() > 0) {
                        player.getInventory().remove(spot.getBait());
                        if (!player.getInventory().contains(spot.getBait())) {
                            stop();
                            player.sendMessage("You have run out of bait.");
                            player.doAnimation(-1);
                            return true;
                        }
                    }
                }
            }
            return true;
        }
        
        @Override
        public void onStop() {
        	player.doAnimation(-1);
        	player.getMasks().setFaceEntity(-1);
        }

        private boolean init() {
            if (player.getLevels().getLevel(Levels.FISHING) < spot.getFishies()[0].getLevel()) {
                player.sendMessage("You need a Fishing level of " + spot.getFishies()[0].getLevel() + ".");
                return false;
            }
            if (!player.getInventory().contains(spot.getItem())) {
                player.sendMessage("You need a " + ItemDefinition.forId(spot.getItem()).name.toLowerCase() + " to fish here.");
                return false;
            }
            if (!player.getInventory().contains(spot.getBait()) && spot.getBait() > 0) {
                player.sendMessage("You don't have the required bait to fish here.");
                return false;
            }
            switch (spot) {
                case NET_NET_AND_BAIT:
                case BIG_NET_NET_AND_HARPOON:
                    player.sendMessage("You cast out your net...");
                    break;
                case BAIT_NET_AND_BAIT:
                case LURE_LURE_AND_BAIT:
                case BAIT_LURE_AND_BAIT:
                    player.sendMessage("You cast out your line...");
                    break;
                case CAGE_CAGE_AND_HARPOON:
                    player.sendMessage("You attempt to catch a lobster...");
                    break;
                case HARPOON_CAGE_AND_HARPOON:
                case HARPOON_NET_AND_HARPOON:
                    player.sendMessage("You start harpooning fish...");
                    break;
            }
            return true;
        }

        private int nextDelay(int fishIndex) {
            int skill = player.getLevels().getLevel(Levels.FISHING);
            int level = spot.getFishies()[fishIndex].getLevel();
            int modifier = spot.getFishies()[fishIndex].getLevel();
            int randomAmt = RANDOM.nextInt(4);
            double cycleCount = Math.ceil((level * 50 - skill * 10) / modifier * 0.25 - randomAmt * 4);
            if (cycleCount < 1) {
                cycleCount = 1;
            }
            return (int) cycleCount + 1;
        }

        private int randomHarvest() {
            int randomHarvest = RANDOM.nextInt(spot.getFishies().length);
            int difference = player.getLevels().getLevel(Levels.FISHING) - spot.getFishies()[randomHarvest].getLevel();
            if (difference < -1) {
                return randomHarvest = 0;
            }
            if (randomHarvest < -1) {
                return randomHarvest = 0;
            }
            return randomHarvest;
        }

    }

    @Override
    public void handleNPCOption1(Player player, NPC npc) {
        FishingHarvest harvest = fishingAction(player, npc, 1);
        if (harvest != null && harvest.execute()) {
        	npc.getPathProcessor().hookEntity(player);
            player.registerTick(harvest);
        }
    }

    @Override
    public void handleNPCOption2(Player player, NPC npc) {
        FishingHarvest harvest = fishingAction(player, npc, 2);
        if (harvest != null && harvest.execute()) {
        	npc.getPathProcessor().hookEntity(player);
            player.registerTick(harvest);
        }
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        List<Integer> fishies = new ArrayList<Integer>();
        for (FishingSpot spot : FishingSpot.values()) {
            if (!fishies.contains(spot.getNpcId())) {
                fishies.add(spot.getNpcId());
            }
        }
        system.registerNPCOptionHandler(fishies.toArray(new Integer[0]), this);
    }

}
