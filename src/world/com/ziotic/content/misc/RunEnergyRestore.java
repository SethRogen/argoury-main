package com.ziotic.content.misc;

import com.ziotic.Static;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.map.PathProcessor;
import com.ziotic.logic.player.Player;

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
