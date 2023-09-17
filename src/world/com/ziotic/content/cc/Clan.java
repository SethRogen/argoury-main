package com.ziotic.content.cc;

import com.ziotic.Static;
import com.ziotic.content.grotto.War;
import com.ziotic.logic.player.Player;
import com.ziotic.logic.player.PlayerType;
import com.ziotic.utility.Streams;
import com.ziotic.utility.Text;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lazaro
 */
public class Clan {
    private static final int MAGIC_NUMBER = 0x9E3779B9;

    public static enum Rank {
        ANYONE(-1, "Anyone"), FRIEND(0, "Friends"), RECRUIT(1, "Recruit+"),
        CORPORAL(2, "Corporal+"), SERGEANT(3, "Sergeant+"), LIEUTENANT(4, "Lieutenant+"),
        CAPTAIN(5, "Captain+"), GENERAL(6, "General+"), OWNER(7, "Only me"), NO_ONE(8, "No-one");

        public static Rank forValue(int value) {
            switch (value) {
                case -1:
                    return ANYONE;
                case 0:
                    return FRIEND;
                case 1:
                    return RECRUIT;
                case 2:
                    return CORPORAL;
                case 3:
                    return SERGEANT;
                case 4:
                    return LIEUTENANT;
                case 5:
                    return CAPTAIN;
                case 6:
                    return GENERAL;
                case 7:
                    return OWNER;
                case 8:
                    return NO_ONE;
            }
            return null;
        }

        private int value;
        private String string;

        private Rank(int value, String string) {
            this.value = value;
            this.string = string;
        }

        public int intValue() {
            return value;
        }

        public String getString() {
            return string;
        }
    }

    private String name = null;
    private String owner = null;

    private boolean enabled = false;

    private Rank enterRequirement = Rank.FRIEND;
    private Rank talkRequirement = Rank.ANYONE;
    private Rank kickRequirement = Rank.OWNER;
    private Rank lootShareRequirement = Rank.NO_ONE;

    private Map<String, PlayerType> players = new HashMap<String, PlayerType>();
    private Map<String, Rank> ranks = new HashMap<String, Rank>();

    private boolean needsUpdate = false;
    private long lastUpdate = 0;
    private Clan versing;
    private War war;

    public Clan() {
        this(null);
    }

    public Clan(String owner) {
        this.owner = owner;
    }

    public void load(byte[] data) throws IOException {
        DataInputStream in = new DataInputStream(/*new GZIPInputStream(*/new ByteArrayInputStream(data)/*)*/);

        int mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid header key");
        }

        name = Streams.readString(in);
        owner = Streams.readString(in);

        enabled = (in.readByte() & 0xff) == 1;

        enterRequirement = Rank.forValue(in.readByte());
        talkRequirement = Rank.forValue(in.readByte());
        kickRequirement = Rank.forValue(in.readByte());
        lootShareRequirement = Rank.forValue(in.readByte());

        int rankCount = in.readByte() & 0xff;
        for (int i = 0; i < rankCount; i++) {
            ranks.put(Streams.readString(in), Rank.forValue(in.readByte()));
        }

        mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid footer key");
        }
    }

    public byte[] toByteArray() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(/*new GZIPOutputStream(*/baos/*)*/);

            out.writeInt(MAGIC_NUMBER); // header key

            Streams.writeString(name, out);
            Streams.writeString(owner, out);

            out.writeByte(enabled ? 1 : 0);

            out.writeByte(enterRequirement.intValue());
            out.writeByte(talkRequirement.intValue());
            out.writeByte(kickRequirement.intValue());
            out.writeByte(lootShareRequirement.intValue());

            out.writeByte(ranks.size());
            for (Map.Entry<String, Rank> rank : ranks.entrySet()) {
                Streams.writeString(rank.getKey(), out);
                out.writeByte(rank.getValue().intValue());
            }

            out.writeInt(MAGIC_NUMBER); // footer key

            out.close(); // flush data

            return baos.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }

    public Map<String, PlayerType> getPlayers() {
        return players;
    }

    public Map<String, Rank> getRanks() {
        return ranks;
    }

    /**
     * @param ranks the ranks to set
     */
    public void setRanks(Map<String, Rank> ranks) {
        this.ranks = ranks;
    }

    public void refresh() {
        for (PlayerType player : players.values()) {
            if (player instanceof Player) {
                Static.proto.sendClan((Player) player);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Text.formatNameForDisplay(name);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Rank getEnterRequirement() {
        return enterRequirement;
    }

    public void setEnterRequirement(Rank enterRequirement) {
        this.enterRequirement = enterRequirement;
    }

    public Rank getTalkRequirement() {
        return talkRequirement;
    }

    public void setTalkRequirement(Rank talkRequirement) {
        this.talkRequirement = talkRequirement;
    }

    public Rank getKickRequirement() {
        return kickRequirement;
    }

    public void setKickRequirement(Rank kickRequirement) {
        this.kickRequirement = kickRequirement;
    }

    public Rank getLootShareRequirement() {
        return lootShareRequirement;
    }

    public void setLootShareRequirement(Rank lootShareRequirement) {
        this.lootShareRequirement = lootShareRequirement;
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Rank getRank(String name) {
        return getRank(name, Rank.ANYONE);
    }

    public Rank getRank(String name, Rank defaultRank) {
        if (name.equals(owner)) {
            return Rank.OWNER;
        }
        Rank rank = ranks.get(name);
        if (rank == null) {
            return defaultRank;
        }
        return rank;
    }

    public void setRank(String name, Rank rank) {
        ranks.put(name, rank);
    }

    public void setVersing(Clan versing) {
        this.versing = versing;
    }

    public Clan getVersing() {
        return versing;
    }

    public void setWar(War war) {
        this.war = war;
    }

    public War getWar() {
        return war;
    }
}
