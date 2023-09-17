package com.ziotic.logic.utility;

import com.ziotic.logic.player.DisplayMode;
import com.ziotic.logic.player.Player;

/**
 * @author Lazaro
 */
public class GameInterface {
    public static final int[] DEFAULT_POS = new int[]{18, 9};
    public static final int[] DEFAULT_INV_POS = new int[]{197, 84};

    public static final int getDefaultPos(DisplayMode displayMode) {
        return DEFAULT_POS[displayMode == DisplayMode.FIXED ? 0 : 1];
    }

    private NodeRunnable<Player> close;
    private int id;
    private int[] pos;
    private NodeRunnable<Player> show;
    private NodeRunnable<Player> update;
    private boolean isWalkable = false;

    public GameInterface(int id) {
        this(id, null);
    }

    public GameInterface(int id, NodeRunnable<Player> show) {
        this(id, null, show);
    }

    public GameInterface(int id, int[] pos, NodeRunnable<Player> show) {
        this(id, pos, show, null);
    }

    public GameInterface(int id, int[] pos, NodeRunnable<Player> show, NodeRunnable<Player> close) {
        this(id, pos, show, close, null);
    }

    public GameInterface(int id, int[] pos, NodeRunnable<Player> show, NodeRunnable<Player> close, boolean isWalkable) {
        this(id, pos, show, close, null);
        this.isWalkable = isWalkable;
    }

    public GameInterface(int id, int[] pos, NodeRunnable<Player> show, NodeRunnable<Player> close, NodeRunnable<Player> update) {
        this.id = id;
        this.pos = pos;
        this.show = show;
        this.close = close;
        this.update = update;

        if (this.pos == null) {
            this.pos = DEFAULT_POS;
        }
    }

    public int getId() {
        return id;
    }

    public int getPos(DisplayMode displayMode) {
        return pos[displayMode == DisplayMode.FIXED ? 0 : 1];
    }

    public void show(Player player) {
        if (show != null) {
            show.run(player);
        }
        update(player);
    }

    public void close(Player player) {
        if (close != null) {
            close.run(player);
        }
    }

    public void update(Player player) {
        if (update != null) {
            update.run(player);
        }
    }

    public void setWalkable(boolean isWalkable) {
        this.isWalkable = isWalkable;
    }

    public boolean isWalkable() {
        return isWalkable;
    }
}
