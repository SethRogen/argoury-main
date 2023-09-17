package com.ziotic.adapter;

import com.ziotic.content.cc.Clan;
import com.ziotic.engine.login.LoginResponse;
import com.ziotic.logic.player.PlayerSave;

/**
 * @author Lazaro
 */
public interface DatabaseLoader {
    public void banIP(String ip);

    public boolean isBanned(String userName);

    public boolean isIPBanned(String ip);

    public LoginResponse loadPlayer(String userName, String password, PlayerSave player);

    public void savePlayer(String userName, boolean lobby, PlayerSave player);

    public void registerClan(String owner, String name);

    public boolean loadClan(String owner, Clan clan);

    public void saveClan(Clan clan);

    public void reload();
}
