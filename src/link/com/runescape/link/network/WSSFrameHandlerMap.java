package com.runescape.link.network;

import com.runescape.link.network.handler.WSSClanHandler;
import com.runescape.link.network.handler.WSSFriendHandler;
import com.runescape.link.network.handler.WSSMuteHandler;
import com.runescape.link.network.handler.WSSPlayerHandler;
import com.runescape.network.FrameHandler;
import com.runescape.utility.Logging;
import com.runescape.utility.Mapper;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class WSSFrameHandlerMap implements Mapper<Integer, FrameHandler> {
    private static final Logger logger = Logging.log();

    private Map<Integer, FrameHandler> handlers = null;

    @Override
    public Map<Integer, FrameHandler> map() {
        handlers = new HashMap<Integer, FrameHandler>();

        bind(new WSSPlayerHandler(), 1, 2, 3, 4, 12);
        bind(new WSSFriendHandler(), 5);
        bind(new WSSClanHandler(), 6, 7, 8, 9, 10, 11);
        bind(new WSSMuteHandler(), 20);

        return handlers;
    }

    private void bind(FrameHandler handler, int... opcodes) {
        for (int op : opcodes) {
            if (handlers.put(op, handler) != null) {
                logger.warn("Ambiguous frame handler opcode : " + op);
            }
        }
    }
}
