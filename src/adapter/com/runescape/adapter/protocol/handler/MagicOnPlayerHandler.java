package com.runescape.adapter.protocol.handler;

import com.runescape.Static;
import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;
import com.runescape.utility.Logging;

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
