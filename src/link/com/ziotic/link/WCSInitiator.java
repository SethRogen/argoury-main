package com.ziotic.link;

import com.ziotic.utility.Initiator;

/**
 * @author Lazaro
 */
public class WCSInitiator implements Initiator<WorldClientSession> {
    @Override
    public void init(WorldClientSession session) throws Exception {
        session.init();
    }
}
