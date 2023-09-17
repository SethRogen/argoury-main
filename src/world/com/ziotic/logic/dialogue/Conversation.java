package com.ziotic.logic.dialogue;

import com.ziotic.Static;
import com.ziotic.logic.dialogue.StatementDialogue.POV;
import com.ziotic.logic.npc.NPC;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class Conversation {
    private static final Logger logger = Logging.log();

    private static Map<String, Map<Integer, Dialogue>> cachedDialogues = new HashMap<String, Map<Integer, Dialogue>>();

    public static Map<Integer, Dialogue> loadDialogue(String name) {
        synchronized (cachedDialogues) {
            Map<Integer, Dialogue> dialogue = cachedDialogues.get(name);
            if (dialogue == null) {
                Map<?, Dialogue> dialogue2 = Static.callScript(name + ".dialogue");
                dialogue = new HashMap<Integer, Dialogue>();
                for (Map.Entry<?, Dialogue> entry : dialogue2.entrySet())
                    dialogue.put(((Number) entry.getKey()).intValue(), entry.getValue());
                cachedDialogues.put(name, dialogue);
            }
            return dialogue;
        }
    }

    private Map<Integer, Dialogue> dialogues;

    private Player player;
    private NPC npc;

    private int stage = -1;
    private Dialogue currentDialogue = null;

    /**
     * Sets up a conversation between a player and an NPC.
     *
     * @param dialogues The dialogue templates to use.
     * @param player    The player.
     * @param npc       The NPC.
     */
    public Conversation(Map<Integer, Dialogue> dialogues, Player player, NPC npc) {
        this.dialogues = dialogues;
        this.player = player;
        this.npc = npc;
    }

    /**
     * Initiates the conversation at stage #0
     */
    public Conversation init() {
        player.setCurrentConversation(this);

        if (npc != null) {
            player.faceEntity(npc);
            npc.faceEntity(player);
            npc.startedConversation(this);
        }

        stage(0);
        return this;
    }

    /**
     * Gets the player in this conversation.
     *
     * @return The player in this conversation.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the NPC in this conversation.
     *
     * @return The NPC in this conversation.
     */
    public NPC getNPC() {
        return npc;
    }

    /**
     * Gets the current stage in the conversation.
     *
     * @return The current stage in the conversation.
     */
    public int getStage() {
        return stage;
    }

    /**
     * Sets and shows the next stage in the conversation.
     *
     * @param stage The next stage.
     */
    public void stage(int stage) {
        this.stage = stage;
        if (stage == -1) {
            player.setCurrentConversation(null);
            if (npc != null)
                npc.endedConversation(this);
            Static.proto.sendCloseChatboxInterface(player);
        } else {
            Dialogue dialogue = this.currentDialogue = dialogues.get(stage);
            if (dialogue != null) {
                if (dialogue instanceof StatementDialogue) {
                    sendStatementDialogue((StatementDialogue) dialogue);
                } else if (dialogue instanceof OptionDialogue) {
                    sendOptionDialogue((OptionDialogue) dialogue);
                }
            } else {
                logger.warn("No dialogue for stage [npc=" + npc == null ? -1 : npc.getId() + ", stage=" + stage + "]");
            }
        }
    }

    private void sendStatementDialogue(StatementDialogue dialogue) {
        if (npc == null && dialogue.pov() == POV.NPC) {
            throw new IllegalStateException("Cannot send an NPC statement dialogue in a one-way conversation!");
        }

        String[] text = dialogue.text(this);

        int interfaceId = dialogue.pov() == POV.NONE ? 740/* TODO: Find correct id */ : ((dialogue.pov() == POV.NPC ? 240 : 63) + text.length);
        int childIdOffset = dialogue.pov() == POV.NONE ? 1 : 4;

        Static.proto.sendChatboxInterface(player, interfaceId);

        for (int i = 0; i < text.length; i++) {
            Static.proto.sendString(player, interfaceId, childIdOffset + i, text[i]);
        }

        if (dialogue.pov() != POV.NONE) {
            Static.proto.sendString(player, interfaceId, 3, dialogue.pov() == POV.NPC ? npc.getDefinition().name : player.getName());
            if (dialogue.pov() == POV.NPC) {
                Static.proto.sendNPCHead(player, interfaceId, 2, npc.getId());
            } else {
                Static.proto.sendPlayerHead(player, interfaceId, 2);
            }
            Static.proto.sendInterfaceAnimation(player, interfaceId, 2, 9827);
        } else {
            if (text.length < 2) {
                for (int i = text.length; i < 2; i++) {
                    Static.proto.sendString(player, interfaceId, childIdOffset + i, "");
                }
            }
        }
    }

    private void sendOptionDialogue(OptionDialogue dialogue) {
        String[] text = dialogue.text(this);

        int interfaceId = 224 + (text.length * 2);
        int childIdOffset = 2;

        Static.proto.sendChatboxInterface(player, interfaceId);

        Static.proto.sendString(player, interfaceId, 1, dialogue.title());
        for (int i = 0; i < text.length; i++) {
            Static.proto.sendString(player, interfaceId, childIdOffset + i, text[i]);
        }
    }

    /**
     * Ends the conversation.
     */
    public void end() {
        stage(-1);
    }

    /**
     * Handles a button/option being clicked.
     *
     * @param option The option index relative to the text. (Can be -1 for no option)
     */
    public void handle(int option) {
        if (currentDialogue instanceof StatementDialogue) {
            ((StatementDialogue) currentDialogue).handle(this);
        } else {
            ((OptionDialogue) currentDialogue).handle(this, option);
        }
    }
}
