package com.runescape.content.skill;

import com.runescape.Static;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.item.Item;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.player.Player;

/**
 * @author Seth Rogen
 */
public abstract class ProducingTick extends Tick {
	
    protected final Player player;
    
    
    public static final int NOT_ENOUGH_ITEMS = 0, SUCCESSFULLY_PRODUCED = 1, UNSUCCESSFULLY_PRODUCED = 2, TOO_LOW_OF_LEVEL = 3;
	private int produceDelay; //the delay before producing again

	private int cycles;
	private boolean firstCycle;

    public ProducingTick(Player player) {
        super("event", 1, Tick.TickPolicy.STRICT);
        super.setInterval(getInterval());
        this.player = player;
    }

    @Override
    public boolean execute() {
        if (!player.isConnected() || player.getPathProcessor().moving()) {
            stop(true);
            return false;
        }
        PossesedItem item = getRequiredItems();
			int amount = player.getInventory().amount(item.getId());
	        if (amount <= 0) {
	            stopProduction();
	            return firstCycle;
	        }
		
        PossesedItem producedItem = produceItem();
		boolean ableToProduce = true;

		if (getRequiredLevel() > player.getLevels().getLevel(getSkill())) {
			ableToProduce = false;
		}
			if (!player.getInventory().contains(item.getId())) {
				ableToProduce = false;
			}
		
		if (ableToProduce) {
			player.doAnimation(getAnimation());
		}
		
		if (produceDelay > 0) {
			produceDelay--;
			return ableToProduce;
		}
		
		produceDelay = cycles;
		PossesedItem Removeitem = getRequiredItems();
			if (amount-- > 0) {
				if (ableToProduce) {
					if (isSuccessfull()) {
							player.getInventory().remove(Removeitem);
						if (!player.getInventory().add(producedItem)) {
							stopProduction();
						}
						Static.proto.sendMessage(player, getMessage(ProducingTick.SUCCESSFULLY_PRODUCED));
						player.getLevels().addXP(getSkill(), getExperience());
					} else {
							player.getInventory().remove(Removeitem);
						if (getFailItem() != null) {
							player.getInventory().add(getFailItem());
						}
						Static.proto.sendMessage(player, getMessage(ProducingTick.UNSUCCESSFULLY_PRODUCED));
					}
				} else if (getRequiredLevel() > player.getLevels().getLevel(getSkill())) {
					stopProduction();
					Static.proto.sendMessage(player, getMessage(ProducingTick.TOO_LOW_OF_LEVEL));
				} else {
					stopProduction();
					Static.proto.sendMessage(player, getMessage(ProducingTick.NOT_ENOUGH_ITEMS));
				}

			} else {
				stopProduction();
			}
        return true;
    }
    
    public abstract int getInterval();

    @Override
    public void onStart() {
    	
    }

    @Override
    public void stop() {
        stop(false);
    }
    
	public void setProduceDelay(int delay) {
		this.interval = delay;
	}

    
	public void stopProduction() {
		 super.stop();
	}
    
    
    public void stop(boolean forceResetMasks) {
        super.stop();
        stopped(forceResetMasks);
    }

    public abstract void stopped(boolean forceResetMasks);
    
    public abstract int getAnimationDelay();

	public abstract PossesedItem getFailItem();

	public abstract PossesedItem produceItem();

	public abstract PossesedItem getRequiredItems();

	public abstract int getRequiredLevel();

	public abstract int getSkill();

	public abstract double getExperience();

	public abstract String getMessage(int type);

	public abstract int getAnimation();
	
	public abstract int getGraphic();

	public abstract boolean isSuccessfull();
}
