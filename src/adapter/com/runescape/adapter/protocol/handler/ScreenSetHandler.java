package com.runescape.adapter.protocol.handler;

import com.runescape.Static;
import com.runescape.logic.player.DisplayMode;
import com.runescape.logic.player.Player;
import com.runescape.logic.utility.GameInterface;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;

import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lazaro
 */
public class ScreenSetHandler extends PlayerFrameHandler {
    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        int displayMode = frame.readUnsigned();
        int width = frame.readUnsignedShort();
        int height = frame.readUnsignedShort();
        frame.readUnsigned();

        if (player.isOnLogin()) {
            player.setDisplayMode(DisplayMode.forValue(displayMode));
            switch (player.getDisplayMode()) {
                case FIXED:
                    Static.proto.sendFixedScreen(player);
                    break;
                case RESIZABLE:
                case FULL_SCREEN:
                    Static.proto.sendResizableScreen(player);
                    break;
            }
            Static.proto.sendAccessMask(player, 65535, 65535, player.getDisplayMode() == DisplayMode.FIXED ? 548 : 746, player.getDisplayMode() == DisplayMode.FIXED ? 81 : 120, 0, 2).sendAccessMask(player, 65535, 65535, 884, 11, 0, 2).sendAccessMask(player, 65535, 65535, 884, 12, 0, 2).sendAccessMask(player, 65535, 65535, 884, 13, 0, 2).sendAccessMask(player, 65535, 65535, 884, 14, 0, 2);
            player.setOnLogin(false);

            Static.callScript("login.onLogin", player);
        } else {
            DisplayMode oldDisplayMode = player.getDisplayMode();
            player.setDisplayMode(DisplayMode.forValue(displayMode));
            if (!player.getDisplayMode().equals(oldDisplayMode)) {
                List<GameInterface> gameInterfaces = new ArrayList<GameInterface>(player.getCurrentInterfaces().values());
                for (GameInterface gameInterface : gameInterfaces) {
                    Static.proto.sendCloseInterface(player, gameInterface);
                }
                switch (player.getDisplayMode()) {
                    case FIXED:
                        Static.proto.switchToFixedScreen(player);
                        break;
                    case RESIZABLE:
                    case FULL_SCREEN:
                        Static.proto.switchToResizableScreen(player);
                        break;
                }
                for (GameInterface gameInterface : gameInterfaces) {
                    Static.proto.sendInterface(player, gameInterface);
                }
            }
        }
    }
}
