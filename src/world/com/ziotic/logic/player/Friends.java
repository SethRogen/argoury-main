package com.ziotic.logic.player;

import com.ziotic.Static;
import com.ziotic.link.WorldClientSession;
import com.ziotic.utility.Text;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Lazaro
 */
public class Friends {
    private Player player;
    private Set<String> friendsList = new HashSet<String>();
    private Set<String> ignoreList = new HashSet<String>();
    private int privateChatSetting = 0;
    private int privateChatColor = 0;

    public Friends(Player player) {
        this.player = player;
    }

    public void initiate() {
        Static.proto.sendPrivateChatSetting(player, privateChatSetting);
        Static.proto.sendConfig(player, 287, privateChatColor);
        Static.proto.sendFriends(player, friendsList.toArray(new String[0]));
        Static.proto.sendIgnores(player);
    }

    public void addFriend(String friend) {
        friend = Text.formatNameForProtocol(friend);
        friendsList.add(friend);
        sendFriend(friend);
    }

    public void removeFriend(String friend) {
        friendsList.remove(Text.formatNameForProtocol(friend));
    }

    public void addIgnore(String ignore) {
        ignore = Text.formatNameForProtocol(ignore);
        ignoreList.add(ignore);
        Static.proto.sendIgnores(player);
    }

    public void removeIgnore(String ignore) {
        ignoreList.remove(Text.formatNameForProtocol(ignore));
    }

    public Set<String> getFriendsList() {
        return friendsList;
    }

    public Set<String> getIgnoreList() {
        return ignoreList;
    }

    public int getPrivateChatSetting() {
        return privateChatSetting;
    }

    public void setPrivateChatSetting(int privateChatSetting) {
        this.privateChatSetting = privateChatSetting;
    }

    public int getPrivateChatColor() {
        return privateChatColor;
    }

    public void setPrivateChatColor(int privateChatColor) {
        this.privateChatColor = privateChatColor;
    }

    public void notify(PlayerType friend) {
        if (friendsList.contains(friend.getProtocolName())) {
            Static.proto.sendFriends(player, friend.getProtocolName());
        }
    }

    public void sendFriend(String name) {
        Static.proto.sendFriends(player, name);
    }

    public long nextMessageId() {
        return (long) (Math.random() * 0xFFFFFFFFFFL);
    }

    public void receivePM(String sender, int rights, String message) {
        Static.proto.sendIncommingPM(player, Text.formatNameForDisplay(sender), rights, message, nextMessageId());
    }

    public void sendPM(String recipient, final String message) {
        if (player.isMuted()) {
            Static.proto.sendMessage(player, "You are muted. To appeal your mute go to the discord server or forums.");
            return;
        }
        recipient = Text.formatNameForProtocol(recipient);
        PlayerType friend = Static.world.findPlayer(recipient);
        if (friend instanceof Player) {
            ((Player) friend).getFriends().receivePM(player.getName(), player.getRights().intValue(), message);
        } else {
            final String fRecipient = recipient;
            Static.engine.dispatchToLinkWorker(new Runnable() {
                @Override
                public void run() {
                    WorldClientSession session = null;
                    try {
                        session = Static.world.getWCSPool().acquire();

                        session.sendPM(player, fRecipient, message);
                    } finally {
                        if (session != null) {
                            Static.world.getWCSPool().release(session);
                        }
                    }
                }
            });
        }
        Static.proto.sendOutgoingPM(player, Text.formatNameForDisplay(recipient), message);
    }
}
