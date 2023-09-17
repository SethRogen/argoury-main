package com.ziotic.adapter.protocol;

import com.ziotic.Constants;
import com.ziotic.Static;
import com.ziotic.content.cc.Clan;
import com.ziotic.content.cc.Clan.Rank;
import com.ziotic.engine.login.LoginResponse;
import com.ziotic.link.WorldEntry;
import com.ziotic.link.WorldServerSession;
import com.ziotic.logic.Entity;
import com.ziotic.logic.item.GroundItem;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.map.Region;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.mask.Chat;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.DisplayMode;
import com.ziotic.logic.player.Levels;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.player.PlayerType;
import com.ziotic.logic.utility.GameInterface;
import com.ziotic.logic.utility.GameInterfaces;
import com.ziotic.network.Frame;
import com.ziotic.network.Frame.FrameType;
import com.ziotic.network.FrameBuilder;
import com.ziotic.network.Protocol;
import com.ziotic.utility.Logging;
import com.ziotic.utility.Text;
import org.apache.log4j.Logger;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Lazaro
 */
public final class ProtocolAdapter implements Protocol {
    private static final Logger logger = Logging.log();

    @Override
    public Protocol sendLobbyResponse(IoSession session, Player player, LoginResponse resp) {
        FrameBuilder fb = new FrameBuilder(resp == LoginResponse.LOGIN ? 256 : 1);
        fb.writeByte(resp.intValue());
        if (resp == LoginResponse.LOGIN) {
            FrameBuilder responseBlock = new FrameBuilder(256);
            responseBlock.writeByte(0);//1
            responseBlock.writeByte(0);//2
            responseBlock.writeByte(0);//3
            responseBlock.writeByte(0);//4
            responseBlock.writeByte(0);//5
            responseBlock.writeLong(player.getSubscriptionEnd());//members subscription end
            responseBlock.writeByte(player.getSubscriptionEnd() > System.currentTimeMillis() ? 0x1 : 0);//0x1 - if members, 0x2 - subscription
            responseBlock.writeInt(0);//recovery questions set date
            responseBlock.writeShort(1);//recovery questions
            responseBlock.writeShort(player.getUnreadMessages());//unread messages
            responseBlock.writeShort(player.getLastLoggedIn() == 0 ? 0 : (int) (((player.getLastLoggedIn() - 1014786000000L) / 86400000) + 1));//last logged in date
            String[] ipSplit = player.getLastIP().split("\\.");
            int ipHash = Integer.parseInt(ipSplit[0]) << 24 | Integer.parseInt(ipSplit[1]) << 16 | Integer.parseInt(ipSplit[2]) << 8 | Integer.parseInt(ipSplit[3]);
            responseBlock.writeInt(ipHash);//last ip
            responseBlock.writeByte(player.getEmail() != null ? 3 : 0); // email status (0 - no email, 1 - pending parental confirmation, 2 - pending confirmation, 3 - registered)
            responseBlock.writeShort(0);//14
            responseBlock.writeShort(0);//15
            responseBlock.writeByte(0);//16
            responseBlock.writeJagString(player.getName());//17
            responseBlock.writeByte(0);//18
            responseBlock.writeInt(1);//0 doesn't let you login, anything over 0 does
            responseBlock.writeByte(0);//20
            responseBlock.writeShort(1); // current world id
            responseBlock.writeJagString(Static.conf.getString("server_address")); // the world address
            Frame responseBlockMessage = responseBlock.toFrame();
            fb.writeByte(responseBlockMessage.getLength()).write(responseBlockMessage.getBytes());
            session.write(fb.toFrame());
        } else {
            session.write(fb.toFrame()).addListener(IoFutureListener.CLOSE);
        }
        return this;
    }

    @Override
    public Protocol sendLoginResponse(IoSession session, Player player, LoginResponse resp) {
        FrameBuilder fb = new FrameBuilder(resp == LoginResponse.LOGIN ? 64 : 1);
        fb.writeByte(resp.intValue());
        if (resp == LoginResponse.LOGIN) {
            FrameBuilder responseBlock = new FrameBuilder(64);
            responseBlock.writeByte(/* player.getRights().intValue() */2).writeByte(0)
                    // TODO temporary
                    .writeByte(0).writeByte(0).writeByte(0).writeByte(0).writeShort(player.getIndex()).writeByte(1).writeTriByte(0).writeByte(1);
            Frame responseBlockPacket = responseBlock.toFrame();
            fb.writeByte(responseBlockPacket.getLength()).write(responseBlockPacket.getBytes());
            session.write(fb.toFrame());
        } else {
            session.write(fb.toFrame()).addListener(IoFutureListener.CLOSE);
        }
        return this;
    }

