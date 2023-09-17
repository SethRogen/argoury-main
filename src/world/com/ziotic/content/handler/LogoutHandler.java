package com.ziotic.content.handler;

import com.ziotic.logic.player.Player;

public interface LogoutHandler extends ActionHandler {

    public void onLogout(Player player, boolean lobby);

}
