package com.ziotic.adapter.protocol;

import com.ziotic.Static;
import com.ziotic.adapter.protocol.cache.RS2CacheFileAdapter;
import com.ziotic.io.rs2cache.RS2CacheFile;
import com.ziotic.network.Frame;
import com.ziotic.network.FrameBuilder;
import com.ziotic.network.StandardFrameEncoder;
import com.ziotic.utility.Logging;
import com.ziotic.utility.Streams;
import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Lazaro
 */
public class JS5Codec extends CumulativeProtocolDecoder implements ProtocolCodecFactory {
    private static final Logger logger = Logging.log();

    public static final ProtocolCodecFilter FILTER = new ProtocolCodecFilter(new JS5Codec());

    private Frame prepareFileFrame(int opcode, RS2CacheFile file) {
        byte[] data = file.getData();
        int compression = data[0] & 0xff;
        int attributes = compression;
        if (opcode == 0) {
            attributes |= 0x80;
        }
        int length = Streams.readInt(1, data);
        FrameBuilder fb = new FrameBuilder(32 + data.length + (data.length / 512)).writeByte(file.getIndexId()).writeShort(file.getId()).writeByte(attributes).writeInt(length);
        if (compression != RS2CacheFileAdapter.COMPRESSION_NONE) {
            length += 4;
        }
        int blockOffset = 8;
        for (int offset = 5; offset < length + 5; offset++) {
            if (blockOffset == 512) {
                fb.writeByte(255);
                blockOffset = 1;
            }
            fb.write(data[offset]);
            blockOffset++;
        }
        return fb.toFrame();
    }

    @Override
    protected boolean doDecode(final IoSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
        final Queue<int[]> requests = new LinkedList<int[]>();
        while (buffer.remaining() >= 4) {
            final int opcode = buffer.get() & 0xff;
            final int index = buffer.get() & 0xff;
            final int file = buffer.getShort() & 0xffff;
            switch (opcode) {
                case 0:
                    requests.add(new int[]{index, file});
                    break;
                case 1:
                    Runnable r = new Runnable() {
                        public void run() {
                            try {
                                Frame fileFrame = prepareFileFrame(1, Static.rs2Cache.getFile(index, file));
                                if (fileFrame != null) {
                                    session.write(fileFrame);
                                }
                            } catch (Exception e) {
                                logger.error("Error handling file [opcode=" + opcode + ", index=" + index + ", file=" + file + "]", e);
                            }
                        }
                    };
                    if (index == 255 && file == 255) {
                        r.run();
                    } else {
                        Static.engine.getCacheWorker().submit(r);
                    }
                    break;
            }
        }
        if (!requests.isEmpty()) {
            Runnable r = new Runnable() {
                public void run() {
                    int[] req = null;
                    while ((req = requests.poll()) != null) {
                        try {
                            Frame fileFrame = prepareFileFrame(0, Static.rs2Cache.getFile(req[0], req[1]));
                            if (fileFrame != null) {
                                session.write(fileFrame);
                            }
                        } catch (Exception e) {
                            logger.error("Error handling file [opcode=" + 0 + ", index=" + req[0] + ", file=" + req[1] + "]", e);
                        }
                    }
                }
            };
            Static.engine.getCacheWorker().submit(r);
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
