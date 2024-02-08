package com.runescape.network.handler;

import com.runescape.Static;
import com.runescape.adapter.protocol.FrameHandlerMapperAdapter;
import com.runescape.link.network.WSSFrameHandlerMap;
import com.runescape.network.Frame;
import com.runescape.network.FrameHandler;
import com.runescape.network.handler.DebugFrameHandler;
import com.runescape.utility.Logging;
import com.runescape.utility.Mapper;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @author Lazaro
 */
public class FrameHandlerManager {
    private static Logger logger = Logging.log();

    private FrameHandler[] frameHandlers = new FrameHandler[Frame.MAX_OPCODE + 1];

    public FrameHandlerManager() {
        Mapper<Integer, FrameHandler> mapper = Static.isLink() ? new WSSFrameHandlerMap() : new FrameHandlerMapperAdapter();
        Map<Integer, FrameHandler> handlerMap = mapper.map();
        for (Map.Entry<Integer, FrameHandler> handler : handlerMap.entrySet()) {
            frameHandlers[handler.getKey()] = handler.getValue();
        }
        logger.info("Loaded " + handlerMap.size() + " frame(s)");
    }

    public FrameHandler getHandler(int opcode) {
        FrameHandler handler = frameHandlers[opcode];
        if (handler == null) {
            handler = frameHandlers[opcode] = new DebugFrameHandler();
        }
        return handler;
    }
}
