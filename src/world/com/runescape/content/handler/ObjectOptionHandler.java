package com.runescape.content.handler;

import com.runescape.logic.object.GameObject;
import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public interface ObjectOptionHandler extends ActionHandler {
    public void handleObjectOption1(Player player, GameObject obj);

    public void handleObjectOption2(Player player, GameObject obj);

    public void handleObjectOption3(Player player, GameObject obj);
}
