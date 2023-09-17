package com.ziotic.content.handler;

import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.Player;

/**
 * @author Lazaro
 */
public interface ObjectOptionHandler extends ActionHandler {
    public void handleObjectOption1(Player player, GameObject obj);

    public void handleObjectOption2(Player player, GameObject obj);

    public void handleObjectOption3(Player player, GameObject obj);
}
