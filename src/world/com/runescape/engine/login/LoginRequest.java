package com.runescape.engine.login;

import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class LoginRequest {
    public IoSession session = null;
    public String name = null;
    public String password = null;
    public int opcode = 0;
    public long time = 0;
}
