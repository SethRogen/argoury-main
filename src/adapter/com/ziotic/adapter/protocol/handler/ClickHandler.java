package com.ziotic.adapter.protocol.handler;

import com.ziotic.network.Frame;
import com.ziotic.network.FrameHandler;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

public class ClickHandler implements FrameHandler {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logging.log();

    @SuppressWarnings("unused")
    @Override
    public void handleFrame(IoSession session, Frame frame) {
        int mouseSettings = frame.readShort();
        int coordinates = frame.readInt();

        int mouseButton = mouseSettings >> 15;
        int interval = mouseSettings & 0xff;
        int y = coordinates >> 16;
        int x = coordinates & 0xffff;
    }

}
