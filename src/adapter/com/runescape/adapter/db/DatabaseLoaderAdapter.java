package com.runescape.adapter.db;

import com.runescape.Static;
import com.runescape.adapter.DatabaseLoader;
import com.runescape.content.cc.Clan;
import com.runescape.content.cc.Clan.Rank;
import com.runescape.engine.login.LoginResponse;
import com.runescape.io.sql.SQLSession;
import com.runescape.logic.player.Levels;
import com.runescape.logic.player.PlayerSave;
import com.runescape.utility.ArrayUtilities;
import com.runescape.utility.Logging;
import com.runescape.utility.Pool;
import com.runescape.utility.Text;

import org.apache.log4j.Logger;

import static com.runescape.utility.ArrayUtilities.primitive;
import static com.runescape.utility.ArrayUtilities.toArray;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Lazaro
 * 
 * @author Seth Rogen
 */
public class DatabaseLoaderAdapter implements DatabaseLoader {
    private static final Logger logger = Logging.log();

    @Override
    public void banIP(String ip) {
    }

    @Override
    public boolean isBanned(String userName) {
        return false;
    }

    @Override
    public boolean isIPBanned(String ip) {
        return false;
    }

    @Override
    public LoginResponse loadPlayer(String userName, String password, PlayerSave player) {
        userName = Text.formatNameForProtocol(userName);

        Pool<SQLSession> pool = Static.currentLink().getSQLPool();

        SQLSession sql = null;
        try {
            sql = pool.acquire();

            Statement st = sql.createStatement();

            ResultSet rs = st.executeQuery("SELECT * FROM members WHERE members_seo_name='" + userName.replace("_","-") + "' OR email='" + userName + "' LIMIT 1");
            if (!rs.next()) {
                return LoginResponse.INVALID_DETAILS;
            }

            if (password != null) {
                String passwordHash = rs.getString("members_pass_hash");
                String passwordSalt = rs.getString("members_pass_salt");
                if (!verifyPassword(password, passwordHash, passwordSalt)) {
                    return LoginResponse.INVALID_DETAILS;
                }
            }

            int userId = rs.getInt("member_id");
            player.userId = userId;

            int[] userGroups = new int[1];
            userGroups[0] = rs.getInt("member_group_id");
            if (userGroups[0] == 5) {
                return LoginResponse.BANNED;
            }
            applyGroupData(player, userGroups);

            player.email = rs.getString("email");
            if (player.email != null && player.email.length() == 0) {
                player.email = null;
            }

            player.unreadMessages = rs.getInt("msg_count_new");
            rs.close();

            player.subscriptionEnd = 0;
            rs = st.executeQuery("SELECT * FROM subscription_trans WHERE subtrans_member_id='" + userId + "'");
            while (rs.next()) {
                String state = rs.getString("subtrans_state");
                if (state.equalsIgnoreCase("paid") || state.equalsIgnoreCase("canceled")) {
                    long end = rs.getLong("subtrans_end_date") * 1000;
                    if (end > player.subscriptionEnd) {
                        player.subscriptionEnd = end;
                    }
                }
            }
            rs.close();

            //if(player.rights == 0 && player.subscriptionEnd < System.currentTimeMillis()) {
            //    return LoginResponse.MEMBERS_REQUIRED;
            //}

            for (int attempt = 0; attempt < 2; attempt++) {
                rs = st.executeQuery("SELECT * FROM playersave WHERE id='" + userId + "' LIMIT 1");
                if (!rs.next()) {
                    rs.close();
                    st.executeUpdate("INSERT INTO playersave (id, name) VALUES ('" + userId + "', '" + userName + "')");
                    continue;
                } else {
                    attempt++;
                }

                player.lastLoggedIn = rs.getLong("lastLoggedIn");

                player.lastIPs = toArray(rs.getString("lastIPs"), new String[3]);
                player.isMuted = rs.getInt("muted") == 1;

                player.x = rs.getInt("x");
                player.y = rs.getInt("y");
                player.z = rs.getInt("z");

                player.runToggled = rs.getBoolean("runToggled");
                player.runEnergy = rs.getInt("runEnergy");

                player.inv = primitive(toArray(rs.getString("inv"), new Short[28]));
                player.invN = primitive(toArray(rs.getString("invN"), new Integer[28]));

                player.equip = primitive(toArray(rs.getString("equip"), new Short[14]));
                player.equipN = primitive(toArray(rs.getString("equipN"), new Integer[14]));

                String bankS = rs.getString("bank");
                if (bankS == null) {
                    player.bank = new int[0][3];
                } else {
                    short[] bank = primitive(toArray(bankS, new Short[0]));
                    int[] bankN = primitive(toArray(rs.getString("bankN"), new Integer[0]));
                    byte[] bankT = primitive(toArray(rs.getString("bankT"), new Byte[0]));

                    player.bank = new int[bank.length][3];
                    for (int i = 0; i < bank.length; i++) {
                        int[] entry = player.bank[i];
                        entry[0] = bank[i];
                        entry[1] = bankN[i];
                        entry[2] = bankT[i];
                    }
                }
                player.level = primitive(toArray(rs.getString("level"), new Byte[Levels.SKILL_COUNT]));
                player.exp = primitive(toArray(rs.getString("exp"), new Double[Levels.SKILL_COUNT]));
                player.xpCounter = rs.getInt("xpCounter");

                player.autoRetaliate = rs.getInt("autoRetaliate") == 1;
                player.attackStyle = rs.getInt("attackStyle");

                player.prayerBook = rs.getInt("prayerBook") == 1;
                String quickPrayers = rs.getString("quickPrayers");
                if (quickPrayers.length() == 0)
                    quickPrayers = player.prayerBook ? "0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0" : "0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0";
                player.quickPrayers = primitive(toArray(quickPrayers, new Byte[player.prayerBook ? 20 : 30]));
                player.spellBook = rs.getInt("spellBook");
                player.specialEnergy = rs.getInt("specialEnergy");
                String looks = rs.getString("looks");
                if (looks.length() == 0)
                    looks = "310;307;443;599;390;646;438";
                player.looks = primitive(toArray(looks, new Integer[7]));
                String colours = rs.getString("colors");
                if (colours.length() == 0)
                    colours = "6;40;216;4;0";
                player.colours = primitive(toArray(colours, new Integer[5]));
                player.isMuted = rs.getInt("muted") == 1;
                player.isSkulled = rs.getInt("skulled") == 1;
                player.skullTimer = rs.getInt("skullTimer");

                player.friends = toArray(rs.getString("friends"), new String[0]);
                player.ignores = toArray(rs.getString("ignores"), new String[0]);

                player.privateChatSetting = rs.getInt("pmSetting");
                player.privateChatColor = rs.getInt("pmColor");

                player.lastClan = rs.getString("lastClan");

                rs.close();

                Clan clan = new Clan(userName);
                if (loadClan(userName, clan, st)) {
                    player.ownClan = clan;
                }

                st.close();

                return LoginResponse.LOGIN;
            }
        } catch (SQLException e) {
            logger.error("Error loading player [name=" + userName + "]", e);
        } finally {
            if (sql != null) {
                pool.release(sql);
            }
        }
        return LoginResponse.ERROR;
    }

