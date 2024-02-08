package com.runescape.network;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.runescape.Static;
import com.runescape.utility.Logging;

public class StandardFrameDecoder extends CumulativeProtocolDecoder {
    private static final Logger logger = Logging.log();

    public static final StandardFrameDecoder INSTANCE = new StandardFrameDecoder();

    @Override
    protected boolean doDecode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
        if (buffer.hasRemaining()) {
            int opcode = buffer.get() & 0xff;
            int length = Static.clientConf.getFrameLengths()[opcode];
            switch (length) {
                case -1:
                    if (buffer.hasRemaining()) {
                        length = buffer.get() & 0xff;
                    } else {
                        buffer.rewind();
                        return false;
                    }
                    break;
                case -2:
                    if (buffer.remaining() >= 2) {
                        length = buffer.getShort() & 0xffff;
                    } else {
                        buffer.rewind();
                        return false;
                    }
                    break;
                case -3:
                    logger.warn("Size not set for frame opcode: " + opcode);
                    length = buffer.remaining();
                    break;
            }
            if (buffer.remaining() >= length) {
                byte[] frameBuffer = new byte[length];
                buffer.get(frameBuffer);

                output.write(new Frame(opcode, Frame.FrameType.RECIEVED, frameBuffer, length));
                return true;
            } else {
                buffer.rewind();
                return false;
            }
        } else {
            return false;
        }
    }
}
