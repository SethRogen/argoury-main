package com.runescape.content.combat.misc;

import java.util.HashMap;
import java.util.Iterator;

import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.LogoutHandler;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.player.Player;

public class SkullRegisterManager implements LogoutHandler {

    private Player player;

    private HashMap<Long, Player> victims = new HashMap<Long, Player>();
    private HashMap<Long, Player> attackedBy = new HashMap<Long, Player>();

    public SkullRegisterManager() {
    }

    public SkullRegisterManager(Player player) {
        this.player = player;
    }

    private void addVictim(Player victim) {
        victims.put(victim.getIdentifier(), victim);
        victim.skullRegisterManager.attackedBy.put(player.getIdentifier(), player);
    }

    public void onLogout() {
        Iterator<Player> it = victims.values().iterator();
        while (it.hasNext()) {
            Player player = it.next();
            if (player != null) {
                player.skullRegisterManager.attackedBy.remove(player.getIdentifier());
                player.skullRegisterManager.victims.remove(player.getIdentifier());
            }
        }
        victims.clear();
        attackedBy.clear();
    }

    public static void handleSkulling(final Player attacker, final Player victim) {
        if (shouldSkull(attacker, victim)) {
            Tick tick = new Tick("SkullTimer", 2000) {
                @Override
                public boolean execute() {
                    attacker.getItemsOnDeathManager().isSkulled(false);
                    attacker.getAppearance().setPKIcon(-1);
                    attacker.getAppearance().refresh();
                    return false;
                }
            };
            attacker.registerTick(tick);
            attacker.getAppearance().setPKIcon(0);
            attacker.getItemsOnDeathManager().isSkulled(true);
            attacker.getAppearance().refresh();
        }
    }

    private static boolean shouldSkull(Player attacker, Player victim) {
        SkullRegisterManager attackerR = attacker.skullRegisterManager;
        if (attackerR.attackedBy.containsKey(victim.getIdentifier())) {
            return false;
        } else if (attackerR.victims.containsKey(victim.getIdentifier())) {
            return false;
        } else {
            attackerR.addVictim(victim);
            return true;
        }
    }

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        system.registerLogoutHandler(this);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void onLogout(Player player, boolean lobby) {
        player.skullRegisterManager.onLogout();
    }

}
