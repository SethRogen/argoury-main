/**
 *
 */
package com.ziotic.content.tutorial;

import com.ziotic.Static;
import com.ziotic.logic.dialogue.Conversation;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.player.Player;

/**
 * @author Lazaro
 */
public class Tutorial {
    public static final int RODDECK_ID = 8863;

    private static NPC roddeck = null;

    public static void onFirstLogin(Player player) {

        if (roddeck == null || !roddeck.isValid() || roddeck.isDestroyed()) {
            roddeck = Static.world.searchForNPCs(RODDECK_ID)[0];
        }
        new Conversation(Conversation.loadDialogue("npc/dialogue/tutorial"), player, roddeck).init();
    }
}
