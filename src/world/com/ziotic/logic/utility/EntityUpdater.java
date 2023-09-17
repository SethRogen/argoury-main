package com.ziotic.logic.utility;

import com.ziotic.logic.Entity;
import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;

/**
 * @author Lazaro
 */
public interface EntityUpdater<T extends Entity> {
    public Frame doMaskBlock(Player owner, T entity);

    public void update(Player player);
}
