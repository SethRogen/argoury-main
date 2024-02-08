package com.runescape.adapter;

import com.runescape.content.cc.Clan;
import com.runescape.engine.login.LoginResponse;
import com.runescape.logic.player.PlayerSave;

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
