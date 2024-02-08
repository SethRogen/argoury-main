package com.runescape.adapter.protocol;

import com.runescape.Static;
import com.runescape.engine.login.LoginRequest;
import com.runescape.engine.login.LoginResponse;
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
public class AccountCompleteCodec extends CumulativeProtocolDecoder implements ProtocolCodecFactory {
    private static final Logger logger = Logging.log();

    public static final ProtocolCodecFilter FILTER = new ProtocolCodecFilter(new AccountCompleteCodec());

    @Override
    protected boolean doDecode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
        if (buffer.remaining() >= 3) {
            int opcode = buffer.get() & 0xff;
            int size = buffer.getShort() & 0xffff;
            LoginResponse resp = LoginResponse.LOGIN;
            do {
                if (buffer.remaining() >= size) {
                    int clientRevision = buffer.getInt();
                    if (clientRevision != Static.clientConf.getClientVersion()) {
                        resp = LoginResponse.CLIENT_UPDATED;
                        break;
                    }
                    int rsaBlockSize, rsaHeaderKey, lang;
                    String email;
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
                    lang = buffer2.get();
                    System.out.println("Account Email: " + email);
                    System.out.println("Account Lang: " + lang);
                } else {
                    buffer.rewind();
                    return false;
                }
            } while (false);
            if (resp == LoginResponse.LOGIN) {
                session.getFilterChain().replace("codec", StandardCodec.FILTER);
                session.getFilterChain().addLast("dispatcher", GameFrameDispatcher.INSTANCE);
            } else {
                //logger.debug("Refused Account Creation request [email =" + email + ", response =" + resp.toString() + "]");
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
