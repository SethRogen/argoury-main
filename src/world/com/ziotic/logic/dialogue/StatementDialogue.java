package com.ziotic.logic.dialogue;

/**
 * @author Lazaro
 */
public interface StatementDialogue extends Dialogue {
    public static enum POV {
        PLAYER, NPC, NONE
    }

    /**
     * Gets the point of view of this dialogue.
     * <p/>
     * Whether the player is speaking it, or the NPC.
     *
     * @return The POV of this dialogue.
     */
    public POV pov();

    /**
     * Handles the click to continue button.
     *
     * @param conversation The conversation context.
     */
    public void handle(Conversation conversation);
}
