package com.runescape.content.handler;

import com.runescape.logic.player.Player;

public interface LogoutHandler extends ActionHandler {

    public void onLogout(Player player, boolean lobby);

}
