package com.runescape.content.misc;

import com.runescape.Static;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.map.PathProcessor;
import com.runescape.logic.player.Player;

/**
 * @author Lazaro
 */
public class RunEnergyRestore extends Tick {
    private Player player;

    public RunEnergyRestore(Player player) {
        super("run_energy_restore", 5);
        this.player = player;
    }

    @Override
    public boolean execute() {
        if (player.getHP() == 0 || player.isDead())
            return true;
        if (player.getRunningEnergy() < 100 && !(player.getPathProcessor().moving() && (player.isRunning() || player.getPathProcessor().getMoveSpeed() == PathProcessor.MOVE_SPEED_RUN))) {
            player.setRunningEnergy(player.getRunningEnergy() + 1);
            Static.proto.sendRunEnergy(player);
        }
        return true;
    }
}
