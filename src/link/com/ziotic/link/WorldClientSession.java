package com.ziotic.link;

import com.ziotic.Application;
import com.ziotic.Static;
import com.ziotic.content.cc.Clan;
import com.ziotic.content.cc.ClanManager;
import com.ziotic.content.combat.Magic;
import com.ziotic.engine.login.LoginResponse;
import com.ziotic.engine.tick.Tick;
import com.ziotic.link.network.FrameWaiter;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.player.*;
import com.ziotic.logic.player.Player.Rights;
import com.ziotic.network.Frame;
import com.ziotic.network.FrameBuilder;
import com.ziotic.utility.Logging;
import com.ziotic.utility.Poolable;
import com.ziotic.utility.Streams;
import com.ziotic.utility.Text;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lazaro
 */
public class WorldClientSession implements Runnable, Poolable {
    private static Logger logger = Logging.log();

    public static final int[] FRAME_LENGTHS = new int[256];

    static {
        for (int i = 0; i < FRAME_LENGTHS.length; i++) {
            FRAME_LENGTHS[i] = -3;
        }
        FRAME_LENGTHS[1] = -2; // World list
        FRAME_LENGTHS[2] = -1; // Register player
        FRAME_LENGTHS[3] = -1; // Unregister player
        FRAME_LENGTHS[4] = -2; // Load player response & data
        FRAME_LENGTHS[5] = -1; // Relieve PM
        FRAME_LENGTHS[6] = -1; // Load clan
        FRAME_LENGTHS[7] = -1; // Join clan response
        FRAME_LENGTHS[8] = -1; // Register clan
        FRAME_LENGTHS[9] = -1; // Unregister clan
        FRAME_LENGTHS[10] = -1; // Register player to clan
        FRAME_LENGTHS[11] = -1; // Unregister player from clan
        FRAME_LENGTHS[12] = -1; // Update clan
        FRAME_LENGTHS[13] = -1; // Receive clan message
        FRAME_LENGTHS[14] = -1; // Kick player from clan
        FRAME_LENGTHS[15] = -2; // Synchronize remote player list
        FRAME_LENGTHS[20] = -1; // Handle muting
    }

    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private FrameWaiter frameWaiter;

    public void init() throws IOException {
        init(FrameWaiter.INSTANCE);
    }

    public void init(FrameWaiter frameWaiter) throws IOException {
        this.frameWaiter = frameWaiter;
        if (connect()) {
            Thread worldClientThread = new Thread(this);
            worldClientThread.setName("world-client");
            worldClientThread.start();
            return;
        }
        throw new IOException("Could not connect to link server!");
    }

    private boolean connect() throws IOException {
        socket = new Socket(Static.conf.getString("link_host"), Static.conf.getInt("link_port"));
        inStream = new DataInputStream(socket.getInputStream());
        outStream = new DataOutputStream(socket.getOutputStream());
        outStream.writeShort(5 + Static.conf.getString("link_pass").length() + Static.conf.getString("server_address").length());
        Streams.writeString(Static.conf.getString("link_pass"), outStream);
        outStream.writeShort(Static.world.getId());
        outStream.writeByte(Static.appType == Application.AppType.GAME_AND_LOBBY ? 2 : Static.isLobby() ? 1 : 0);
        Streams.writeString(Static.conf.getString("server_address"), outStream);
        outStream.flush();
        int response = inStream.readByte() & 0xff;
        if (response != LinkServer.OK_RESP) {
            logger.error("Recieved response " + response + " connecting to link server!");
            return false;
        }
        return true;
    }

