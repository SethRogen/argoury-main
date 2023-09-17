package com.ziotic.network;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * @author Lazaro
 */
public class StandardFrameEncoder implements ProtocolEncoder {
    public static final StandardFrameEncoder INSTANCE = new StandardFrameEncoder();

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput output) throws Exception {
        if(message instanceof Frame) {
            Frame frame = (Frame) message;

            byte[] buffer = frame.getBuffer();
            if (frame.getLength() < buffer.length) {
                byte[] newBuffer = new byte[frame.getLength()];
                System.arraycopy(buffer, 0, newBuffer, 0, frame.getLength());
                buffer = newBuffer;
            }

            output.write(IoBuffer.wrap(buffer));
        }
    }

    @Override
    public void dispose(IoSession session) throws Exception {
    }
}