package com.runescape.content.skill.free.prayer;

import java.util.HashMap;
import java.util.Map;

import com.runescape.Static;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ButtonHandler;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.player.Levels;
import com.runescape.logic.player.Player;

/**
 * @author Seth Rogen
 *
 * This class will handle all bones on alter and clicking.
 */

public class BuryingBones implements ButtonHandler {
	
	/**
	 * Information on bones.
	 */
	private static Map<Short, PRAYER_ITEM> prayer = new HashMap<Short, PRAYER_ITEM>();
	
	/**
	 * Loads into array.
	 */
	static {
		for(PRAYER_ITEM b : PRAYER_ITEM.values())
			prayer.put(b.item, b);
	}
	
	/**
	 * Prayer items, ashes etc.
	 * @author Seth Rogen
	 *
	 */
	public enum PRAYER_ITEM {
		
		BONES(526, 4.5),
		
		WOLF_BONES(2859, 4.5),
		
		BURNT_BONES(528, 4.5),
		
		MONKEY_BONES(3183, 5), 
		
		BAT_BONES(530, 5.3), 
		
		BIG_BONES(532, 15),
		
		JOGRE_BONES(3125, 15),
		
		ZOGRE_BONES(4812, 22.5),
		
		SHAIKAHAN_BONES(3123, 25),
		
		BABY_DRAGON_BONES(534, 30),
		
		WYVERN_BONES(6812, 72),
		
		DRAGON_BONES(17676, 72),
		
		FAYRG_BONES(4830, 84),
		
		LAVA_DRAGON_BONES(-1, 85),
		
		RAURG_BONES(4832, 96),
		
		DAGANNOTH_BONES(6729, 125),
		
		OURG_BONES(4834, 140),
		
		FROST_DRAGON_BONES(18830, 180);
		
		
		PRAYER_ITEM(int id, double d) {
			this.item = (short) id;
			this.exp = d;
		}
		
		PRAYER_ITEM(int id, int xp, boolean ashes) {
			this.item = (short) id;
			this.exp = xp;
			this.ashes = (byte) (ashes ? 1 : 0);
		}

		/**
		 * @return the boneId
		 */
		public short getBoneId() {
			return item;
		}

		/**
		 * @return the exp
		 */
		public double getExp() {
			return exp;
		}

		/**
		 * @return the ashes
		 */
		public boolean isAshes() {
			return ashes == 1;
		}

		private short item;
		private byte ashes;
		private double exp;
	}
	

	@Override
	public void load(ActionHandlerSystem system) throws Exception {
		system.registerButtonHandler(new int[]{149}, this);
		
	}

	@Override
	public boolean explicitlyForMembers() {
		// Memebrs only bones here.
		return false;
	}

	@Override
	public void handleButton(final Player player, int opcode, int interfaceId, int b, int b2, int b3) {
	    switch(opcode) { 
	        case 1:
	            final PossesedItem item = player.getInventory().array()[b2];
	            if (item != null) {
	                if (item.getId() == b3) {
	                    PRAYER_ITEM bone = prayer.get((short) b3);
	                    player.getCombat().stop(false);
	                    if (bone != null) {
	                    	final double xp = bone.getExp();
	                    	player.doAnimation(827, 0);
	                        Static.proto.sendMessage(player, "You dig a hole in the ground...");
	                        player.registerTick(new Tick("event", 2, Tick.TickPolicy.STRICT) {
	                            @Override
	                            public boolean execute() {
	                                Static.proto.sendMessage(player, "You bury the bones.");
	                                player.getInventory().remove(item);
	                                player.getLevels().addXP(player.getLevels().PRAYER, xp);
	                                return false;
	                            }
	                        });
	                    } 
	                }
	            }
	            break;
	    }
	}


}
