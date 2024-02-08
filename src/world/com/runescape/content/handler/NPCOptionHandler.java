package com.runescape.content.handler;

import com.runescape.logic.npc.NPC;
import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public interface NPCOptionHandler extends ActionHandler {
    public void handleNPCOption2(Player player, NPC npc);

    public void handleNPCOption1(Player player, NPC npc);
}