    @Override
    public Frame generateWorldList() {
        if (!Static.isLink()) {
            throw new UnsupportedOperationException("Must be called only on the link server!");
        }

        FrameBuilder fb = new FrameBuilder(1, Frame.FrameType.VAR_SHORT, 512);
        fb.writeByte(1).writeByte(2).writeByte(1);
        fb.writeSmart(Static.currentLink().getWorldList().size()); // world count
        for (WorldEntry w : Static.currentLink().getWorldList().values()) {
            fb.writeSmart(w.countryId).writeJagString(w.countryName); // country id and name
        }
        fb.writeSmart(0).writeSmart(Static.currentLink().getWorldList().size() + 1).writeSmart(Static.currentLink().getWorldList().size()); // world count
        for (WorldEntry w : Static.currentLink().getWorldList().values()) {
            fb.writeSmart(w.id); // world id
            fb.writeByte(w.members ? 1 : 0); // members
            int flags = 0;
            if (w.members) flags |= 0x1;
            if (w.quickChat) flags |= 0x2;
            if (w.highRisk) flags |= 0x400;
            if (w.skillReq) flags |= 0x80;
            if (w.lootShare) flags |= 0x8;
            if (w.highlight) flags |= 0x10;
            fb.writeInt(flags); // flags
            fb.writeJagString(w.activity); // activity

            WorldServerSession session = Static.currentLink().getGame(w.id);
            if (session != null) {
                fb.writeJagString(session.getAddress()); // world ip
            } else {
                fb.writeJagString("0.0.0.0");
            }
        }
        fb.writeInt(0x94DA4A87); // magic key
        for (WorldEntry w : Static.currentLink().getWorldList().values()) {
            fb.writeSmart(w.id); // world id

            WorldServerSession session = Static.currentLink().getGame(w.id);
            if (session != null) {
                fb.writeShort(session.getPlayers().size());
            } else {
                fb.writeShort(-1);
            }
        }
        return fb.toFrame();
    }

