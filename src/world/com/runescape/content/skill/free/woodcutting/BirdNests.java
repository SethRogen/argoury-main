package com.runescape.content.skill.free.woodcutting;

import java.util.Random;

import com.runescape.Static;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ButtonHandler;
import com.runescape.logic.item.ConsumablesHandler;
import com.runescape.logic.item.PossesedItem;
import com.runescape.logic.player.Player;

/**
 * @author Seth Rogen
 */

public class BirdNests implements ButtonHandler {
	
	private static Random random = new Random();
	
    public static final int[][] SEEDS = { { 5312, 5283, 5284, 5313, 5285, 5286 }, { 5314, 5288, 5287, 5315, 5289 }, { 5316, 5290 }, { 5317 } };
    private static final int[] RINGS = { 1635, 1637, 1639, 1641, 1643 };

    public static boolean isNest(int id) {
	return id == 5070 || id == 5071 || id == 5072 || id == 5073 || id == 5074 || id == 7413 || id == 11966;
    }
    
    public static void searchNest(final Player player) {
    	Static.proto.sendMessage(player, "<col=FF0000>You search the nest and find nothing...");
        }

	@Override
	public void load(ActionHandlerSystem system) throws Exception {
		// TODO Auto-generated method stub
		 system.registerButtonHandler(new int[]{149}, this);
	}

	@Override
	public boolean explicitlyForMembers() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	 public void handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3) {
        switch (opcode) {
            case 1:
                PossesedItem item = player.getInventory().array()[b2];
                if (item != null) {
                	if (5070 == b3) {
                    	Static.proto.sendMessage(player, "<col=FF0000>You search the nest...");
                    }
                }
                break;
        }
    }
}
