package com.ziotic.content.handler;

import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.player.Player;

/**
 * @author Lazaro
 */
public interface NPCOptionHandler extends ActionHandler {
    public void handleNPCOption2(Player player, NPC npc);

    public void handleNPCOption1(Player player, NPC npc);
}