    @Override
    public Protocol sendFriends(Player player, String... friends) {
        Clan clan = player.getOwnClan();

        FrameBuilder fb = new FrameBuilder(118, FrameType.VAR_SHORT, 256);
        for (String friendObj : friends) {
            String friendName = Text.formatNameForDisplay(friendObj);
            PlayerType friend = Static.world.findPlayer(friendObj);

            fb.writeByte(player.isOnLogin() ? 1 : 0);

            fb.writeString(friendName); // display name
            fb.writeString(""); // last known as
            fb.writeShort(friend == null ? 0 : friend.getWorld());
            fb.writeByte(clan != null ? clan.getRank(friendObj, Rank.FRIEND).intValue() : 0); // rank
            if (friend != null) {
                StringBuilder sb = new StringBuilder();
                int thisWorldHash = player.inLobby() ? -(Static.world.getId() + 1) : Static.world.getId() + 1;
                int friendWorldHash = friend.inLobby() ? -(friend.getWorld() + 1) : friend.getWorld() + 1;
                if (thisWorldHash != friendWorldHash) {
                    sb.append("<col=FFFF00>");
                } else {
                    sb.append("<col=00FF00>");
                }
                if (friend.inLobby()) {
                    sb.append("Lobby ");
                } else {
                    sb.append("Runescape ").append(' ');
                }
                sb.append(friend.getWorld());
                fb.writeString(sb.toString());
                fb.writeByte(0); // ??
            }
        }
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendIncommingPM(Player player, String sender, int rights, String message, long id) {
        FrameBuilder fb = new FrameBuilder(10, FrameType.VAR_BYTE, 64);
        fb.writeByte(0); // has display name bool
        fb.writeString(sender);
        fb.writeShort((int) (id >> 32)).writeTriByte((int) (id - ((id >> 32) << 32)));
        fb.writeByte(rights);
        byte[] textBuffer = new byte[256];
        textBuffer[0] = (byte) message.length();
        int length = Text.huffmanCompress(message, textBuffer, 1);
        fb.write(textBuffer, 0, length + 1);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendOutgoingPM(Player player, String recipient, String message) {
        FrameBuilder fb = new FrameBuilder(32, FrameType.VAR_BYTE, 64);
        byte[] textBuffer = new byte[256];
        textBuffer[0] = (byte) message.length();
        int length = Text.huffmanCompress(message, textBuffer, 1);
        fb.writeString(recipient).write(textBuffer, 0, length + 1);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendPrivateChatSetting(Player player, int setting) {
        FrameBuilder fb = new FrameBuilder(62, 1);
        fb.writeByte(setting);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendIgnores(Player player) {
        FrameBuilder fb = new FrameBuilder(72, FrameType.VAR_SHORT, 256);
        fb.writeByte(player.getFriends().getIgnoreList().size());
        for (String ignore : player.getFriends().getIgnoreList()) {
            ignore = Text.formatNameForDisplay(ignore);

            fb.writeString(ignore);
            fb.writeString("");
            fb.writeString("");
            fb.writeString("");
        }
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendClan(Player player) {
        Clan clan = player.getClan();

        FrameBuilder fb = new FrameBuilder(22, FrameType.VAR_SHORT, 256);
        if (clan != null) {
            fb.writeString(Text.formatNameForDisplay(clan.getOwner()));
            fb.writeByte(0);
            fb.writeLong(Text.stringToLong(clan.getName()));
            fb.writeByte(clan.getKickRequirement().intValue());
            fb.writeByte(clan.getPlayers().size());
            for (PlayerType p2 : clan.getPlayers().values()) {
                fb.writeString(p2.getName());
                fb.writeByte(0);
                fb.writeShort(p2.getWorld());
                Rank rank = clan.getRank(p2.getProtocolName());
                fb.writeByte(rank.intValue());
                StringBuilder sb = new StringBuilder();
                int thisWorldHash = player.inLobby() ? -(Static.world.getId() + 1) : Static.world.getId() + 1;
                int friendWorldHash = p2.inLobby() ? -(p2.getWorld() + 1) : p2.getWorld() + 1;
                if (thisWorldHash != friendWorldHash) {
                    sb.append("<col=FFFF00>");
                } else {
                    sb.append("<col=00FF00>");
                }
                if (p2.inLobby()) {
                    sb.append("Lobby ");
                } else {
                    sb.append("Runescape ").append(' ');
                }
                sb.append(p2.getWorld());
                fb.writeString(sb.toString());
            }
        }
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendWorldList(IoSession session) {
        byte[] worldListData = Static.world.getWorldListData();
        if (worldListData == null) {
            return this;
        }
        FrameBuilder fb = new FrameBuilder(90, Frame.FrameType.VAR_SHORT, 256);
        fb.write(worldListData);
        session.write(fb.toFrame());
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendMapRegion(Player)
      */

    @Override
    public Protocol sendMapRegion(Player player) {
        FrameBuilder fb = new FrameBuilder(35, Frame.FrameType.VAR_SHORT, 256);
        if (player.isOnLogin()) {
            fb.writeBits(30, player.getLocation().getX() << 14 | player.getLocation().getY() & 0x3fff | player.getLocation().getZ() << 28);

            for (int i = 1; i < 2048; i++) {
                if (i != player.getIndex()) {
                    fb.writeBits(18, player.gei.playerLocations[i]);
                }
            }
        }
        fb.writeLEShortA(player.getLocation().getPartX()).writeByte(0).writeByteA(1).writeShortA(player.getLocation().getPartY());
        int depth = Constants.REGION_SIZE[0] >> 4;
        for (int xCalc = (player.getLocation().getPartX() - depth) >> 3; xCalc <= (player.getLocation().getPartX() + depth) >> 3; xCalc++) {
            for (int yCalc = (player.getLocation().getPartY() - depth) >> 3; yCalc <= (player.getLocation().getPartY() + depth) >> 3; yCalc++) {
                int region = yCalc + (xCalc << 8);
                int[] key = Static.mapXTEA.getKey(region);
                if (key == null) {
                    key = new int[4];
                    logger.warn("No mapdata for region : " + region);
                }
                for (int i = 0; i < 4; i++) {
                    fb.writeInt(key[i]);
                }
                /*final int actualX = (xCalc << 3) << 3, actualY = (yCalc << 3) << 3;
                Static.engine.dispatchToMapWorker(new Runnable() {
                	public void run() {
                		Region.forAbsoluteCoordinates(actualX, actualY).load();
                	}
                });*/
            }
        }
        player.getSession().write(fb.toFrame());
        player.setMapRegionUpdatePosition(player.getLocation());
        Static.world.getGroundItemManager().refresh(player);
        Static.world.getObjectManager().refresh(player);
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendOnLogin(Player)
      */

    @Override
    public Protocol sendOnLogin(Player player) {
        sendMapRegion(player);
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendWindow(int)
      */

    @Override
    public Protocol sendWindow(Player player, int window) {
        FrameBuilder fb = new FrameBuilder(120, 3);
        fb.writeLEShort(window).writeByteC(0);
        player.getSession().write(fb.toFrame());
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendFixedScreen(Player)
      */

    @Override
    public Protocol sendFixedScreen(Player player) {
        sendWindow(player, 548);
        sendInterface(player, 751, 548, 67, true);
        sendInterface(player, 752, 548, 192, true);
        sendInterface(player, 754, 548, 16, true);
        sendInterface(player, 748, 548, 182, true);
        sendInterface(player, 749, 548, 184, true);
        sendInterface(player, 750, 548, 185, true);
        sendInterface(player, 747, 548, 187, true);
        sendInterface(player, 745, 548, 14, true);
        sendInterface(player, GameInterfaces.WILDY_SIGN);
        sendInterfaceShowConfig(player, 381, 0, !player.isInPVP());
        if (player.isOnLogin())
            sendInterface(player, 137, 752, 9, true);
        sendInterface(player, 884, 548, 202, true);
        sendAccessMask(player, -1, -1, 548, 128, 0, 2);
        sendAccessMask(player, -1, -1, 884, 11, 0, 2);
        sendAccessMask(player, -1, -1, 884, 12, 0, 2);
        sendAccessMask(player, -1, -1, 884, 13, 0, 2);
        sendInterface(player, 320, 548, 204, true);
        sendAccessMask(player, -1, -1, 548, 130, 0, 2);
        sendInterface(player, 190, 548, 205, true);
        sendAccessMask(player, -1, -1, 548, 131, 0, 2);
        sendAccessMask(player, 0, 300, 190, 18, 0, 14);
        sendAccessMask(player, 0, 11, 190, 15, 0, 2);
        sendInterface(player, 1056, 548, 203, true);
        sendAccessMask(player, -1, -1, 548, 129, 0, 2);
        sendInterface(player, 149, 548, 206, true);
        sendAccessMask(player, -1, -1, 548, 132, 0, 2);
        sendAccessMask(player, 0, 27, 149, 0, 69, 32142);
        sendAccessMask(player, 28, 55, 149, 0, 32, 0);
        sendInterface(player, 387, 548, 207, true);
        sendAccessMask(player, -1, -1, 548, 133, 0, 2);
        sendInterface(player, 271, 548, 208, true);
        sendAccessMask(player, -1, -1, 548, 134, 0, 2);
        sendAccessMask(player, 0, 30, 271, 8, 0, 2);
        sendInterface(player, 192, 548, 209, true);
        sendAccessMask(player, -1, -1, 548, 135, 0, 2);
        sendInterface(player, 550, 548, 211, true);
        sendAccessMask(player, -1, -1, 548, 99, 0, 2);
        sendInterface(player, 551, 548, 212, true);
        sendAccessMask(player, -1, -1, 548, 100, 0, 2);
        sendInterface(player, 589, 548, 213, true);
        sendAccessMask(player, -1, -1, 548, 101, 0, 2);
        sendInterface(player, 261, 548, 214, true);
        sendAccessMask(player, -1, -1, 548, 102, 0, 2);
        sendInterfaceScript(player, 1297);
        sendInterface(player, 464, 548, 215, true);
        sendAccessMask(player, -1, -1, 548, 103, 0, 2);
        sendInterface(player, 187, 548, 216, true);
        sendAccessMask(player, -1, -1, 548, 104, 0, 2);
        sendAccessMask(player, 0, 1953, 187, 1, 0, 26);
        sendAccessMask(player, 0, 11, 187, 9, 36, 6);
        sendAccessMask(player, 12, 23, 187, 9, 0, 4);
        sendAccessMask(player, 24, 24, 187, 9, 32, 0);
        sendInterface(player, 34, 548, 217, true);
        sendAccessMask(player, -1, -1, 548, 105, 0, 2);
        sendAccessMask(player, 0, 29, 34, 9, 40, 30);
        sendInterface(player, 182, 548, 220, true);
        sendInterfaceScript(player, 3336);
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendResizbleScreen(Player)
      */

    @Override
    public Protocol sendResizableScreen(Player player) {
        sendWindow(player, 746);
        sendInterface(player, 751, 746, 16, true);
        sendInterface(player, 752, 746, 69, true);
        sendInterface(player, 754, 746, 70, true);
        sendInterface(player, 748, 746, 174, true);
        sendInterface(player, 749, 746, 175, true);
        sendInterface(player, 750, 746, 176, true);
        sendInterface(player, 747, 746, 177, true);
        sendInterface(player, 745, 746, 12, true);
        Static.proto.sendInterface(player, GameInterfaces.WILDY_SIGN);
        Static.proto.sendInterfaceShowConfig(player, 381, 0, !player.isInPVP());
        if (player.isOnLogin())
            sendInterface(player, 137, 752, 9, true);
        sendInterface(player, 884, 746, 87, true);
        sendAccessMask(player, -1, -1, 746, 36, 0, 2);
        sendAccessMask(player, -1, -1, 884, 11, 0, 2);
        sendAccessMask(player, -1, -1, 884, 12, 0, 2);
        sendAccessMask(player, -1, -1, 884, 13, 0, 2);
        sendInterface(player, 320, 746, 89, true);
        sendAccessMask(player, -1, -1, 746, 38, 0, 2);
        sendInterface(player, 190, 746, 90, true);
        sendAccessMask(player, -1, -1, 746, 39, 0, 2);
        sendAccessMask(player, 0, 300, 190, 18, 0, 14);
        sendAccessMask(player, 0, 11, 190, 15, 0, 2);
        sendInterface(player, 1056, 746, 88, true);
        sendAccessMask(player, -1, -1, 746, 37, 0, 2);
        sendInterface(player, 149, 746, 91, true);
        sendAccessMask(player, -1, -1, 746, 40, 0, 2);
        sendAccessMask(player, 0, 27, 149, 0, 69, 32142);
        sendAccessMask(player, 28, 55, 149, 0, 32, 0);
        sendInterface(player, 387, 746, 92, true);
        sendAccessMask(player, -1, -1, 746, 41, 0, 2);
        sendInterface(player, 271, 746, 93, true);
        sendAccessMask(player, -1, -1, 746, 42, 0, 2);
        sendAccessMask(player, 0, 30, 271, 8, 0, 2);
        sendInterface(player, 192, 746, 94, true);
        sendAccessMask(player, -1, -1, 746, 43, 0, 2);
        sendInterface(player, 550, 746, 96, true);
        sendAccessMask(player, -1, -1, 746, 45, 0, 2);
        sendInterface(player, 551, 746, 97, true);
        sendAccessMask(player, -1, -1, 746, 46, 0, 2);
        sendInterface(player, 589, 746, 98, true);
        sendAccessMask(player, -1, -1, 746, 47, 0, 2);
        sendInterface(player, 261, 746, 99, true);
        sendAccessMask(player, -1, -1, 746, 48, 0, 2);
        sendInterfaceScript(player, 1297);
        sendInterface(player, 464, 746, 100, true);
        sendAccessMask(player, -1, -1, 746, 49, 0, 2);
        sendInterface(player, 187, 746, 101, true);
        sendAccessMask(player, -1, -1, 746, 50, 0, 2);
        sendAccessMask(player, 0, 1953, 187, 1, 0, 26);
        sendAccessMask(player, 0, 11, 187, 9, 36, 6);
        sendAccessMask(player, 12, 23, 187, 9, 0, 4);
        sendAccessMask(player, 24, 24, 187, 9, 32, 0);
        sendInterface(player, 34, 746, 102, true);
        sendAccessMask(player, -1, -1, 746, 51, 0, 2);
        sendAccessMask(player, 0, 29, 34, 9, 40, 30);
        sendInterface(player, 182, 746, 105, true);
        sendInterfaceScript(player, 3336);
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendInterface(int, int, int, boolean)
      */

    @Override
    public Protocol sendInterface(Player player, int id, int window, int location, boolean walkable) {
        FrameBuilder fb = new FrameBuilder(37, 7);
        fb.writeLEShort(id).writeLEInt(window << 16 | location).writeByte(walkable ? 1 : 0);
        player.getSession().write(fb.toFrame());
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendInterface(com.ziotic.network.GameInterface)
      */

    @Override
    public Protocol sendInterface(Player player, GameInterface gameInterface) {
        int normalLocation = player.getDisplayMode() == DisplayMode.FIXED ? 18 : 9;
        int location = gameInterface.getPos(player.getDisplayMode());
        if (location == normalLocation) {
            player.resetEvents();
        }
        if (location != normalLocation || player.getCurrentInterfaces().get(location) == null) {
            gameInterface.show(player);
            if (player.getDisplayMode() == DisplayMode.FIXED) {
                sendInterface(player, gameInterface.getId(), 548, location, gameInterface.isWalkable());
                player.getCurrentInterfaces().put(location, gameInterface);
            } else {
                sendInterface(player, gameInterface.getId(), 746, location, gameInterface.isWalkable());
                player.getCurrentInterfaces().put(location, gameInterface);
            }
        } else {
            sendMessage(player, "Please finish what you are currently doing.");
        }
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendMessage(Player, java.lang.String)
      */

    @Override
    public Protocol sendMessage(Player player, String message) {
        sendMessage(player, message, null, null, 0);
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendMessage(Player, java.lang.String, java.lang.String, int)
      */

    @Override
    public Protocol sendMessage(Player player, String message, String messageExtension, int req) {
        sendMessage(player, message, messageExtension, null, req);
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendMessage(Player, java.lang.String, java.lang.String, java.lang.String, int)
      */

    @Override
    public Protocol sendMessage(Player player, String message, String messageExtension, String messageExtension2, int req) {
        int flags = 0;
        if (messageExtension != null) {
            flags |= 0x1;
            if (messageExtension2 != null) {
                flags |= 0x2;
            }
        }
        FrameBuilder fb = new FrameBuilder(43, FrameType.VAR_BYTE, 128);
        fb.writeSmart(req).writeInt(0).writeByte(flags);
        if ((flags & 0x1) != 0) {
            fb.writeString(messageExtension);
            if ((flags & 0x2) != 0) {
                fb.writeString(messageExtension2);
            }
        }
        fb.writeString(message);
        player.getSession().write(fb.toFrame());
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendCloseInterface(Player)
      */

    @Override
    public Protocol sendCloseInterface(Player player) {
        int window = player.getDisplayMode() == DisplayMode.FIXED ? 548 : 746;
        for (Iterator<Map.Entry<Integer, GameInterface>> it = player.getCurrentInterfaces().entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, GameInterface> gameInterface = it.next();
            if (gameInterface.getValue().isWalkable()) {
                continue;
            }
            sendCloseInterface(player, window, gameInterface.getKey());
            gameInterface.getValue().close(player);
            it.remove();
        }
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendCloseInterface(Player, GameInterface)
      */

    @Override
    public Protocol sendCloseInterface(Player player, GameInterface gameInterface) {
        int window = player.getDisplayMode() == DisplayMode.FIXED ? 548 : 746;
        int pos = gameInterface.getPos(player.getDisplayMode());
        sendCloseInterface(player, window, pos);
        gameInterface.close(player);
        player.getCurrentInterfaces().remove(pos);
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendCloseInterface(Player, int, int)
      */

    @Override
    public Protocol sendCloseInterface(Player player, int window, int location) {
        FrameBuilder fb = new FrameBuilder(99, 4);
        fb.writeLEInt(window << 16 | location);
        player.getSession().write(fb.toFrame());
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#switchToFixedScreen(Player)
      */

    @Override
    public Protocol switchToFixedScreen(Player player) {
        sendFixedScreen(player);
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#switchToResizableScreen(Player)
      */

    @Override
    public Protocol switchToResizableScreen(Player player) {
        sendResizableScreen(player);
        return this;
    }
    /* (non-Javadoc)
      * @see Protocol#sendAccessMask(Player, int, int, int, int, int, int)
      */

    @Override
    public Protocol sendAccessMask(Player player, int range1, int range2, int interfaceId1, int childId1, int interfaceId2, int childId2) {
        FrameBuilder fb = new FrameBuilder(41, 12);
        fb.writeLEShortA(range2).writeLEShortA(range1).writeLEInt(interfaceId1 << 16 | childId1).writeInt2(interfaceId2 << 16 | childId2);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendInterfaceScript(Player player, Object... args) {
        FrameBuilder fb = new FrameBuilder(50, Frame.FrameType.VAR_SHORT, 512);
        StringBuilder types = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof Number) {
                types.append('i');
            } else {
                types.append('s');
            }
        }
        fb.writeString(types.toString());
        for (int i = args.length - 1; i >= 0; i--) {
            Object arg = args[i];
            if (arg instanceof Number) {
                fb.writeInt(((Number) arg).intValue());
            } else {
                fb.writeString(arg.toString());
            }
        }
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendPublicChat(Player player, Player p2, Chat chat) {
        int effects = chat.getColor() << 8 | chat.getEffect();
        // effects |= 0x8000; // flags something
        byte[] textBuffer = new byte[256];
        textBuffer[0] = (byte) chat.getText().length();
        int length = Text.huffmanCompress(chat.getText(), textBuffer, 1);
        FrameBuilder fb = new FrameBuilder(40, FrameType.VAR_BYTE, 64);
        fb.writeShort(p2.getIndex()).writeShort(effects).writeByte(p2.getRights().intValue()).write(textBuffer, 0, length + 1);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendItems(Player player, int position, boolean posBoolean, PossesedItem[] items) {
        FrameBuilder fb = new FrameBuilder(48, FrameType.VAR_SHORT, (items.length * 8) + 4);
        fb.writeShort(position).writeByte(posBoolean ? 1 : 0).writeShort(items.length);
        for (PossesedItem item : items) {
            int type = -1;
            int amount = 0;
            if (item != null) {
                type = item.getId();
                amount = item.getAmount();
            }

            if (amount >= 255) {
                fb.writeByteC(255).writeInt(amount);
            } else {
                fb.writeByteC(amount);
            }
            fb.writeShortA(type + 1);
        }
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendLocation(Player player, Tile location) {
        int x = location.getPartX() - (player.getMapRegionUpdatePosition().getPartX() - 6);
        int y = location.getPartY() - (player.getMapRegionUpdatePosition().getPartY() - 6);
        FrameBuilder fb = new FrameBuilder(46, 3);
        fb.writeByte(x).writeByteS(location.getZ()).writeByte(y);//TODO x and y might be switched
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendCreateGameObject(Player player, GameObject obj) {
        int x = obj.getLocation().getX() - (obj.getLocation().getPartX() << 3);
        int y = obj.getLocation().getY() - (obj.getLocation().getPartY() << 3);
        sendLocation(player, obj.getLocation());
        FrameBuilder fb = new FrameBuilder(101, 4);
        fb.writeLEShort(obj.getId());
        fb.writeByteC(obj.getType() << 2 | obj.getDirection());
        fb.writeByte((x & 0x7) << 4 | y & 0x7);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendAnimateObject(Player player, GameObject obj, int animation) {
        int x = obj.getLocation().getX() - (obj.getLocation().getPartX() << 3);
        int y = obj.getLocation().getY() - (obj.getLocation().getPartY() << 3);
        sendLocation(player, obj.getLocation());
        FrameBuilder fb = new FrameBuilder(91, 4);
        fb.writeLEShort(animation);
        fb.writeByteA(obj.getType() << 2 | obj.getDirection());
        fb.writeByteC((x & 0x7) << 4 | y & 0x7);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendDestroyGameObject(Player player, GameObject obj) {
        int x = obj.getLocation().getX() - (obj.getLocation().getPartX() << 3);
        int y = obj.getLocation().getY() - (obj.getLocation().getPartY() << 3);
        sendLocation(player, obj.getLocation());
        FrameBuilder fb = new FrameBuilder(44, 2);
        fb.writeByteC(obj.getType() << 2 | obj.getDirection());
        fb.writeByteS((x & 0x7) << 4 | y & 0x7);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendCreateGroundItem(Player player, GroundItem item) {
        int x = item.getLocation().getX() - (item.getLocation().getPartX() << 3);
        int y = item.getLocation().getY() - (item.getLocation().getPartY() << 3);

        sendLocation(player, item.getLocation());

        FrameBuilder fb = new FrameBuilder(56, 5);                   // TODO id and amt might be switched
        fb.writeLEShort(item.getId()).writeShortA(item.getAmount()).writeByteC((x & 0x7) << 4 | y & 0x7);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendDestroyGroundItem(Player player, GroundItem item) {
        int x = item.getLocation().getX() - (item.getLocation().getPartX() << 3);
        int y = item.getLocation().getY() - (item.getLocation().getPartY() << 3);

        sendLocation(player, item.getLocation());

        FrameBuilder fb = new FrameBuilder(63, 3);
        fb.writeByte((x & 0x7) << 4 | y & 0x7).writeLEShort(item.getId());
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendLevel(Player player, int skillId) {
        FrameBuilder fb = new FrameBuilder(57, 6);
        if (skillId == Levels.PRAYER)
            fb.writeByteS((byte) Math.ceil(player.getLevels().getCurrentPrayer())).writeInt((int) player.getLevels().getExperience(skillId)).writeByteA(skillId);
        else
            fb.writeByteS(player.getLevels().getCurrentLevel(skillId)).writeInt((int) player.getLevels().getExperience(skillId)).writeByteA(skillId);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendConfig(Player player, int id, int val) {
        FrameBuilder fb;
        if (val < Byte.MIN_VALUE || val > Byte.MAX_VALUE) {
            fb = new FrameBuilder(107, 6).writeLEInt(val).writeLEShortA(id);
        } else {
            fb = new FrameBuilder(89, 3).writeByteA(val).writeShort(id);
        }
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendRunEnergy(Player player) {
        FrameBuilder fb = new FrameBuilder(80, 1);
        fb.writeByte(player.getRunningEnergy());
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendExitToLogin(Player player) {
        player.getSession().write(new FrameBuilder(26, 0).toFrame()).addListener(IoFutureListener.CLOSE);
        return this;
    }

    @Override
    public Protocol sendExitToLobby(final Player player) {
        player.getSession().write(new FrameBuilder(60, 0).toFrame()).addListener(IoFutureListener.CLOSE);
        return this;
    }

    @Override
    public Protocol sendPlayerOption(Player player, String option, int index, boolean onTop) {
        FrameBuilder fb = new FrameBuilder(17, FrameType.VAR_BYTE, 64);
        fb.writeLEShortA(-1).writeByte(index).writeString(option).writeByte(onTop ? 1 : 0);
        player.getSession().write(fb.toFrame());
        return this;
    }

    /* (non-Javadoc)
      * @see Protocol#requestAmountInput()
      */
    @Override
    public Protocol requestAmountInput(Player player) {
        sendInterfaceScript(player, 108, "Enter amount:");
        return this;
    }

    /* (non-Javadoc)
      * @see Protocol#requestTextInput(java.lang.String)
      */
    @Override
    public Protocol requestTextInput(Player player, String request) {
        sendInterfaceScript(player, 109, request);
        return this;
    }

    @Override
    public Protocol sendString(Player player, int interfaceId, int childId, String string) {
        FrameBuilder fb = new FrameBuilder(53, FrameType.VAR_SHORT, 128);
        fb.writeInt(interfaceId << 16 | childId);
        fb.writeString(string);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendClanChatMessage(Player player, String clan, String sender, int rights, String message, long id) {
        FrameBuilder fb = new FrameBuilder(39, FrameType.VAR_BYTE, 64);
        fb.writeByte(0);
        fb.writeString(sender);
        fb.writeLong(Text.stringToLong(clan));
        fb.writeShort((int) (id >> 32)).writeTriByte((int) (id - ((id >> 32) << 32)));
        fb.writeByte(rights);
        byte[] textBuffer = new byte[256];
        textBuffer[0] = (byte) message.length();
        int length = Text.huffmanCompress(message, textBuffer, 1);
        fb.write(textBuffer, 0, length + 1);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendInterfaceVariable(Player player, int id, int val) {
        FrameBuilder fb;
        if (val < Byte.MIN_VALUE || val > Byte.MAX_VALUE) {
            fb = new FrameBuilder(119, 6).writeInt1(val).writeLEShortA(id);
        } else {
            fb = new FrameBuilder(24, 3).writeShortA(id).writeByteC(val);
        }
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendNPCHead(Player player, int interfaceId, int childId, int npcId) {
        FrameBuilder fb = new FrameBuilder(121, 6);
        fb.writeShort(npcId).writeLEInt(interfaceId << 16 | childId);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendPlayerHead(Player player, int interfaceId, int childId) {
        FrameBuilder fb = new FrameBuilder(88, 4);
        fb.writeInt(interfaceId << 16 | childId);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendInterfaceAnimation(Player player, int interfaceId, int childId, int animId) {
        FrameBuilder fb = new FrameBuilder(79, 6);
        fb.writeLEInt(interfaceId << 16 | childId).writeLEShortA(animId);
        player.getSession().write(fb.toFrame());
        return this;
    }

    public Protocol sendChatboxInterface(Player player, int interfaceId) {
        sendInterface(player, interfaceId, 752, 13, false);
        return this;
    }

    public Protocol sendCloseChatboxInterface(Player player) {
        sendCloseInterface(player, 752, 13);
        return this;
    }

    @Override
    public Protocol sendInterfaceConfig(Player player, int interfaceId, int value) {
        FrameBuilder fb = new FrameBuilder(55, 3);
        fb.writeByteA(value).writeShort(interfaceId);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendInterfaceShowConfig(Player player, int interfaceId, int childId, boolean hidden) {
        FrameBuilder fb = new FrameBuilder(92, 5);
        fb.writeByteA(hidden ? 1 : 0).writeInt1(interfaceId << 16 | childId);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendPing(Player player) {
        player.getSession().write(new FrameBuilder(109, 0).toFrame());
        return this;
    }

    @Override
    public Protocol sendProjectile(Player player, Entity receiver, int projectileId, Tile start, Tile end,
                                   int startHeight, int endHeight, int slowness, int delay, int curve, int startDistanceOffset, int creatorSize) {
        FrameBuilder fb = new FrameBuilder(94, Frame.FrameType.VAR_SHORT, 64);
        int distance = start.distance(end);
        int duration = delay + slowness + distance * 5;
        int x = start.getPartX() - (player.getMapRegionUpdatePosition().getPartX() - 6);
        int y = start.getPartY() - (player.getMapRegionUpdatePosition().getPartY() - 6);
        fb.writeByteS(x)
                .writeByteC(y)
                .writeByteS(player.getZ());
        fb.writeByte(2); // projectile subopcode
        x = start.getX() - (start.getPartX() << 3);
        y = start.getY() - (start.getPartY() << 3);
        fb.writeByte((x & 0x7) << 3 | y & 0x7);
        fb.writeByte(end.getX() - start.getX())
                .writeByte(end.getY() - start.getY());
        fb.writeShort(receiver != null ? (receiver instanceof Player ? -(receiver.getIndex() + 1) : receiver.getIndex() + 1) : 0);
        fb.writeShort(projectileId);
        fb.writeByte(startHeight);
        fb.writeByte(endHeight);
        fb.writeShort(delay);
        fb.writeShort(duration);
        fb.writeByte(curve);
        fb.writeShort(creatorSize * 64 + startDistanceOffset * 64);
        player.getSession().write(fb.toFrame());
        return this;
    }

    @Override
    public Protocol sendSpecialString(Player player, int id, String string) {
        FrameBuilder fb = new FrameBuilder(100, FrameType.VAR_BYTE, 128);
        fb.writeLEShort(id).writeString(string);
        player.getSession().write(fb.toFrame());
        return this;
    }
}
