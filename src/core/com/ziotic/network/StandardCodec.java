package com.ziotic.network;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class StandardCodec implements ProtocolCodecFactory {
    public static final ProtocolCodecFilter FILTER = new ProtocolCodecFilter(new StandardCodec());

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return StandardFrameEncoder.INSTANCE;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return StandardFrameDecoder.INSTANCE;
    }
}
