package com.ziotic.content.misc;

import com.ziotic.Static;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.utility.GameInterface;

public class SearchInterface {

    public static final void searchInterface(final Player player, final int startId, final int length, int interval) {
        player.registerTick(new Tick("SearchInterface", interval) {
            int id = startId;
            int max = startId + length;

            @Override
            public boolean execute() {
                Static.proto.sendInterface(player, new GameInterface(id++));
                if (id > max)
                    return false;
                return true;
            }
        });
    }

    public static final void searchInterface(final Player player, final int startId, final int length) {
        searchInterface(player, startId, length, 2);
    }

}