    @Override
    public void savePlayer(String userName, boolean lobby, PlayerSave player) {
        Pool<SQLSession> pool = Static.currentLink().getSQLPool();

        SQLSession sql = null;
        try {
            sql = pool.acquire();

            StringBuilder query = new StringBuilder();
            query.append("UPDATE playersave SET");
            query.append(" lastIPs='").append(ArrayUtilities.toString(player.lastIPs)).append("'");
            query.append(", muted='").append(player.isMuted ? 1 : 0).append("'");
            query.append(", lastLoggedIn='").append(System.currentTimeMillis()).append("'");
            query.append(", friends='").append(ArrayUtilities.toString(player.friends)).append("'");
            query.append(", ignores='").append(ArrayUtilities.toString(player.ignores)).append("'");
            query.append(", pmSetting='").append(player.privateChatSetting).append("'");
            query.append(", pmColor='").append(player.privateChatColor).append("'");
            if (!lobby) {
                query.append(", x='").append(player.x).append("'");
                query.append(", y='").append(player.y).append("'");
                query.append(", z='").append(player.z).append("'");
                query.append(", runToggled='").append(player.runToggled ? 1 : 0).append("'");
                query.append(", runEnergy='").append(player.runEnergy).append("'");
                query.append(", inv='").append(ArrayUtilities.toString(player.inv)).append("'");
                query.append(", invN='").append(ArrayUtilities.toString(player.invN)).append("'");
                query.append(", equip='").append(ArrayUtilities.toString(player.equip)).append("'");
                query.append(", equipN='").append(ArrayUtilities.toString(player.equipN)).append("'");

                int[] bank = new int[player.bank.length];
                for (int i = 0; i < bank.length; i++) {
                    bank[i] = player.bank[i][0];
                }
                query.append(", bank='").append(ArrayUtilities.toString(bank)).append("'");
                for (int i = 0; i < bank.length; i++) {
                    bank[i] = player.bank[i][1];
                }
                query.append(", bankN='").append(ArrayUtilities.toString(bank)).append("'");
                for (int i = 0; i < bank.length; i++) {
                    bank[i] = player.bank[i][2];
                }
                query.append(", bankT='").append(ArrayUtilities.toString(bank)).append("'");

                query.append(", level='").append(ArrayUtilities.toString(player.level)).append("'");
                query.append(", exp='").append(ArrayUtilities.toString(player.exp)).append("'");
                query.append(", xpCounter='").append(player.xpCounter).append("'");
                query.append(", autoRetaliate='").append(player.autoRetaliate ? 1 : 0).append("'");
                query.append(", attackStyle='").append(player.attackStyle).append("'");
                query.append(", prayerBook='").append(player.prayerBook ? 1 : 0).append("'");
                query.append(", quickPrayers='").append(ArrayUtilities.toString(player.quickPrayers)).append("'");
                query.append(", spellBook='").append(player.spellBook).append("'");
                query.append(", specialEnergy='").append(player.specialEnergy).append("'");
                query.append(", looks='").append(ArrayUtilities.toString(player.looks)).append("'");
                query.append(", colors='").append(ArrayUtilities.toString(player.colours)).append("'");
                query.append(", muted='").append(player.isMuted ? 1 : 0).append("'");
                query.append(", skulled='").append(player.isSkulled ? 1 : 0).append("'");
                query.append(", skullTimer='").append(player.skullTimer).append("'");
                query.append(", lastClan=");
                if (player.lastClan == null) {
                    query.append("NULL");
                } else {
                    query.append("'").append(player.lastClan).append("'");
                }
            }
            query.append(" WHERE id='").append(player.userId).append("'");
            Statement st = sql.createStatement();
            st.executeUpdate(query.toString());
            st.close();
        } catch (SQLException e) {
            logger.error("Error saving player [name=" + userName + "]", e);
        } finally {
            if (sql != null) {
                pool.release(sql);
            }
        }
    }

