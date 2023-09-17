package com.ziotic.content.misc;

import com.ziotic.Static;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;

/**
 * @author Lazaro
 */
public class LevelNormalize extends Tick {
    private Player player;

    public LevelNormalize(Player player) {
        super("level_normalize", 100);
        this.player = player;
    }

    @Override
    public boolean execute() {
        if (player.getHP() == 0 || player.isDead())
            return true;
        for (int i = 0; i < Levels.SKILL_COUNT; i++) {
            int level = player.getLevels().getCurrentLevel(i);
            if (level > player.getLevels().getLevel(i)) {
                level--;
                player.getLevels().setCurrentLevel(i, level);
                Static.proto.sendLevel(player, i);
            }
        }
        return true;
    }
}
