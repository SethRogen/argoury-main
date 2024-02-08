package com.runescape.content.cc;

import com.runescape.Constants;
import com.runescape.Static;
import com.runescape.content.cc.Clan.Rank;
import com.runescape.content.handler.ActionHandlerSystem;
import com.runescape.content.handler.ButtonHandler;
import com.runescape.engine.event.RecurringEvent;
import com.runescape.engine.event.Event.ExecutorType;
import com.runescape.engine.login.LoginResponse;
import com.runescape.link.WorldClientSession;
import com.runescape.logic.player.Player;
import com.runescape.logic.player.PlayerSave;
import com.runescape.logic.player.PlayerType;
import com.runescape.logic.player.RemotePlayer;
import com.runescape.logic.utility.GameInterface;
import com.runescape.logic.utility.NodeRunnable;
import com.runescape.utility.Logging;
import com.runescape.utility.Text;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Lazaro
 */
public class ClanManager implements ButtonHandler {
    private static final Logger logger = Logging.log();

    private static Map<String, Clan> clans = new HashMap<String, Clan>();
    private static List<Clan> clanUpdateList = new LinkedList<Clan>();

    public static final GameInterface CLAN_SETUP = new GameInterface(590, new NodeRunnable<Player>() {
        @Override
        public void run(Player player) {
            refreshClanSetup(player);
        }
    });

    private static RecurringEvent CLAN_UPDATE_FLUSHER = new RecurringEvent(10000, ExecutorType.PARALLEL_LOGIC) {
        @Override
        public void run() {
            synchronized (clanUpdateList) {
                for (Iterator<Clan> it = clanUpdateList.iterator(); it.hasNext(); ) {
                    Clan clan = it.next();
                    if (clan.needsUpdate() && System.currentTimeMillis() - clan.getLastUpdate() >= 50000) {
                        flushClan(clan);
                        clan.setNeedsUpdate(false);
                        it.remove();
                    }
                }
            }
        }
    };

    public static Map<String, Clan> getClans() {
        return clans;
    }