    @Override
    public void registerClan(String owner, String name) {
        Pool<SQLSession> pool = Static.currentLink().getSQLPool();

        SQLSession sql = null;
        try {
            sql = pool.acquire();

            Statement st = sql.createStatement();
            st.executeUpdate("INSERT INTO clans (owner, name) VALUES ('" + owner + "', '" + name + "')");
            st.close();
        } catch (SQLException e) {
            logger.error("Error registering clan [owner=" + owner + "]");
        } finally {
            if (sql != null) {
                pool.release(sql);
            }
        }
    }

    @Override
    public boolean loadClan(String owner, Clan clan) {
        Pool<SQLSession> pool = Static.currentLink().getSQLPool();

        SQLSession sql = null;
        try {
            sql = pool.acquire();

            Statement st = sql.createStatement();

            boolean success = loadClan(owner, clan, st);

            st.close();

            return success;
        } catch (SQLException e) {
            logger.error("Error loading clan [owner=" + owner + "]", e);
        } finally {
            if (sql != null) {
                pool.release(sql);
            }
        }
        return false;
    }

    private boolean loadClan(String owner, Clan clan, Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT * FROM clans WHERE owner='" + owner + "' LIMIT 1");
        if (!rs.next()) {
            rs.close();
            return false;
        }

        clan.setName(rs.getString("name"));

        clan.setEnabled(rs.getBoolean("enabled"));

        clan.setEnterRequirement(Rank.forValue(rs.getInt("enterReq")));
        clan.setTalkRequirement(Rank.forValue(rs.getInt("talkReq")));
        clan.setKickRequirement(Rank.forValue(rs.getInt("kickReq")));
        clan.setLootShareRequirement(Rank.forValue(rs.getInt("lootShareReq")));

        String[] rankedMembers;

        rankedMembers = toArray(rs.getString("recruits"), new String[0]);
        for (String member : rankedMembers) {
            clan.getRanks().put(member, Rank.RECRUIT);
        }
        rankedMembers = toArray(rs.getString("corporals"), new String[0]);
        for (String member : rankedMembers) {
            clan.getRanks().put(member, Rank.CORPORAL);
        }
        rankedMembers = toArray(rs.getString("sergeants"), new String[0]);
        for (String member : rankedMembers) {
            clan.getRanks().put(member, Rank.SERGEANT);
        }
        rankedMembers = toArray(rs.getString("lieutenants"), new String[0]);
        for (String member : rankedMembers) {
            clan.getRanks().put(member, Rank.LIEUTENANT);
        }
        rankedMembers = toArray(rs.getString("captains"), new String[0]);
        for (String member : rankedMembers) {
            clan.getRanks().put(member, Rank.CAPTAIN);
        }
        rankedMembers = toArray(rs.getString("generals"), new String[0]);
        for (String member : rankedMembers) {
            clan.getRanks().put(member, Rank.GENERAL);
        }

        rs.close();

        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void saveClan(Clan clan) {
        Pool<SQLSession> pool = Static.currentLink().getSQLPool();

        SQLSession sql = null;
        try {
            sql = pool.acquire();

            Statement st = sql.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM clans WHERE owner='" + clan.getOwner() + "' LIMIT 1");
            if (!rs.next()) {
                st.executeUpdate("INSERT INTO clans (owner) VALUES ('" + clan.getOwner() + "')");
            }
            rs.close();

            StringBuilder query = new StringBuilder();
            query.append("UPDATE clans SET");
            query.append(" name='").append(clan.getName()).append("'");
            query.append(", enabled='").append(clan.isEnabled() ? 1 : 0).append("'");
            query.append(", enterReq='").append(clan.getEnterRequirement().intValue()).append("'");
            query.append(", talkReq='").append(clan.getTalkRequirement().intValue()).append("'");
            query.append(", kickReq='").append(clan.getKickRequirement().intValue()).append("'");
            query.append(", lootShareReq='").append(clan.getLootShareRequirement().intValue()).append("'");

            List[] ranks = new List[9];
            for (Map.Entry<String, Rank> ranked : clan.getRanks().entrySet()) {
                List rankList = ranks[ranked.getValue().intValue() + 1];
                if (rankList == null) {
                    rankList = ranks[ranked.getValue().intValue() + 1] = new ArrayList();
                }

                rankList.add(ranked.getKey());
            }
            List curList;

            query.append(", recruits='").append((curList = ranks[Rank.RECRUIT.intValue() + 1]) != null ? ArrayUtilities.toString(curList.toArray(new String[0])) : "").append("'");
            query.append(", corporals='").append((curList = ranks[Rank.CORPORAL.intValue() + 1]) != null ? ArrayUtilities.toString(curList.toArray(new String[0])) : "").append("'");
            query.append(", sergeants='").append((curList = ranks[Rank.SERGEANT.intValue() + 1]) != null ? ArrayUtilities.toString(curList.toArray(new String[0])) : "").append("'");
            query.append(", lieutenants='").append((curList = ranks[Rank.LIEUTENANT.intValue() + 1]) != null ? ArrayUtilities.toString(curList.toArray(new String[0])) : "").append("'");
            query.append(", captains='").append((curList = ranks[Rank.CAPTAIN.intValue() + 1]) != null ? ArrayUtilities.toString(curList.toArray(new String[0])) : "").append("'");
            query.append(", generals='").append((curList = ranks[Rank.GENERAL.intValue() + 1]) != null ? ArrayUtilities.toString(curList.toArray(new String[0])) : "").append("'");

            query.append(" WHERE owner='").append(clan.getOwner()).append("'");
            st.executeUpdate(query.toString());
            st.close();
        } catch (SQLException e) {
            logger.error("Error saving clan [owner=" + clan + "]", e);
        } finally {
            if (sql != null) {
                pool.release(sql);
            }
        }
    }

    @Override
    public void reload() {
    }

    private boolean verifyPassword(String inputPassword, String storedPasswordHash, String storedSalt) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error("MD5 is an invalid algorithm!", e);
            return false;
        }

