package com.runescape.logic.dialogue;

/**
 * @author Lazaro
 */
public interface OptionDialogue extends Dialogue {
    /**
     * Gets the title of the dialogue.
     *
     * @return The title of the dialogue.
     */
    public String title();

    /**
     * Handles and option.
     *
     * @param conversation The conversation context.
     * @param option       The option index corresponding to the text.
     */
    public void handle(Conversation conversation, int option);
}
