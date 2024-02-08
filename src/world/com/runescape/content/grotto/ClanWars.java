package com.runescape.content.grotto;

import com.runescape.Static;
import com.runescape.content.grotto.War.Stage;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ButtonHandler;
import com.runescape.content.handler.LogoutHandler;
import com.runescape.logic.player.Player;
import com.runescape.logic.utility.GameInterfaces;
import com.runescape.utility.Text;

public class ClanWars implements ButtonHandler, LogoutHandler {

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        system.registerButtonHandler(new int[]{791}, this);
        system.registerLogoutHandler(this);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleButton(Player player, int opcode, int interfaceId, int button, int button2, int button3) {
        War war = player.getOwnedWar();
        if (war != null && war.getStage() == Stage.SETUP) {
            war.onClick(button);
        }
        Player other = (Player) Static.world.findPlayer(player.getClan().getVersing().getOwner());
        Static.proto.sendConfig(player, 1305, war.calculate());
        Static.proto.sendConfig(other, 1305, war.calculate());
    }

    public static void initateChallange(Player player, Player other) {
        if (!valid(player, other)) {
            return;
        }
        player.getClan().setVersing(other.getClan());
        if (player.getClan().getVersing() == other.getClan() && other.getClan().getVersing() == player.getClan()) {
            init(player, other, new War());
            return;
        }
        Static.proto.sendMessage(player, "Sending challenge request...");
        Static.proto.sendMessage(other, "wishes to challenge your clan to a clan war!", Text.optimizeText(player.getClanOwner()), 101);
    }

    @Override
    public void onLogout(Player player, boolean lobby) {
        War war = player.getOwnedWar();
        if (war != null && war.getStage() == Stage.SETUP) {
            end(player);
        }
    }

    private static boolean valid(Player player, Player other) {
        if (player.getClanOwner() == null || !player.getClanOwner().equalsIgnoreCase(player.getName())) {
            Static.proto.sendMessage(player, "You are not in a clan in which you are the owner!");
            return false;
        }
        if (other.getClanOwner() == null || !other.getClanOwner().equalsIgnoreCase(other.getName())) {
            Static.proto.sendMessage(player, "That person is not the owner of a clan.");
            return false;
        }
        return true;
    }

    public static void end(Player player) {
        if (player.getClan().getVersing() != null) {
            Player other = (Player) Static.world.findPlayer(player.getClan().getVersing().getOwner());
            if (other != null) {
                other.getClan().setVersing(null);
                other.getClan().setWar(null);
                Static.proto.sendCloseInterface(other, GameInterfaces.SETUP_SCREEN);
                Static.proto.sendCloseInterface(other, GameInterfaces.SETUP_SCREEN_INVENTORY);
            }
        }
        player.getClan().setVersing(null);
        player.getClan().setWar(null);
    }

    private static void init(Player player, Player other, War war) {
        player.getClan().setWar(war);
        other.getClan().setWar(war);
        Static.proto.sendInterface(player, GameInterfaces.SETUP_SCREEN);
        Static.proto.sendInterface(other, GameInterfaces.SETUP_SCREEN);
    }
}