    /**
     * Sends a request to the link server to try to attempt to join a channel.
     * <p/>
     * Note: Lobby/Game server method.
     *
     * @param player
     * @param owner
     */
    public static void joinChannel(final Player player, String owner) {
        owner = Text.formatNameForProtocol(owner);
        final String fOwner = owner;
        Static.proto.sendMessage(player, "Attempting to join channel...");

        Static.engine.dispatchToLinkWorker(new Runnable() {
            @Override
            public void run() {
                WorldClientSession session = null;
                try {
                    session = Static.world.getWCSPool().acquire();

                    int resp = session.joinClan(player, fOwner);
                    if (resp == 1) {
                        Clan clan = null;
                        for (int i = 0; i < 5; i++) {
                            clan = clans.get(fOwner);
                            if (clan != null) {
                                break;
                            }

                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                            }
                        }
                        if (clan == null) {
                            Static.proto.sendMessage(player, "An unexpected error occured while trying to join this channel.");
                            return;
                        }

                        player.setClan(clan);
                        if (fOwner.equals(player.getProtocolName())) {
                            player.setOwnClan(clan);
                        }

                        clan.getPlayers().put(player.getProtocolName(), player);
                        clan.refresh();

                        Static.proto.sendMessage(player, "Now talking in clan channel " + clan.getName());
                        Static.proto.sendMessage(player, "To talk, start each line of chat with the / symbol.");
                    } else {
                        switch (resp) {
                            case 2:
                                Static.proto.sendMessage(player, "An unexpected error occured while trying to join this channel.");
                                break;
                            case 3:
                                Static.proto.sendMessage(player, "The channel you tried to join does not exist.");
                                break;
                            case 4:
                                Static.proto.sendMessage(player, "You do not have a high enough rank to enter this clan chat.");
                                break;
                        }
                    }
                } finally {
                    if (session != null) {
                        Static.world.getWCSPool().release(session);
                    }
                }
            }
        });
    }

    private static void refreshClanSetup(Player player) {
        Clan clan = player.getOwnClan();
        if (clan == null) {
            Static.proto.sendString(player, 590, 22, "Chat disabled");
            Static.proto.sendString(player, 590, 23, Rank.FRIEND.getString());
            Static.proto.sendString(player, 590, 24, Rank.ANYONE.getString());
            Static.proto.sendString(player, 590, 25, Rank.OWNER.getString());
            Static.proto.sendString(player, 590, 26, Rank.NO_ONE.getString());
        } else {
            if (clan.isEnabled()) {
                Static.proto.sendString(player, 590, 22, Text.formatNameForDisplay(player.getOwnClan().getName()));
            } else {
                Static.proto.sendString(player, 590, 22, "Chat disabled");
            }
            Static.proto.sendString(player, 590, 23, player.getOwnClan().getEnterRequirement().getString());
            Static.proto.sendString(player, 590, 24, player.getOwnClan().getTalkRequirement().getString());
            Static.proto.sendString(player, 590, 25, player.getOwnClan().getKickRequirement().getString());
            Static.proto.sendString(player, 590, 26, player.getOwnClan().getLootShareRequirement().getString());
        }
    }

    public static void leaveChannel(final Player player) {
        unregisterPlayer(player.getProtocolName(), player.getClan().getOwner());
        if (!player.isDestroyed()) {
            player.setClan(null);
            Static.proto.sendClan(player);
            Static.proto.sendMessage(player, "You have left the channel.");
        }
    }

    /**
     * Attempts to join a specified channel.
     * <p/>
     * Note: Link server method.
     *
     * @param userName
     * @param owner
     * @return
     */
    public static int attemptJoinChannel(String userName, String owner) {
        PlayerType player = Static.currentLink().findPlayer(userName);
        if (player == null) {
            return 2;
        }

        PlayerSave playerSave = new PlayerSave();
        if (Static.currentLink().getDBLoader().loadPlayer(userName, null, playerSave) != LoginResponse.LOGIN) {
            return 2;
        }

        PlayerSave ownerSave = new PlayerSave();
        if (Static.currentLink().getDBLoader().loadPlayer(owner, null, ownerSave) != LoginResponse.LOGIN) {
            return 3;
        }

        if (ownerSave.ownClan == null) {
            return 3;
        }

        if (!ownerSave.ownClan.isEnabled()) {
            return 3;
        }

        if (ownerSave.ownClan.getEnterRequirement().intValue() > ownerSave.ownClan.getRank(userName).intValue()) {
            return 4;
        }

        Clan clan = clans.get(owner);
        if (clan == null) {
            clan = new Clan(owner);
            Static.currentLink().getDBLoader().loadClan(owner, clan);
            clans.put(owner, clan);

            Static.currentLink().registerClan(clan);
        }
        clan.getPlayers().put(userName, player);
        Static.currentLink().registerPlayerToClan((RemotePlayer) player, clan);
        return 1;
    }

    public static void registerPlayer(RemotePlayer player, String owner) {
        Clan clan = clans.get(owner);
        if (clan != null) {
            player.setClanOwner(clan.getOwner());

            clan.getPlayers().put(player.getProtocolName(), player);
            clan.refresh();
        }
    }

    /**
     * Unregisters a player from a clan.
     * <p/>
     * Note: Link/Lobby/Game server method.
     *
     * @param name
     * @param owner
     */
    public static void unregisterPlayer(String name, String owner) {
        Clan clan = clans.get(owner);
        if (clan != null) {
            PlayerType player = clan.getPlayers().remove(name);
            if (player != null) {
                if (!Static.isLink()) {
                    clan.refresh();

                    if (player instanceof Player) {
                        WorldClientSession session = null;
                        try {
                            session = Static.world.getWCSPool().acquire();

                            session.leaveClan((Player) player, owner);
                        } finally {
                            if (session != null) {
                                Static.world.getWCSPool().release(session);
                            }
                        }
                    }
                } else {
                    Static.currentLink().unregisterPlayerFromClan((RemotePlayer) player, clan);
                    if (clan.getPlayers().size() == 0) {
                        Static.currentLink().unregisterClan(clan);
                        unregisterClan(clan.getOwner());
                    }
                }
            }
        }
    }

    public static void registerClan(Clan clan) {
        clans.put(clan.getOwner(), clan);
    }

    public static void unregisterClan(String owner) {
        clans.remove(owner);
    }

    public static void changeClanPrefix(Player player, String prefix) {
        Clan clan;
        if (player.getOwnClan() == null) {
            clan = new Clan(player.getProtocolName());
            player.setOwnClan(clan);
        } else {
            clan = player.getOwnClan();
        }
        clan.setName(prefix);
        clan.setEnabled(true);
        saveClan(player, clan);
    }

    private static void saveClan(Player player, Clan clan) {
        refreshClanSetup(player);

        if (!clan.needsUpdate()) {
            Static.proto.sendMessage(player, "Changes will take effect on your clan in the next 60 seconds.");
            clan.setLastUpdate(System.currentTimeMillis());
            clan.setNeedsUpdate(true);
            synchronized (clanUpdateList) {
                clanUpdateList.add(clan);
            }
        }
    }

    private static void flushClan(final Clan clan) {
        Static.engine.dispatchToLinkWorker(new Runnable() {
            @Override
            public void run() {
                WorldClientSession session = null;
                try {
                    session = Static.world.getWCSPool().acquire();

                    session.saveClan(clan);
                } finally {
                    if (session != null) {
                        Static.world.getWCSPool().release(session);
                    }
                }
            }
        });
        clan.refresh();
    }

    public static void saveClan2(Clan clanRef) {
        Clan clan = clans.get(clanRef.getOwner());
        if (clan != null) {
            updateClan(clanRef.getOwner(), clanRef);

            Static.currentLink().broadcastClanUpdate(clanRef);
        }

        Static.currentLink().getDBLoader().saveClan(clanRef);
    }

    public static boolean updateClan(String owner, Clan clanRef) {
        Clan clan = clans.get(owner);
        if (clan != null) {
            clan.setName(clanRef.getName());
            clan.setEnterRequirement(clanRef.getEnterRequirement());
            clan.setTalkRequirement(clanRef.getTalkRequirement());
            clan.setKickRequirement(clanRef.getKickRequirement());
            clan.setLootShareRequirement(clanRef.getLootShareRequirement());
            clan.setRanks(clanRef.getRanks());

            if (!Static.isLink())
                clan.refresh();
            return true;
        }
        return false;
    }

    @Override
    public void load(ActionHandlerSystem system) throws Exception {
        system.registerButtonHandler(new int[]{589, 590}, this);
        Static.engine.submit(CLAN_UPDATE_FLUSHER);
    }

    @Override
    public boolean explicitlyForMembers() {
        return false;
    }

    @Override
    public void handleButton(Player player, int opcode, int interfaceId, int b, int b2, int b3) {
        switch (interfaceId) {
            case 589:
                switch (b) {
                    case 16:
                        Static.proto.sendInterface(player, CLAN_SETUP);
                        break;
                    default:
                        logger.debug("Unhandled button [interface=" + interfaceId + ", button=" + b + ", button2=" + b2 + ", button3=" + b3 + ", opcode=" + opcode + "]");
                        break;
                }
                break;
            case 590:
                switch (b) {
                    case 22:
                        switch (opcode) {
                            case 1:
                                Static.proto.requestTextInput(player, "Enter chat prefix:");
                                player.getAttributes().set("inputId", Constants.Inputs.CC_PREFIX);
                                break;
                            case 2:
                                player.getOwnClan().setEnabled(false);
                                saveClan(player, player.getOwnClan());
                                break;
                            default:
                                logger.debug("Unhandled button [interface=" + interfaceId + ", button=" + b + ", button2=" + b2 + ", button3=" + b3 + ", opcode=" + opcode + "]");
                                break;
                        }
                        break;
                    case 23: // Toggle enter requirement
                    case 24: // Toggle talk requirement
                    case 25: // Toggle kick requirement
                    case 26: // Toggle loot share requirement
                        Rank r = null;
                        switch (opcode) {
                            case 1:
                                r = Rank.ANYONE;
                                break;
                            case 2:
                                r = Rank.FRIEND;
                                break;
                            case 3:
                                r = Rank.RECRUIT;
                                break;
                            case 4:
                                r = Rank.CORPORAL;
                                break;
                            case 5:
                                r = Rank.SERGEANT;
                                break;
                            case 6:
                                r = Rank.LIEUTENANT;
                                break;
                            case 7:
                                r = Rank.CAPTAIN;
                                break;
                            case 8:
                                r = Rank.GENERAL;
                                break;
                            case 9:
                                r = Rank.OWNER;
                                break;
                            default:
                                logger.debug("Unhandled clan chat rank opcode : " + opcode);
                                break;
                        }
                        if (r != null) {
                            if (b == 23) {
                                player.getOwnClan().setEnterRequirement(r);
                            } else if (b == 24) {
                                player.getOwnClan().setTalkRequirement(r);
                            } else if (b == 25) {
                                player.getOwnClan().setKickRequirement(r);
                            } else if (b == 26) {
                                if (r == Rank.ANYONE) {
                                    r = Rank.NO_ONE;
                                }
                                player.getOwnClan().setLootShareRequirement(r);
                            }
                            saveClan(player, player.getOwnClan());
                        }
                        break;
                    default:
                        logger.debug("Unhandled button [interface=" + interfaceId + ", button=" + b + ", button2=" + b2 + ", button3=" + b3 + ", opcode=" + opcode + "]");
                        break;
                }
                break;
        }
    }

    public static Clan getClan(String owner) {
        return clans.get(owner);
    }

    public static void receiveClanMessage(String owner, String sender, int rights, String message) {
        Clan clan = clans.get(owner);
        if (clan == null) {
            return;
        }

        sender = Text.formatNameForDisplay(sender);

        long id = (long) (Math.random() * 0xFFFFFFFFFFL);
        for (PlayerType player : clan.getPlayers().values()) {
            if (player instanceof Player) {
                Static.proto.sendClanChatMessage((Player) player, clan.getName(), sender, rights, message, id);
            }
        }
    }

    public static void sendMessage(final Player player, final String message) {
        Static.engine.dispatchToLinkWorker(new Runnable() {
            @Override
            public void run() {
                WorldClientSession session = null;
                try {
                    session = Static.world.getWCSPool().acquire();

                    session.sendClanMessage(player, player.getClan().getOwner(), message);
                } finally {
                    if (session != null) {
                        Static.world.getWCSPool().release(session);
                    }
                }
            }
        });
    }

    public static void kickPlayer(Player player, String name) {
        name = Text.formatNameForProtocol(name);
        Clan clan = player.getClan();
        if (clan.getRank(player.getProtocolName()).intValue() >= clan.getKickRequirement().intValue()) {
            PlayerType p2 = clan.getPlayers().get(name);
            if (p2 == null) {
                Static.proto.sendMessage(player, "Your request to kick/ban this user was unsuccessful.");
                return;
            }

            unregisterPlayer(p2.getProtocolName(), clan.getOwner());
            if (p2 instanceof Player) {
                Player pp2 = (Player) p2;

                pp2.setClan(null);
                Static.proto.sendClan(pp2);
                Static.proto.sendMessage(pp2, "You have been kicked from the channel.");
            } else {
                WorldClientSession session = null;
                try {
                    session = Static.world.getWCSPool().acquire();

                    session.kickPlayerFromClan(p2.getProtocolName());
                } finally {
                    if (session != null) {
                        Static.world.getWCSPool().release(session);
                    }
                }
            }
        }
    }

    public static void kickPlayer2(String name) {
        PlayerType player = Static.world.findPlayer(name);
        if (!(player instanceof Player)) {
            return;
        }

        Player p = (Player) player;
        p.setClan(null);
        Static.proto.sendClan(p);
        Static.proto.sendMessage(p, "You have been kicked from the channel.");
    }

    public static void rankPlayer(Player player, String name, Rank rank) {
        Clan clan;
        if (player.getOwnClan() == null) {
            clan = new Clan(player.getProtocolName());
            player.setOwnClan(clan);
        } else {
            clan = player.getOwnClan();
        }
        name = Text.formatNameForProtocol(name);
        clan.setRank(name, rank);

        player.getFriends().sendFriend(name);
        saveClan(player, clan);
    }
}
