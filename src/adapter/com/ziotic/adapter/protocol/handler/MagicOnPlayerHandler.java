package com.ziotic.adapter.protocol.handler;

import com.ziotic.Static;
import com.ziotic.logic.player.Player;
import com.ziotic.network.Frame;
import com.ziotic.network.handler.PlayerFrameHandler;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

public class MagicOnPlayerHandler extends PlayerFrameHandler {

    @SuppressWarnings("unused")
    private static final Logger logger = Logging.log();

    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        int playerIndex = frame.readLEShortA();
        frame.readLEShortA();
        frame.readLEShort();
        frame.read();
        int interfaceSettings = frame.readInt();
        int interfaceId = interfaceSettings >> 16;
        int interfaceButton = interfaceSettings & 0xff;

        final Player player2 = Static.world.getPlayers().get(playerIndex);

        player.getCombat().createNewCombatAction(player, player2, true, interfaceId, interfaceButton);

    }

}
