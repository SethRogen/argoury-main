package com.runescape.content.skill;

import org.apache.log4j.Logger;

import com.runescape.Static;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ButtonHandler;
import com.runescape.logic.player.Player;
import com.runescape.utility.Logging;


/**
 * @author Seth Rogen
 */

public class SkillDialouge implements ButtonHandler {
	
	public static final int MAKE = 0, MAKE_SETS = 1, COOK = 2, ROAST = 3, OFFER = 4, SELL = 5, BAKE = 6, CUT = 7, DEPOSIT = 8, MAKE_NO_ALL_NO_CUSTOM = 9, TELEPORT = 10, SELECT = 11, TAKE = 13;

	public static interface ItemNameFilter {
		public String rename(String name);
	}
    
    public void Open(Player player, int option, String ChooseString, int[] items, int maxQuantity, String ItemName, ItemNameFilter filter) { 
    	Static.proto.sendInterface(player, 905, 752, 13, true); //ChatBox Interface ID
        Static.proto.sendInterface(player, 916, 905, 4, true);//Amount Interface ID
        Static.proto.sendString(player, 916, 1, ChooseString); //Choose String
        Static.proto.sendInterfaceVariable(player, 754, option); //Options
		if (option != MAKE_SETS && option != MAKE_NO_ALL_NO_CUSTOM) {
			Static.proto.sendAccessMask(player, -1, 0, 916, 8, 0, 0);
		}
        for (int i = 0; i < 10; i++) {
    	    if (i >= items.length) {
    	    	Static.proto.sendInterfaceVariable(player, i >= 6 ? (1139 + i - 6) : 755 + i, -1);
    	    	continue;
    	    }
    	    Static.proto.sendInterfaceVariable(player, i >= 6 ? (1139 + i - 6) : 755 + i, items[i]);
    	    Static.proto.sendSpecialString(player, i >= 6 ? (280 + i - 6) : 132 + i, ItemName);
        }
    }

	public static void setMaxQuantity(Player player, int maxQuantity) {
		player.getAttributes().set("SkillsDialogueMaxQuantity", maxQuantity);
		Static.proto.sendInterfaceVariable(player, 8094, maxQuantity);
	}

	public static void setQuantity(Player player, int quantity) {
		setQuantity(player, quantity, true);
	}

	public static void setQuantity(Player player, int quantity, boolean refresh) {
		int maxQuantity = getMaxQuantity(player);
		if (quantity > maxQuantity) {
			quantity = maxQuantity;
		} else if (quantity < 0) {
			quantity = 0;
		}
		player.getAttributes().set("SkillsDialogueQuantity", quantity);
		if (refresh) {
			 Static.proto.sendInterfaceVariable(player, 8095, quantity);
		}
	}

	public static int getMaxQuantity(Player player) {
		Integer maxQuantity = (Integer) player.getAttributes().get("SkillsDialogueMaxQuantity");
		if (maxQuantity == null) {
			return 0;
		}
		return maxQuantity;
	}

	public static int getQuantity(Player player) {
		Integer quantity = (Integer) player.getAttributes().get("SkillsDialogueQuantity");
		if (quantity == null) {
			return 0;
		}
		return quantity;
	}

	public static int getItemSlot(int componentId) {
		if (componentId < 14) {
			return 0;
		}
		return componentId - 14;
	}


	@Override
	public void load(ActionHandlerSystem system) throws Exception {
		system.registerButtonHandler(new int[]{916}, this);
		
	}

	@Override
	public boolean explicitlyForMembers() {
		return false;
	}
	private static final Logger logger = Logging.log();
	@Override
	public void handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3) {
	switch(interfaceId) { 
		case 916: 
			
			switch(b) { 
				case 5:
					setQuantity(player, 1, false);
				break;
					case 6: 
					setQuantity(player, 5, false);
				break;
					case 7:
					setQuantity(player, 10, false);
					break;
				case 8: 
					setQuantity(player, getMaxQuantity(player), false);
					break;
				case 19: 
					setQuantity(player, getQuantity(player) + 1, false);
					break;
				case 20:
					setQuantity(player, getQuantity(player) - 1, false);
					break;
				 default:
                     logger.debug("Unhandled button [interface=" + interfaceId + ", button=" + b + ", button2=" + b2 + ", button3=" + b3 + ", opcode=" + opcode + "]");
                     break;
			}
			break;
		}
	}
}
