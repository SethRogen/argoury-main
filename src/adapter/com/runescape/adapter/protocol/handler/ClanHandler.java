package com.runescape.adapter.protocol.handler;

import com.runescape.content.cc.ClanManager;
import com.runescape.content.cc.Clan.Rank;
import com.runescape.logic.player.Player;
import com.runescape.network.Frame;
import com.runescape.network.handler.PlayerFrameHandler;

import org.apache.mina.core.session.IoSession;

/**
 * @author Lazaro
 */
public class ClanHandler extends PlayerFrameHandler {
    @Override
    public void handleFrame(Player player, IoSession session, Frame frame) {
        switch (frame.getOpcode()) {
            case 18: // Join/Leave clan
                joinOrLeaveClan(player, frame);
                break;
            case 39: // Kick player from clan
                kickPlayer(player, frame);
                break;
            case 25: // Rank player in clan
                rankPlayer(player, frame);
                break;
        }
    }

    private void joinOrLeaveClan(Player player, Frame frame) {
        if (frame.remaining() >= 1) {
            String owner = frame.readString();

            ClanManager.joinChannel(player, owner);
        } else {
            ClanManager.leaveChannel(player);
        }
    }

    private void kickPlayer(Player player, Frame frame) {
        String name = frame.readString();

        ClanManager.kickPlayer(player, name);
    }

    private void rankPlayer(Player player, Frame frame) {
        String name = frame.readString();
        int rank = frame.readUnsigned();

        if (rank > 6) {
            return;
        }

        ClanManager.rankPlayer(player, name, Rank.forValue(rank));
    }
}
