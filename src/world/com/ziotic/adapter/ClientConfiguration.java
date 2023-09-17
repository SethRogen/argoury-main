package com.ziotic.adapter;

import com.ziotic.engine.login.LoginResponse;

/**
 * @author Lazaro
 */
public interface ClientConfiguration {
    public int getClientVersion();

    public int getLoginResponseCode(LoginResponse resp);

    public LoginResponse getLoginResponseForCode(int code);

    public int[] getFrameLengths();
}
