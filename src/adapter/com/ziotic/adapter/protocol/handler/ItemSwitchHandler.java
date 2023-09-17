package com.ziotic.adapter.protocol.handler;

import com.ziotic.Static;
import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.handler.PlayerFrameHandler;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class ItemSwitchHandler extends PlayerFrameHandler {
    private static final Logger logger = Logging.log();

    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        int interfaceSet1 = frame.readInt();
        int id1 = frame.readShortA();
        int interfaceSet2 = frame.readInt();
        int indexFrom = frame.readLEShort();
        int indexTo = frame.readLEShort();
        int id2 = frame.readLEShort();

        int interfaceId1 = interfaceSet1 >> 16;
        int childId1 = interfaceSet1 & 0xffff;

        int interfaceId2 = interfaceSet2 >> 16;
        int childId2 = interfaceSet2 & 0xffff;

        if (!Static.ahs.handleItemSwitch(player, interfaceId1, childId1, interfaceId2, childId2, id1, id2, indexFrom, indexTo)) {
            Static.callScript("itemswitches.handleSwitch", player, interfaceId1, childId1, interfaceId2, childId2, id1, id2, indexFrom, indexTo);
        }

        if (interfaceId1 != interfaceId2) {
            logger.warn("Item switch performed with different interfaces! Please check which " + "interface is which and refactor this properly in " + "com.ziotic.adapter.protocol.handler.ItemSwitchHandler");
        }
    }
}
