package com.runescape.adapter;

import com.runescape.engine.login.LoginResponse;
import com.runescape.engine.registration.RegistrationResponse;

/**
 * @author Lazaro
 */
public interface ClientConfiguration {
	
    public int getClientVersion();

    public int getLoginResponseCode(LoginResponse resp);
    
    public int getRegistrationResponseCode(RegistrationResponse resp);

    public RegistrationResponse getRegistrationResponseForCode(int code);
    
    public LoginResponse getLoginResponseForCode(int code);

    public int[] getFrameLengths();
}
