package com.ziotic.link.network.handler;

import com.ziotic.Static;
import com.ziotic.content.cc.Clan;
import com.ziotic.content.cc.ClanManager;
import com.ziotic.link.WorldServerSession;
import com.ziotic.network.Frame;
import com.ziotic.network.FrameBuilder;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import java.io.IOException;

/**
 * @author Lazaro
 */
public class WSSClanHandler extends WSSFrameHandler {
    private static final Logger logger = Logging.log();

    @Override
    public void handleFrame(WorldServerSession world, IoSession session, Frame frame) {
        try {
            switch (frame.getOpcode()) {
                case 6: // Load clan
                    loadClan(world, frame);
                    break;
                case 7: // Join clan
                    joinClan(world, frame);
                    break;
                case 8: // Leave clan
                    leaveClan(world, frame);
                    break;
                case 9: // Save clan
                    saveClan(world, frame);
                    break;
                case 10: // Send message
                    sendMessage(world, frame);
                    break;
                case 11: // Kick player
                    kickPlayer(world, frame);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling clan frame [" + frame.toString() + "]", e);
        }
    }

    private void kickPlayer(WorldServerSession world, Frame frame) {
        String name = frame.readString();

        Static.currentLink().kickPlayerFromClan(name);
    }

    private void sendMessage(WorldServerSession world, Frame frame) {
        String sender = frame.readString();
        int rights = frame.readUnsigned();
        String owner = frame.readString();
        String message = frame.readString();

        Static.currentLink().sendCCMessage(sender, rights, owner, message);
    }

    private void saveClan(WorldServerSession world, Frame frame) throws IOException {
        byte[] clanData = new byte[frame.remaining()];
        frame.read(clanData);
        Clan clan = new Clan();
        clan.load(clanData);
        ClanManager.saveClan2(clan);
    }

    private void leaveClan(WorldServerSession world, Frame frame) {
        String name = frame.readString();
        String owner = frame.readString();

        ClanManager.unregisterPlayer(name, owner);
    }

    private void joinClan(WorldServerSession world, Frame frame) {
        String name = frame.readString();
        String owner = frame.readString();
        int resp = ClanManager.attemptJoinChannel(name, owner);
        FrameBuilder fb = new FrameBuilder(7, Frame.FrameType.VAR_BYTE, 64);
        fb.writeString(name);
        fb.writeByte(resp);
        IoSession session = world.getSession();
        if (session != null) {
            session.write(fb.toFrame());
        }
    }

    private void loadClan(WorldServerSession world, Frame frame) {
        String name = frame.readString();
        String owner = frame.readString();
        Clan clan = new Clan(owner);
        boolean resp = Static.currentLink().getDBLoader().loadClan(owner, clan);
        FrameBuilder fb = new FrameBuilder(6, Frame.FrameType.VAR_BYTE, 1024);
        fb.writeString(name);
        if (resp) {
            fb.writeByte(1);
            fb.write(clan.toByteArray());
        } else {
            fb.writeByte(0);
        }
        IoSession session = world.getSession();
        if (session != null) {
            session.write(fb.toFrame());
        }
    }
}
