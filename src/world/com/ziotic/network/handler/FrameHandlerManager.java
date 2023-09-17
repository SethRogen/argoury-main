package com.ziotic.network.handler;

import com.ziotic.Static;
import com.ziotic.adapter.protocol.FrameHandlerMapperAdapter;
import com.ziotic.link.network.WSSFrameHandlerMap;
import com.ziotic.network.Frame;
import com.ziotic.network.FrameHandler;
import com.ziotic.utility.Logging;
import com.ziotic.utility.Mapper;
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