    public void run() {
        while (true) {
            try {
                int opcode = inStream.readByte() & 0xff;
                int length = FRAME_LENGTHS[opcode];
                switch (length) {
                    case -1:
                        length = inStream.readByte() & 0xff;
                        break;
                    case -2:
                        length = inStream.readShort() & 0xffff;
                        break;
                    case -3:
                        logger.warn("Unhandled link client opcode : " + opcode);
                        continue;
                }
                byte[] payload = new byte[length];
                inStream.readFully(payload);
                Frame frame = new Frame(opcode, Frame.FrameType.RECIEVED, payload, length);
                handleFrame(frame);
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e1) {
                }
                logger.error("Link server disconnected : " + e.getMessage());
                while (true) {
                    try {
                        connect();
                        break;
                    } catch (IOException e1) {
                    }
                }
                logger.info("Reestablished a connection with the link server.");
            } catch (Throwable e) {
                logger.error("Error caught while communicating with the link server!", e);
            }
        }
    }

    private void handleFrame(Frame frame) {
        String name;
        int world;
        boolean lobby;
        PlayerType player;
        switch (frame.getOpcode()) {
            case 1: // World list
                Static.world.setWorldListData(frame.getBytes());
                break;
            case 2: // Register remote player
                name = frame.readString();
                world = frame.readUnsigned();
                lobby = frame.readUnsigned() == 1;

                player = new RemotePlayer(name, world, lobby);

                Static.world.register((RemotePlayer) player);
                break;
            case 3: // Un-register remote player
                name = frame.readString();

                Static.world.unregister(name);
                break;
            case 5: // Receive PM
                name = frame.readString();

                String sender = frame.readString();
                int rights = frame.readUnsigned();
                String message = frame.readString();

                player = Static.world.findPlayer(name);
                if (player != null) {
                    ((Player) player).getFriends().receivePM(sender, rights, message);
                }
                break;
            case 8: // Register clan
                try {
                    byte[] data = new byte[frame.remaining()];
                    frame.read(data);

                    Clan clan = new Clan();
                    clan.load(data);

                    ClanManager.registerClan(clan);
                } catch (IOException e) {
                    logger.error("Error registering clan!", e);
                }
                break;
            case 9: // Unregister clan
                String owner = frame.readString();

                ClanManager.unregisterClan(owner);
                break;
            case 10: // Register player to clan
                name = frame.readString();
                owner = frame.readString();

                player = Static.world.findPlayer(name);
                if (player instanceof RemotePlayer) {
                    ClanManager.registerPlayer((RemotePlayer) player, owner);
                }
                break;
            case 11: // Unregister player from clan
                name = frame.readString();
                owner = frame.readString();

                ClanManager.unregisterPlayer(name, owner);
                break;
            case 12: // Update clan
                try {
                    byte[] data = new byte[frame.remaining()];
                    frame.read(data);

                    Clan clan = new Clan();
                    clan.load(data);

                    ClanManager.updateClan(clan.getOwner(), clan);
                } catch (IOException e) {
                    logger.error("Error updating clan!", e);
                }
                break;
            case 13:
                owner = frame.readString();
                name = frame.readString();
                rights = frame.readUnsigned();
                message = frame.readString();

                ClanManager.receiveClanMessage(owner, name, rights, message);
                break;
            case 14:
                name = frame.readString();

                ClanManager.kickPlayer2(name);
                break;
            case 20:
                String moderator = frame.readString();
                String adjustedUser = Text.formatNameForDisplay(frame.readString());
                boolean mute = frame.read() == 1;
                boolean succesful = frame.read() == 1;
                boolean changedState = frame.read() == 1;
                player = Static.world.findPlayer(moderator);
                if (player != null) {
                    if (succesful) {
                        if (changedState) {
                            if (mute)
                                ((Player) player).sendMessage("You have succesfully muted " + adjustedUser + ".");
                            else
                                ((Player) player).sendMessage("You have succesfully unmuted " + adjustedUser + ".");
                        } else {
                            if (mute)
                                ((Player) player).sendMessage(adjustedUser + " is already muted. You can't mute an already muted player.");
                            else
                                ((Player) player).sendMessage(adjustedUser + " is not muted. You can't unmute an unmuted player.");
                        }
                    } else {
                        ((Player) player).sendMessage("The user " + adjustedUser + " does not to exist.");
                    }
                }
                break;
            default:
                String userName = frame.readString();
                frameWaiter.submitFrame(userName, frame);
                break;
        }
    }

    public Frame waitForFrame(String userName) throws InterruptedException {
        return frameWaiter.waitForFrame(userName);
    }

    public boolean write(Frame frame) {
        return write(frame, -1);
    }

    public boolean write(Frame frame, int maxTrys) {
        int tryNumber = 1;
        while (true) {
            synchronized (outStream) {
                try {
                    outStream.write(frame.getBuffer(), 0, frame.getLength());
                    outStream.flush();
                    return true;
                } catch (IOException e) {
                }
            }
            if (tryNumber == maxTrys) {
                return false;
            }
            if (tryNumber == 3) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            tryNumber++;
        }
    }

    public LoginResponse loadPlayerDetails(final Player player) {
        try {
            FrameBuilder fb = new FrameBuilder(3, Frame.FrameType.VAR_BYTE, 1024);
            fb.writeString(player.getProtocolName());
            fb.writeString(player.getPassword());
            fb.writeByte(player.getLoginOpcode());
            write(fb.toFrame());

            Frame frame = waitForFrame(player.getProtocolName());

            LoginResponse resp = Static.clientConf.getLoginResponseForCode(frame.readUnsigned());
            if (resp != LoginResponse.LOGIN) {
                return resp;
            }

            byte[] data = new byte[frame.remaining()];
            frame.read(data);

            PlayerSave save = new PlayerSave();
            save.load(data);

            player.setUserId(save.userId);

            player.setEmail(save.email);
            player.setUnreadMessages(save.unreadMessages);
            player.setSubscriptionEnd(save.subscriptionEnd);

            player.setLastLoggedIn(save.lastLoggedIn);

            player.setLastIPs(save.lastIPs);

            player.setRights(Rights.forValue(save.rights));

            for (String friend : save.friends) {
                player.getFriends().getFriendsList().add(friend);
            }

            for (String ignore : save.ignores) {
                player.getFriends().getIgnoreList().add(ignore);
            }

            player.getFriends().setPrivateChatSetting(save.privateChatSetting);
            player.getFriends().setPrivateChatColor(save.privateChatColor);

            player.setMuted(save.isMuted);

            if (player.inGame()) {
                if (save.x != -1 || save.y != -1 || save.z != -1) {
                    player.getAttributes().set("loc", Tile.locate(save.x, save.y, save.z));
                }

                player.setRunning(save.runToggled);
                player.setRunningEnergy(save.runEnergy);

                for (int i = 0; i < 28; i++) {
                    player.getInventory().set(save.inv[i], save.invN[i], i);
                }
                for (int i = 0; i < 14; i++) {
                    player.getEquipment().set(save.equip[i], save.equipN[i], i);
                }
                for (int i = 0; i < save.bank.length; i++) {
                    int[] item = save.bank[i];
                    player.getBank().setTab(player.getBank().set(item[0], item[1], i), item[2]);
                }
                for (int i = 0; i < Levels.SKILL_COUNT; i++) {
                    player.getLevels().setCurrentLevel(i, save.level[i]);
                    player.getLevels().setXP(i, save.exp[i]);
                }
                player.getLevels().setXPGained(save.xpCounter);
                player.getCombat().setAutoRetaliate(save.autoRetaliate);
                player.getCombat().setWeapon(player, save.attackStyle);
                player.getPrayerManager().setCurses(save.prayerBook);
                player.getPrayerManager().setQuickPrayers(save.quickPrayers);
                Magic.setMagic(player, save.spellBook);
                player.getCombat().getSpecialEnergy().setAmount(save.specialEnergy);
                player.getAppearance().setLooks(save.looks);
                player.getAppearance().setColors(save.colours);
                player.getLevels().calculateLevels();
                player.getLevels().calculateCombat();
                player.getItemsOnDeathManager().isSkulled(save.isSkulled);
                if (save.isSkulled) {
                    player.getAppearance().setPKIcon(0);
                    Tick tick = new Tick("SkullTimer", save.skullTimer) {
                        @Override
                        public boolean execute() {
                            player.getItemsOnDeathManager().isSkulled(false);
                            player.getAppearance().setPKIcon(-1);
                            return false;
                        }
                    };
                    player.registerTick(tick);
                }

                player.setHP(player.getMaxHP());

                player.setOwnClan(save.ownClan);

                if (save.lastClan != null) {
                    player.getAttributes().set("lastClan", save.lastClan);
                }
            }

            return resp;
        } catch (Exception e) {
            logger.error("Error loading player [" + player.getProtocolName() + "]", e);

            return LoginResponse.ERROR;
        }
    }

    public void registerPlayer(Player player) {
        FrameBuilder fb = new FrameBuilder(1, Frame.FrameType.VAR_BYTE, 64);
        fb.writeString(player.getProtocolName());
        fb.writeByte(player.inLobby() ? 1 : 0);
        write(fb.toFrame());
    }

    public void unregisterPlayer(Player player) {
        unregisterPlayer(player, true);
    }

    public void unregisterPlayer(Player player, boolean savePlayer) {
        FrameBuilder fb = new FrameBuilder(2, Frame.FrameType.VAR_BYTE, 64);
        fb.writeString(player.getProtocolName());
        write(fb.toFrame());

        if (savePlayer) {
            savePlayerDetails(player);
        }
    }

    public boolean savePlayerDetails(Player player) {
        FrameBuilder fb = new FrameBuilder(4, Frame.FrameType.VAR_SHORT, 1024);
        fb.writeString(player.getProtocolName());
        fb.writeByte(player.inLobby() ? 1 : 0);

        PlayerSave save = new PlayerSave();
        save.load(player);
        fb.write(save.toByteArray());

        return write(fb.toFrame(), 5);
    }

    public void sendPM(Player player, String recipient, String message) {
        FrameBuilder fb = new FrameBuilder(5, Frame.FrameType.VAR_BYTE, 64);
        fb.writeString(player.getProtocolName());
        fb.writeByte(player.getRights().intValue());
        fb.writeString(recipient);
        fb.writeString(message);

        write(fb.toFrame());
    }

    public Clan loadClan(Player player, String owner) {
        try {
            FrameBuilder fb = new FrameBuilder(6, Frame.FrameType.VAR_BYTE, 512);
            fb.writeString(player.getProtocolName());
            fb.writeString(owner);

            write(fb.toFrame());

            Frame frame = waitForFrame(player.getProtocolName());

            int resp = frame.readUnsigned();
            if (resp == 0) {
                return null;
            } else {
                byte[] data = new byte[frame.remaining()];
                frame.read(data);

                Clan clan = new Clan();
                clan.load(data);

                return clan;
            }
        } catch (Exception e) {
            logger.error("Error loading clan: " + owner);

            return null;
        }
    }

    public int joinClan(Player player, String owner) {
        try {
            FrameBuilder fb = new FrameBuilder(7, Frame.FrameType.VAR_BYTE, 64);
            fb.writeString(player.getProtocolName());
            fb.writeString(owner);

            write(fb.toFrame());

            Frame frame = waitForFrame(player.getProtocolName());

            return frame.readUnsigned();
        } catch (Exception e) {
            logger.error("Error joining clan: " + owner);

            return 2;
        }
    }

    public void leaveClan(Player player, String owner) {
        FrameBuilder fb = new FrameBuilder(8, Frame.FrameType.VAR_BYTE, 64);
        fb.writeString(player.getProtocolName());
        fb.writeString(owner);

        write(fb.toFrame());
    }

    public void saveClan(Clan clan) {
        FrameBuilder fb = new FrameBuilder(9, Frame.FrameType.VAR_BYTE, 512);
        fb.write(clan.toByteArray());

        write(fb.toFrame());
    }

    public void sendClanMessage(Player player, String owner, String message) {
        FrameBuilder fb = new FrameBuilder(10, Frame.FrameType.VAR_BYTE, 64);
        fb.writeString(player.getProtocolName());
        fb.writeByte(player.getRights().intValue());
        fb.writeString(owner);
        fb.writeString(message);

        write(fb.toFrame());
    }

    public void kickPlayerFromClan(String name) {
        FrameBuilder fb = new FrameBuilder(11, Frame.FrameType.VAR_BYTE, 64);
        fb.writeString(name);

        write(fb.toFrame());
    }

    public void sendPlayerMuteSettings(String moderator, String name, boolean mute) {
        FrameBuilder fb = new FrameBuilder(20, Frame.FrameType.VAR_BYTE, 64);
        fb.writeString(moderator);
        fb.writeString(name);
        fb.writeByte(mute ? 1 : 0);

        write(fb.toFrame());
    }


    public void synchronizeLocalPlayerList() {
        List<PlayerType> playerList;
        synchronized (Static.world.getPlayerMap()) {
            playerList = new ArrayList<PlayerType>(Static.world.getPlayerMap().values());
        }

        FrameBuilder fb = new FrameBuilder(12, Frame.FrameType.VAR_SHORT, 5120);

        int playerCount = 0;
        FrameBuilder block = new FrameBuilder(5120);
        for (PlayerType playerType : playerList) {
            if (playerType instanceof Player) {
                Player player = (Player) playerType;

                block.writeString(player.getProtocolName());
                block.writeString(player.getClanOwner() == null ? "" : player.getClanOwner());
                block.writeByte(player.inLobby() ? 1 : 0);

                playerCount++;
            }
        }

        fb.writeShort(playerCount);
        fb.write(block.toFrame().getBytes());

        write(fb.toFrame());

    }

    @Override
    public boolean expired() {
        return socket == null || !socket.isConnected() || socket.isClosed();
    }

    @Override
    public void recycle() {
    }
}
