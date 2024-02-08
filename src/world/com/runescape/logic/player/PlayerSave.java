package com.runescape.logic.player;

import com.runescape.content.cc.Clan;
import com.runescape.content.combat.Magic;
import com.runescape.engine.tick.Tick;
import com.runescape.logic.item.PossesedItem;
import com.runescape.utility.Streams;

import java.io.*;

/**
 * @author Lazaro
 */
public class PlayerSave {
    private static final int MAGIC_NUMBER = 0x9E3779B9;

    public int userId = 0;

    public String email = null;
    public int unreadMessages = 0;
    public long subscriptionEnd = 0;

    public long lastLoggedIn = 0;

    public String[] lastIPs = new String[3];

    public int rights = 0;

    public int x, y, z;

    public boolean runToggled = false;
    public int runEnergy = 100;

    public short[] inv = new short[28];
    public int[] invN = new int[28];

    public short[] equip = new short[14];
    public int[] equipN = new int[14];

    public int[][] bank = new int[0][3];

    public byte[] level = new byte[Levels.SKILL_COUNT];
    public double[] exp = new double[Levels.SKILL_COUNT];
    public int xpCounter = 0;
    public boolean autoRetaliate = false;
    public int attackStyle = 0;
    public boolean prayerBook = false;
    public byte[] quickPrayers = new byte[30];
    public int spellBook = 193;
    public int specialEnergy = 100;
    public int[] looks = new int[7];
    public int[] colours = new int[5];
    public boolean isMuted = false;
    public boolean isSkulled = false;
    public int skullTimer = 0;

    public String[] friends = new String[0];
    public String[] ignores = new String[0];
    public int privateChatSetting = 0;
    public int privateChatColor = 0;

    public Clan ownClan = null;

    public String lastClan = null;

    public void load(Player player) {
        userId = player.getUserId();
        lastIPs = player.getLastIPs();
        rights = player.getRights().intValue();
        isMuted = player.isMuted();
        if (player.inGame()) {
            x = player.getX();
            y = player.getY();
            z = player.getZ();

            runToggled = player.isRunning();
            runEnergy = player.getRunningEnergy();

            for (int i = 0; i < 28; i++) {
                PossesedItem item = player.getInventory().get(i);
                if (item == null) {
                    inv[i] = -1;
                } else {
                    inv[i] = (short) item.getId();
                    invN[i] = item.getAmount();
                }
            }
            for (int i = 0; i < 14; i++) {
                PossesedItem item = player.getEquipment().get(i);
                if (item == null) {
                    equip[i] = -1;
                } else {
                    equip[i] = (short) item.getId();
                    equipN[i] = item.getAmount();
                }
            }
            bank = new int[player.getBank().size()][3];
            int index = 0;
            for (PossesedItem item : player.getBank().array()) {
                if (item != null) {
                    int[] entry = bank[index];
                    entry[0] = item.getId();
                    entry[1] = item.getAmount();
                    entry[2] = player.getBank().getTab(item);
                    index++;
                }
            }
            for (int i = 0; i < level.length; i++) {
                level[i] = (byte) player.getLevels().getCurrentLevel(i);
                exp[i] = player.getLevels().getExperience(i);
            }
            xpCounter = (int) player.getLevels().getXPGained();
            autoRetaliate = player.getCombat().autoRetaliate();
            attackStyle = player.getCombat().getWeapon().getIndex();
            prayerBook = player.getPrayerManager().isCurses();
            quickPrayers = player.getPrayerManager().getQuickPrayers();
            spellBook = Magic.SpellBook.interfaceForValue(player.getCombat().getMagic().getSpellBook());
            specialEnergy = player.getCombat().getSpecialEnergy().getAmount();
            looks = player.getAppearance().getLooks();
            colours = player.getAppearance().getColors();
            isMuted = player.isMuted();
            isSkulled = player.getItemsOnDeathManager().isSkulled();
            Tick tick = player.retrieveTick("SkullTimer");
            if (tick != null) {
                skullTimer = tick.getCounter();
            }
            ownClan = player.getOwnClan();
            lastClan = player.getClan() == null ? null : player.getClan().getOwner();
        }
        friends = player.getFriends().getFriendsList().toArray(new String[0]);
        ignores = player.getFriends().getIgnoreList().toArray(new String[0]);
        privateChatSetting = player.getFriends().getPrivateChatSetting();
        privateChatColor = player.getFriends().getPrivateChatColor();
    }

    public void load(byte[] data) throws IOException {
        DataInputStream in = new DataInputStream(/*new GZIPInputStream(*/new ByteArrayInputStream(data)/*)*/);

        int mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid header key");
        }

        userId = in.readInt();

        email = Streams.readString(in);
        if (email.equals("")) {
            email = null;
        }

        unreadMessages = in.readShort();

        subscriptionEnd = in.readLong();

        lastLoggedIn = in.readLong();

        mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid 1 key");
        }

        for (int i = 0; i < lastIPs.length; i++) {
            lastIPs[i] = Streams.readString(in);
        }

        mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid 2 key");
        }

        rights = in.readByte();

        x = in.readShort();
        y = in.readShort();
        z = in.readByte();

        runToggled = (in.readByte() & 0xff) == 1;
        runEnergy = in.readByte() & 0xff;

        mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid 3 key");
        }

        for (int i = 0; i < 28; i++) {
            inv[i] = in.readShort();
            invN[i] = in.readInt();
        }
        mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid 4 key");
        }

        for (int i = 0; i < 14; i++) {
            equip[i] = in.readShort();
            equipN[i] = in.readInt();
        }
        mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid 5 key");
        }

        bank = new int[in.readShort() & 0xffff][3];
        for (int i = 0; i < bank.length; i++) {
            int[] item = bank[i];
            item[0] = in.readShort();
            item[1] = in.readInt();
            item[2] = in.readByte();
        }
        mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid 6 key");
        }

        for (int i = 0; i < level.length; i++) {
            level[i] = in.readByte();
            exp[i] = in.readDouble();
        }
        xpCounter = in.readInt();
        autoRetaliate = in.readBoolean();
        attackStyle = in.readByte();
        prayerBook = in.readBoolean();
        if (prayerBook)
            quickPrayers = new byte[20];
        else
            quickPrayers = new byte[30];
        in.read(quickPrayers);
        spellBook = in.readShort();
        specialEnergy = in.readByte();
        for (int i = 0; i < 7; i++) {
            looks[i] = in.readShort();
        }
        for (int i = 0; i < 5; i++) {
            colours[i] = in.readShort();
        }
        isMuted = in.readBoolean();
        isSkulled = in.readBoolean();
        skullTimer = in.readShort();
        mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid 7 key");
        }

        friends = new String[in.readByte() & 0xff];
        for (int i = 0; i < friends.length; i++) {
            friends[i] = Streams.readString(in);
        }
        mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid 8 key");
        }

        ignores = new String[in.readByte() & 0xff];
        for (int i = 0; i < ignores.length; i++) {
            ignores[i] = Streams.readString(in);
        }
        mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid 9 key");
        }

        privateChatSetting = in.readByte() & 0xff;
        privateChatColor = in.readByte() & 0xff;

        if ((in.readByte() & 0xff) == 1) {
            byte[] clanData = new byte[in.readShort() & 0xffff];
            in.read(clanData);

            ownClan = new Clan();
            try {
                ownClan.load(clanData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mN = in.readInt();
        if (mN != MAGIC_NUMBER) {
            throw new IOException("Invalid 10 key");
        }

        lastClan = Streams.readString(in);
        if (lastClan.equals("")) {
            lastClan = null;
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

            out.writeInt(userId);

            if (email == null) {
                out.writeByte(0);
            } else {
                Streams.writeString(email, out);
            }

            out.writeShort(unreadMessages);

            out.writeLong(subscriptionEnd);

            out.writeLong(lastLoggedIn);

            out.writeInt(MAGIC_NUMBER);

            for (int i = 0; i < lastIPs.length; i++) {
                Streams.writeString(lastIPs[i], out);
            }

            out.writeInt(MAGIC_NUMBER);

            out.writeByte(rights);

            out.writeShort(x);
            out.writeShort(y);
            out.writeByte(z);

            out.writeByte(runToggled ? 1 : 0);
            out.writeByte(runEnergy);

            out.writeInt(MAGIC_NUMBER);

            for (int i = 0; i < 28; i++) {
                out.writeShort(inv[i]);
                out.writeInt(invN[i]);
            }
            out.writeInt(MAGIC_NUMBER);

            for (int i = 0; i < 14; i++) {
                out.writeShort(equip[i]);
                out.writeInt(equipN[i]);
            }
            out.writeInt(MAGIC_NUMBER);

            out.writeShort(bank.length);
            for (int i = 0; i < bank.length; i++) {
                int[] item = bank[i];
                out.writeShort(item[0]);
                out.writeInt(item[1]);
                out.writeByte(item[2]);
            }
            out.writeInt(MAGIC_NUMBER);

            for (int i = 0; i < level.length; i++) {
                out.writeByte(level[i]);
                out.writeDouble(exp[i]);
            }
            out.writeInt(xpCounter);
            out.writeBoolean(autoRetaliate);
            out.writeByte(attackStyle);

            out.writeBoolean(prayerBook);
            out.write(quickPrayers);
            out.writeShort(spellBook);
            out.writeByte(specialEnergy);
            for (int i = 0; i < 7; i++) {
                out.writeShort(looks[i]);
            }
            for (int i = 0; i < 5; i++) {
                out.writeShort(colours[i]);
            }
            out.writeBoolean(isMuted);
            out.writeBoolean(isSkulled);
            out.writeShort(skullTimer);

            out.writeInt(MAGIC_NUMBER);

            out.writeByte(friends.length);
            for (String friend : friends) {
                Streams.writeString(friend, out);
            }
            out.writeInt(MAGIC_NUMBER);

            out.writeByte(ignores.length);
            for (String ignore : ignores) {
                Streams.writeString(ignore, out);
            }
            out.writeInt(MAGIC_NUMBER);

            out.writeByte(privateChatSetting);
            out.writeByte(privateChatColor);

            if (ownClan != null) {
                out.writeByte(1);

                byte[] clanData = ownClan.toByteArray();
                out.writeShort(clanData.length);
                out.write(clanData);
            } else {
                out.writeByte(0);
            }
            out.writeInt(MAGIC_NUMBER);

            if (lastClan == null) {
                out.writeByte(0);
            } else {
                Streams.writeString(lastClan, out);
            }

            out.writeInt(MAGIC_NUMBER); // footer key

            out.close(); // flush data

            return baos.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }
}