        byte[] inputPasswordHash = digest.digest(inputPassword.getBytes());
        digest.reset();

        byte[] storedSaltHash = digest.digest(storedSalt.getBytes());
        digest.reset();

        byte[] finalHash = digest.digest(new StringBuilder().append(toHexString(storedSaltHash)).append(toHexString
                (inputPasswordHash)).toString().getBytes());

        return toHexString(finalHash).equalsIgnoreCase(storedPasswordHash);
    }

    private void applyGroupData(PlayerSave player, int[] groups) {
        for (int groupId : groups) {
            switch (groupId) {
                case 7: // Super moderator
                case 6: // Moderator
                case 20: // Trial moderator
                    if (player.rights < 1) {
                        player.rights = 1;
                    }
                    break;
                case 4: // Administrator
                    if (player.rights < 2) {
                        player.rights = 2;
                    }
                    break;
            }
        }
    }

    private String toHexString(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfByte = (data[i] >>> 4) & 0x0F;
            int twoHalfs = 0;
            do {
                if ((0 <= halfByte) && (halfByte <= 9))
                    buf.append((char) ('0' + halfByte));
                else
                    buf.append((char) ('a' + (halfByte - 10)));
                halfByte = data[i] & 0x0F;
            } while (twoHalfs++ < 1);
        }
        return buf.toString();
    }
}
