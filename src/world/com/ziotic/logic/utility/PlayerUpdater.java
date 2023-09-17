package com.ziotic.logic.utility;

import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;

/**
 * @author Lazaro
 */
public interface PlayerUpdater extends EntityUpdater<Player> {
    public Frame doApperanceBlock(Player player);

    public Player[] getPlayers();

    public int[] getPlayerLocations();
}
