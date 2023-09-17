package com.ziotic.link.network.handler;

import com.ziotic.Static;
import com.ziotic.content.cc.ClanManager;
import com.ziotic.engine.login.LoginResponse;
import com.ziotic.link.WorldEntry;
import com.ziotic.link.WorldServerSession;
import com.ziotic.logic.player.PlayerSave;
import com.ziotic.logic.player.RemotePlayer;
import com.ziotic.network.Frame;
import com.ziotic.network.FrameBuilder;
import com.ziotic.utility.Logging;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class WSSPlayerHandler extends WSSFrameHandler {
    private static final Logger logger = Logging.log();

    @Override
    public void handleFrame(WorldServerSession world, IoSession session, Frame frame) {
        try {
            switch (frame.getOpcode()) {
                case 1: // Register player
                    registerPlayer(world, frame);
                    break;
                case 2: // Unregister player
                    unregisterPlayer(world, frame);
                    break;
                case 3: // Load player
                    loadPlayer(world, frame);
                    break;
                case 4: // Save player
                    savePlayer(world, frame);
                    break;
                case 12:
                    synchronizePlayerList(world, frame);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling player frame [" + frame.toString() + "]", e);
        }
    }

    private void synchronizePlayerList(WorldServerSession world, Frame frame) {
        System.out.println("recv sync");

        int playerCount = frame.readShort() & 0xffff;
        System.out.println("player count: " + playerCount);
        Map<String, RemotePlayer> playerList = new HashMap<String, RemotePlayer>(playerCount);
        for (int i = 0; i < playerCount; i++) {
            String name = frame.readString();
            String clanOwner = frame.readString();
            if (clanOwner.equals("")) {
                clanOwner = null;
            }
            boolean lobby = frame.readUnsigned() == 1;

            RemotePlayer player = new RemotePlayer(name, world.getId(), lobby);
            player.setClanOwner(clanOwner);
            playerList.put(name, player);
        }

        if (world.getServerType() == 0 || world.getServerType() == 2) {
            for (RemotePlayer player : new ArrayList<RemotePlayer>(world.getPlayers())) {
                if (!playerList.containsKey(player.getProtocolName())) {
                    Static.currentLink().unregisterPlayer(world, player);
                } else {
                    playerList.remove(player.getProtocolName());
                }
            }
        }
        if (world.getServerType() == 1 || world.getServerType() == 2) {
            for (RemotePlayer player : new ArrayList<RemotePlayer>(world.getLobbyPlayers())) {
                if (!playerList.containsKey(player.getProtocolName())) {
                    Static.currentLink().unregisterPlayer(world, player);
                } else {
                    playerList.remove(player.getProtocolName());
                }
            }
        }
        for (RemotePlayer player : playerList.values()) {
            Static.currentLink().registerPlayer(world, player);
            if (player.getClanOwner() != null) {
                ClanManager.unregisterPlayer(player.getProtocolName(), player.getClanOwner());
            }
        }
    }

    private void savePlayer(WorldServerSession world, Frame frame) throws IOException {
        String name = frame.readString();
        boolean lobby = frame.readUnsigned() == 1;
        byte[] data = new byte[frame.remaining()];
        frame.read(data);
        PlayerSave save = new PlayerSave();
        save.load(data);
        Static.currentLink().getDBLoader().savePlayer(name, lobby, save);
    }

    private void loadPlayer(WorldServerSession world, Frame frame) {
        String name = frame.readString();
        String password = frame.readString();
        int loginOpcode = frame.readUnsigned();
        PlayerSave save = new PlayerSave();
        LoginResponse resp = Static.currentLink().getDBLoader().loadPlayer(name, password, save);
        if (resp == LoginResponse.LOGIN && loginOpcode != 19) {
            WorldEntry worldDef;
            synchronized (Static.currentLink().getWorldList()) {
                worldDef = Static.currentLink().getWorldList().get(world.getId());
            }

            if (worldDef != null) {
                if (worldDef.members && (save.subscriptionEnd < System.currentTimeMillis() && save.rights == 0)) {
                    resp = LoginResponse.MEMBERS_REQUIRED;
                }
            }
        }
        FrameBuilder fb = new FrameBuilder(4, Frame.FrameType.VAR_SHORT, 1024);
        fb.writeString(name);
        fb.writeByte(resp.intValue());
        if (resp == LoginResponse.LOGIN) {
            fb.write(save.toByteArray());
        }
        IoSession session = world.getSession();
        if (session != null) {
            session.write(fb.toFrame());
        }
    }

    private void unregisterPlayer(WorldServerSession world, Frame frame) {
        String name = frame.readString();
        Static.currentLink().unregisterPlayer(world, name);
    }

    private void registerPlayer(WorldServerSession world, Frame frame) {
        String name = frame.readString();
        boolean lobby = frame.readUnsigned() == 1;
        RemotePlayer player = new RemotePlayer(name, world.getId(), lobby);
        Static.currentLink().registerPlayer(world, player);
    }
}
