package com.runescape.adapter.protocol.handler;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import com.runescape.network.Frame;
import com.runescape.network.FrameHandler;
import com.runescape.utility.Logging;

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
