package com.ziotic.content.skill;

import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.map.Region;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.Player;

/**
 * @author Lazaro
 */
public abstract class HarvestingTick extends Tick {
    protected final Player player;
    protected final GameObject obj;

    public HarvestingTick(Player player, GameObject obj) {
        super("event", 1, Tick.TickPolicy.STRICT);
        super.setInterval(getInterval());
        this.player = player;
        this.obj = obj;
    }

    @Override
    public boolean execute() {
        if (!player.isConnected() || player.getPathProcessor().moving()) {
            stop(true);
            return false;
        }
        if (Region.getObject(obj.getLocation()) != obj) {
            stop();
            return false;
        }
        if (isPeriodicRewards()) {
            final double random = Math.random(), factor = getRewardFactor();
            if (random <= factor) {
                if (!reward()) {
                    stop(true);
                    return false;
                }
            }
        }
        if (shouldExpire()) {
            expire();
            if (!isPeriodicRewards()) {
                reward();
            }
            stop(true);
            return false;
        } else {
            player.doAnimation(getAnimation());
        }
        return true;
    }

    public abstract void init();

    public abstract int getInterval();

    public abstract int getSkill();

    public abstract double getExperience();

    public abstract int getAnimation();

    public abstract boolean isPeriodicRewards();

    public abstract PossesedItem getReward();

    public abstract double getRewardFactor();

    public abstract void onReward();

    public abstract boolean shouldExpire();

    public abstract void expire();

    @Override
    public void onStart() {
        init();
    }

    private boolean reward() {
        PossesedItem reward = getReward();
        if (reward == null || player.getInventory().add(reward.getId(), reward.getAmount())) {
            player.getLevels().addXP(getSkill(), getExperience());
            player.updateXPCounter();
            onReward();
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        stop(false);
    }

    public void stop(boolean forceResetMasks) {
        super.stop();
        stopped(forceResetMasks);
    }

    public abstract void stopped(boolean forceResetMasks);
}
