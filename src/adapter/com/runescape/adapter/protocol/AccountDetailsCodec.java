package com.runescape.adapter.protocol;

import com.runescape.Static;
import com.runescape.engine.login.LoginRequest;
import com.runescape.engine.login.LoginResponse;
import com.runescape.engine.registration.RegistrationResponse;
import com.runescape.network.StandardCodec;
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
 * @author Seth Rogen
 */
public class AccountDetailsCodec extends CumulativeProtocolDecoder implements ProtocolCodecFactory {
    private static final Logger logger = Logging.log();

    public static final ProtocolCodecFilter FILTER = new ProtocolCodecFilter(new AccountDetailsCodec());

    @Override
    protected boolean doDecode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
    	System.out.println("Here1");
        if (buffer.remaining() >= 3) {
            int opcode = buffer.get() & 0xff;
            int size = buffer.getShort() & 0xffff;
            RegistrationResponse resp = RegistrationResponse.REGISTRATION_COMPLETE;
            System.out.println("Here1");
            do {
                if (buffer.remaining() >= size) {
                    int clientRevision = buffer.getInt();
                    if (clientRevision != Static.clientConf.getClientVersion()) {
                        resp = RegistrationResponse.SERVER_UPDATED;
                        break;
                    }
                    int rsaBlockSize, rsaHeaderKey, affId, lang, game, uuid, additionalInfo, age;
                    String password, email;
                    boolean subscribe;
                    long userFlow;
                    int[] xteaKey;
                    byte[] xteaBlock;
                    XTEA xtea;
                    IoBuffer buffer2;
                    rsaBlockSize = buffer.getShort(); // RSA block size
                    rsaHeaderKey = buffer.get(); // RSA header key
                    if (rsaHeaderKey != 10) {
                        logger.warn("Incorrect RSA header key: " + rsaHeaderKey);
                    }
                    xteaKey = new int[4];
                    for (int i = 0; i < xteaKey.length; i++) {
                        xteaKey[i] = buffer.getInt();
                    }
                    xteaBlock = new byte[size - rsaBlockSize - 10]; // - 6
                    buffer.get(xteaBlock);
                    xtea = new XTEA(xteaKey);
                    xtea.decrypt(xteaBlock, 0, xteaBlock.length);
                    buffer2 = IoBuffer.wrap(xteaBlock);
                    
                    email = Streams.readString(buffer);
                    password = Streams.readString(buffer2);
                    lang = buffer2.get();
                    game = buffer2.get();
                    for (int i = 0; i < 24; i++) {
                        uuid = buffer2.get();
                    }
                    additionalInfo = buffer2.get();
                    age = buffer2.get();
                    subscribe = buffer2.get() == 1;
                } else {
                    buffer.rewind();
                    return false;
                }
            } while (false);
            if (resp == RegistrationResponse.REGISTRATION_COMPLETE) {
                session.getFilterChain().replace("codec", StandardCodec.FILTER);
                session.getFilterChain().addLast("dispatcher", GameFrameDispatcher.INSTANCE);
            } else { 
               // logger.debug("Refused Account Creation request [email =" + req.name + ", response =" + resp.toString() + "]");
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
