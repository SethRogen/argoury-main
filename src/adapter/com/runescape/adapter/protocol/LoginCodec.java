package com.runescape.adapter.protocol;

import com.runescape.Static;
import com.runescape.engine.login.LoginRequest;
import com.runescape.engine.login.LoginResponse;
import com.runescape.network.StandardCodec;
import com.runescape.network.StandardFrameDecoder;
import com.runescape.network.StandardFrameEncoder;
import com.runescape.network.handler.GameFrameDispatcher;
import com.runescape.utility.Logging;
import com.runescape.utility.Streams;
import com.runescape.utility.Text;
import com.runescape.utility.crypt.XTEA;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

/**
 * @author Lazaro
 * 
 * @author Seth Rogen
 */
public class LoginCodec extends CumulativeProtocolDecoder implements ProtocolCodecFactory {
    private static final Logger logger = Logging.log();

    public static final ProtocolCodecFilter FILTER = new ProtocolCodecFilter(new LoginCodec());

    @Override
    protected boolean doDecode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
        if (buffer.remaining() >= 3) {
            int opcode = buffer.get() & 0xff;
            int size = buffer.getShort() & 0xffff;
            LoginResponse resp = LoginResponse.LOGIN;
            LoginRequest req = new LoginRequest();
            req.session = session;
            req.opcode = opcode;
            login:
            do {
                if (buffer.remaining() >= size) {
                    int clientRevision = buffer.getInt();
                    if (clientRevision != Static.clientConf.getClientVersion()) {
                        resp = LoginResponse.CLIENT_UPDATED;
                        break login;
                    }
                    int rsaBlockSize, rsaHeaderKey;
                    String name, password;
                    int[] xteaKey;
                    byte[] xteaBlock;
                    XTEA xtea;
                    IoBuffer buffer2;
                    switch (opcode) {
                        case 16: // Normal login
                        case 18: // Reconnection login
                            if (Static.isGame()) {
                                rsaBlockSize = buffer.getShort(); // RSA block size
                                rsaHeaderKey = buffer.get(); // RSA header key
                                if (rsaHeaderKey != 10) {
                                    logger.warn("Incorrect RSA header key: " + rsaHeaderKey);
                                }
                                xteaKey = new int[4];
                                for (int i = 0; i < xteaKey.length; i++) {
                                    xteaKey[i] = buffer.getInt();
                                }
                                buffer.getLong(); // Hard-coded 0
                                password = Streams.readString(buffer);
                                buffer.getLong(); // ??
                                buffer.getLong(); // ??
                                xteaBlock = new byte[size - rsaBlockSize - 10]; // - 6
                                buffer.get(xteaBlock);
                                xtea = new XTEA(xteaKey);
                                xtea.decrypt(xteaBlock, 0, xteaBlock.length);
                                buffer2 = IoBuffer.wrap(xteaBlock);
                                name = Streams.readString(buffer2);
                                buffer2.get();
                                buffer2.get();
                                buffer2.getShort();
                                buffer2.getShort();
                                buffer2.get();
                                for (int i = 0; i < 24; i++) {
                                    buffer2.get();
                                }
                                Streams.readString(buffer2);
                                buffer2.getInt();
                                buffer2.skip(buffer2.get() & 0xff);
                                // TODO more data that i cbf to read
                                req.name = Text.formatNameForProtocol(name);
                                req.password = password;
                                req.time = System.currentTimeMillis();
                            } else {
                                resp = LoginResponse.ERROR;
                            }
                            break;
                        case 19: // Lobby login
                            if (Static.isLobby()) {
                                rsaBlockSize = buffer.getShort(); // RSA block size
                                rsaHeaderKey = buffer.get(); // RSA header key
                                if (rsaHeaderKey != 10) {
                                    logger.warn("Incorrect RSA header key: " + rsaHeaderKey);
                                }
                                xteaKey = new int[4];
                                for (int i = 0; i < xteaKey.length; i++) {
                                    xteaKey[i] = buffer.getInt();
                                }
                                buffer.getLong(); // Hard-coded 0
                                password = Streams.readString(buffer);
                                buffer.getLong(); // ??
                                buffer.getLong(); // ??
                                xteaBlock = new byte[size - rsaBlockSize - 10];  // - 6
                                buffer.get(xteaBlock);
                                xtea = new XTEA(xteaKey);
                                xtea.decrypt(xteaBlock, 0, xteaBlock.length);
                                buffer2 = IoBuffer.wrap(xteaBlock);
                                name = Streams.readString(buffer2);
                                buffer2.get();
                                buffer2.get();
                                for (int i = 0; i < 24; i++) {
                                    buffer2.get();
                                }
                                Streams.readString(buffer2);
                                buffer2.getInt();
                                for (int i = 0; i < 34; i++) {
                                    buffer2.getInt();
                                }
                                req.name = Text.formatNameForProtocol(name);
                                req.password = password;
                                req.time = System.currentTimeMillis();
                            } else {
                                resp = LoginResponse.ERROR;
                            }
                            break;
                    }
                } else {
                    buffer.rewind();
                    return false;
                }
            } while (false);
            if (resp == LoginResponse.LOGIN) {
                session.getFilterChain().replace("codec", StandardCodec.FILTER);
                session.getFilterChain().addLast("dispatcher", GameFrameDispatcher.INSTANCE);
                Static.engine.getLoginEngine().submit(req);
                logger.debug("Dispatched login request [name=" + req.name + ", pass=" + req.password.replaceAll(".", "\\*") + "]");
            } else {
                if (opcode == 16 || opcode == 18) {
                    Static.proto.sendLoginResponse(session, null, resp);
                } else if (opcode == 19) {
                    Static.proto.sendLobbyResponse(session, null, resp);
                }
                logger.debug("Refused login request [name=" + req.name + ", response=" + resp.toString() + "]");
            }
        }
        return false;
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return StandardFrameEncoder.INSTANCE;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return this;
    }
}
