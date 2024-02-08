package com.runescape.engine.registration;

import com.runescape.Static;

/**
 * @author Seth Rogen
 */

public enum RegistrationResponse {
	
	INVALID_DETAILS, EMAIL_IN_USE, IP_BANNED, REGISTRATION_COMPLETE, ERROR, SERVER_UPDATED;
	
	public static RegistrationResponse valueFor(int responseCode) {
	        return Static.clientConf.getRegistrationResponseForCode(responseCode);
	    }

	    public int intValue() {
	        return Static.clientConf.getRegistrationResponseCode(this);
	    }

	    @Override
	    public String toString() {
	        return "response=" + super.toString().replace("_", " ").toLowerCase();
	    }
}
