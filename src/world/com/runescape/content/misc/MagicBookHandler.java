package com.runescape.content.misc;

import com.runescape.Static;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ButtonHandler;
import com.runescape.logic.player.Player;
import com.runescape.utility.Attributes;

/**
 * @author 'Mystic flow
 */
public class MagicBookHandler implements ButtonHandler {

    //XXX sortId might be wrong !

    public static boolean refreshModern(Player player) {
        Attributes atr = player.getAttributes();
        int sortId = atr.get("sortLevel", true) ? 0 : atr.get("sortCombat", false) ? 1 : 2;
        Static.proto.sendConfig(player, 1376, (atr.get("showSkill", true) ? 0 : 2) << 9 | (atr.get("showTeleport", true) ? 0 : 2) << 11 | (atr.get("showMisc", true) ? 0 : 2) << 10 | (atr.get("showCombat", true) ? 0 : 2) << 8 | sortId);
        return true;
    }

    public static boolean refreshAncient(Player player) {
        Attributes atr = player.getAttributes();
        int sortId = atr.get("sortLevel", true) ? 0 : atr.get("sortCombat", false) ? 1 : 2;
        Static.proto.sendConfig(player, 1376, 1 << 9 | (atr.get("showCombat", true) ? 0 : 2) << 15 | (atr.get("showTeleport", true) ? 0 : 2) << 16 | sortId << 3);
        return true;
    }

    public static boolean refreshLunar(Player player) {
        Attributes atr = player.getAttributes();
        int sortId = atr.get("sortLevel", true) ? 0 : atr.get("sortCombat", false) ? 1 : 2;
        Static.proto.sendConfig(player, 1376, 2 << 9 | (atr.get("showCombat", true) ? 0 : 2) << 12 | (atr.get("showMisc", true) ? 0 : 2) << 13 | (atr.get("showTeleport", true) ? 0 : 2) << 14 | sortId << 6);
        return true;
    }

    @Override
    public void handleButton(Player player, int opcode, int interfaceId, int buttonId, int b2, int b3) {
        if (!handle(player, interfaceId, buttonId)) {
            System.out.println("hi " + buttonId);
        }
    }

    public boolean handle(Player player, int interfaceId, int buttonId) {
        boolean ancient = interfaceId == 193;
        boolean lunar = interfaceId == 430;
        boolean modern = interfaceId == 192;
        Attributes atr = player.getAttributes();
        if (modern && buttonId == 15 || ancient && buttonId == 9 || lunar && buttonId == 11) {
            if (atr.get("sortLevel", true)) {
                return true;
            }
            set(true, false, false, atr);
            return ancient ? refreshAncient(player) : modern ? refreshModern(player) : refreshLunar(player);
        } else if (modern && buttonId == 16 || ancient && buttonId == 10 || lunar && buttonId == 12) {
            if (atr.get("sortCombat", false)) {
                return true;
            }
            set(false, true, false, atr);
            return ancient ? refreshAncient(player) : modern ? refreshModern(player) : refreshLunar(player);
        } else if (modern && buttonId == 17 || ancient && buttonId == 11 || lunar && buttonId == 13) {
            if (atr.get("sortTeleport", false)) {
                return true;
            }
            set(false, false, true, atr);
            return ancient ? refreshAncient(player) : modern ? refreshModern(player) : refreshLunar(player);
        } else if (modern && buttonId == 7 || (lunar || ancient) && buttonId == 5) {
            atr.set("showCombat", !atr.get("showCombat", true));
            return ancient ? refreshAncient(player) : modern ? refreshModern(player) : refreshLunar(player);
        } else if (modern && buttonId == 9 || (lunar || ancient) && buttonId == 7) {
            atr.set("showTeleport", !atr.get("showTeleport", true));
            return ancient ? refreshAncient(player) : modern ? refreshModern(player) : refreshLunar(player);
        } else if (modern && buttonId == 11 || lunar && buttonId == 9) {
            atr.set("showMisc", !atr.get("showMisc", true));
            return modern ? refreshModern(player) : refreshLunar(player);
        } else if (modern && buttonId == 13) {
            atr.set("showSkill", !atr.get("showSkill", true));
            refreshModern(player);
        } else if (modern && buttonId == 2 || ancient && buttonId == 18 || lunar && buttonId == 20) {
            atr.set("defensiveCast", !atr.get("defensiveCast", false));
            Static.proto.sendConfig(player, 439, atr.get("defensiveCast", false) ? (ancient ? 257 : (lunar ? 258 : 256)) : (ancient ? 1 : (lunar ? 2 : 0)));
            return true;
        }
        return false;
    }

    public void set(boolean sortLevel, boolean sortCombat, boolean sortTeleport, Attributes atr) {
        atr.set("sortLevel", sortLevel);
        atr.set("sortCombat", sortCombat);
        atr.set("sortTeleport", sortTeleport);
    }

    @Override
    public boolean explicitlyForMembers() {
        return true;
    }

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        system.registerButtonHandler(new int[]{192, 193, 430}, this);
    }

}
