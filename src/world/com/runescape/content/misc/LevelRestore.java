package com.runescape.content.misc;

import com.runescape.Static;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.player.Levels;
import com.runescape.logic.player.Player;

/**
 * Every minute, if any skill's level is less than the level for experience, it
 * ups the level.
 *
 * @author Lazaro
 */
public class LevelRestore extends Tick {
    private Player player;

    public LevelRestore(Player player) {
        super("level_restore", 100);
        this.player = player;
    }


    @Override
    public boolean execute() {
        if (player.getHP() == 0 || player.isDead())
            return true;
        for (int i = 0; i < Levels.SKILL_COUNT; i++) {
            if (i != Levels.PRAYER && i != Levels.SUMMONING) {
                int level = player.getLevels().getCurrentLevel(i);
                if (level < player.getLevels().getLevel(i)) {
                    level++;
                    player.getLevels().setCurrentLevel(i, level);
                    Static.proto.sendLevel(player, i);
                }
            }
        }
        return true;
    }
}