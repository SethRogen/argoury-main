package com.runescape.logic.utility;

import com.runescape.logic.Entity;
import com.runescape.logic.player.Player;
import com.runescape.network.Frame;

/**
 * @author Lazaro
 */
public interface EntityUpdater<T extends Entity> {
    public Frame doMaskBlock(Player owner, T entity);

    public void update(Player player);
}
