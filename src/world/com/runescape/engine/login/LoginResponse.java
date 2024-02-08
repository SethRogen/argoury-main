package com.runescape.engine.login;

import com.runescape.Static;

/**
 * @author Lazaro
 */
public enum LoginResponse {
    ALREADY_ONLINE, BANNED, CLIENT_UPDATED, CURRENTLY_UPDATING, ERROR, INVALID_DETAILS, IP_BANNED, LOGIN, LOGIN_LIMIT_EXCEEDED, LOGIN_SERVER_OFFLINE, WORLD_FULL, MEMBERS_REQUIRED;

    public static LoginResponse valueFor(int responseCode) {
        return Static.clientConf.getLoginResponseForCode(responseCode);
    }

    public int intValue() {
        return Static.clientConf.getLoginResponseCode(this);
    }

    @Override
    public String toString() {
        return "response=" + super.toString().replace("_", " ").toLowerCase();
    }
}
