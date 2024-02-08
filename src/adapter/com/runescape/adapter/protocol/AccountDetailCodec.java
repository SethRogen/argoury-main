package com.runescape.adapter.protocol;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;

import com.runescape.Static;
import com.runescape.engine.login.LoginResponse;
import com.runescape.network.FrameBuilder;
import com.runescape.utility.Logging;
import com.runescape.utility.Streams;
import com.runescape.utility.crypt.XTEA;

/**
 * @author Seth Rogen
 */


public class AccountDetailCodec {
	private static final Logger logger = Logging.log();
	public static void decodeDetails(IoSession session, IoBuffer buffer) { 
        int rsaBlockSize, rsaHeaderKey, affId, lang, game, uuid,  additionalInfo, age;
        String password, email;
        boolean subscribe;
        int[] xteaKey;
        byte[] xteaBlock;
        long userFlow;
        XTEA xtea;
        IoBuffer buffer2;
        
        	int size = buffer.getShort() & 0xffff; //PacketSize
			int clientRevision = buffer.getShort();
            if (clientRevision != Static.clientConf.getClientVersion()) {
            	//
            }
            rsaBlockSize = buffer.getShort() & 0xffff;; // RSA block size
            if (rsaBlockSize > buffer.remaining()) {
            	return;
            }
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
            affId = buffer2.getShort();
            password = Streams.readString(buffer2);
            userFlow = buffer2.getLong();
            lang = buffer2.get();
            game = buffer2.get();
            for (int i = 0; i < 3; i++) {
                uuid = buffer2.get();
            }
            additionalInfo = buffer2.get();
            age = buffer2.get();
            subscribe = buffer2.get() == 1;
            session.write(new FrameBuilder(1).writeByte(2).toFrame()).addListener(IoFutureListener.CLOSE);
            
		}
}
