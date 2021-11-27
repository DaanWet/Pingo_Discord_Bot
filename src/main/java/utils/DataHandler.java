package utils;


import com.mysql.cj.exceptions.WrongArgumentException;
import commands.roles.RoleCommand;
import commands.settings.Setting;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.sk.PrettyTable;
import utils.dbdata.RecordData;
import utils.dbdata.RoleAssignData;
import utils.dbdata.RoleAssignRole;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


@SuppressWarnings("unchecked")
public class DataHandler {

    private final String JDBC_URL = "jdbc:mysql://localhost:3306/pingo?character_set_server=utf8mb4&serverTimezone=CET";
    private static String USER_ID;
    private static String PASSWD;
    private Properties properties;
    private Properties nomultiproperties;

    public DataHandler() {
        properties = new Properties();
        properties.setProperty("user", USER_ID);
        properties.setProperty("password", PASSWD);
        properties.setProperty("allowMultiQueries", "true");
        properties.setProperty("characterEncoding", "utf8");
        properties.setProperty("CharSet", "utf8mb4");
        properties.setProperty("useUnicode", "true");
        nomultiproperties = new Properties(properties);
        nomultiproperties.setProperty("allowMultiQueries", "false");
    }

    public static void setUserId(String userId) {
        USER_ID = userId;
    }

    public static void setPASSWD(String PASSWD) {
        DataHandler.PASSWD = PASSWD;
    }

    public void createDatabase() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement setuptable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS Record (ID INT, Name VARCHAR(50) NOT NULL PRIMARY KEY, IsInt BOOLEAN DEFAULT TRUE);" +
                                                                          "CREATE TABLE IF NOT EXISTS Member (UserId BIGINT NOT NULL, GuildId BIGINT NOT NULL, Credits INT DEFAULT 0, LastDaily TIMESTAMP, LastWeekly TIMESTAMP, Experience INT DEFAULT 0, CurrentStreak  INT DEFAULT 0, PRIMARY KEY(UserId, GuildId)); " +
                                                                          "CREATE TABLE IF NOT EXISTS RoleAssign (Name VARCHAR(255) NOT NULL, GuildId BIGINT NOT NULL, ChannelId BIGINT, MessageId BIGINT, Sorting VARCHAR(20) DEFAULT 'NONE', Compacting VARCHAR(20) DEFAULT 'NORMAL', Title VARCHAR(255), PRIMARY KEY(Name, GuildId));" +
                                                                          "CREATE TABLE IF NOT EXISTS Role (RoleId BIGINT NOT NULL, Name VARCHAR(255) NOT NULL, Emoji VARCHAR(255) NOT NULL, Type VARCHAR(255) NOT NULL, GuildId BIGINT NOT NULL, FOREIGN KEY (Type, GuildId) REFERENCES RoleAssign(Name, GuildId), PRIMARY KEY (Emoji, Type, GuildId));" +
                                                                          "CREATE TABLE IF NOT EXISTS UserRecord (UserId BIGINT NOT NULL, GuildId BIGINT NOT NULL, Name VARCHAR(50) NOT NULL, Link VARCHAR(255), Value DOUBLE NOT NULL, PRIMARY KEY(UserId, GuildId, Name), FOREIGN KEY(UserId, GuildId) REFERENCES Member(UserId, GuildId), FOREIGN KEY (Name) REFERENCES Record(Type));" +
                                                                          "CREATE TABLE IF NOT EXISTS Setting (ID INT AUTO_INCREMENT PRIMARY KEY,  Name VARCHAR(50) NOT NULL, ValueType VARCHAR(10) NOT NULL, Type VARCHAR(50), Multiple BOOLEAN NOT NULL,  UNIQUE (Name, Type));" +
                                                                          "CREATE TABLE IF NOT EXISTS GuildSetting (GuildId BIGINT NOT NULL, ID INT NOT NULL, Value VARCHAR(255) NOT NULL, Type VARCHAR(50), FOREIGN KEY(ID) REFERENCES Setting(ID), PRIMARY KEY(GuildId, ID, Value));" +
                                                                          "CREATE TABLE IF NOT EXISTS Cooldown (GuildId BIGINT NOT NULL, UserId BIGINT NOT NULL, Setting INT NOT NULL, Time TIMESTAMP, PRIMARY KEY (GuildId, UserId, Setting), FOREIGN KEY (Setting) REFERENCES Setting(ID));" +
                                                                          "INSERT IGNORE INTO Record VALUES (1, 'highest_credits', TRUE) ON DUPLICATE KEY UPDATE ID = 1;" +
                                                                          "INSERT IGNORE INTO Record VALUES (4, 'biggest_bj_win', TRUE) ON DUPLICATE KEY UPDATE ID = 4;" +
                                                                          "INSERT IGNORE INTO Record VALUES (5, 'biggest_bj_loss', TRUE) ON DUPLICATE KEY UPDATE ID = 5;" +
                                                                          "INSERT IGNORE INTO Record VALUES (3, 'bj_win_rate', FALSE) ON DUPLICATE KEY UPDATE ID = 2;" +
                                                                          "INSERT IGNORE INTO Record VALUES (2, 'bj_games_played', TRUE) ON DUPLICATE KEY UPDATE ID = 3;" +
                                                                          "INSERT IGNORE INTO Record VALUES (6, 'bj_win_streak', TRUE) ON DUPLICATE KEY UPDATE ID = 6;" +
                                                                          "INSERT IGNORE INTO Record VALUES (7, 'bj_loss_streak', TRUE) ON DUPLICATE KEY UPDATE ID = 7;"
             )) {
            setuptable.executeUpdate();
            for (Setting s : Setting.values()) {
                PreparedStatement stm = conn.prepareStatement("INSERT IGNORE INTO Setting(Name, ValueType, Type, Multiple) VALUES(?, ?, ?, ?)");
                stm.setString(1, s.name());
                stm.setString(2, s.getValueType().getName());
                stm.setString(3, s.getType());
                stm.setBoolean(4, s.isMultiple());
                stm.executeUpdate();
                if (s.isMultiple()) {
                    PreparedStatement stm1 = conn.prepareStatement("INSERT IGNORE INTO Setting(Name, ValueType, Type, Multiple) VALUES(?, ?, ?, ?)");
                    stm1.setString(1, s.name() + "_on");
                    stm1.setString(2, Setting.ValueType.BOOLEAN.getName());
                    stm1.setString(3, s.getType());
                    stm1.setBoolean(4, false);
                    stm1.executeUpdate();
                }
                for (Setting.SubSetting subs : s.getSubSettings()) {
                    PreparedStatement stmn = conn.prepareStatement("INSERT IGNORE INTO Setting(Name, ValueType, Type, Multiple) VALUES(?, ?, ?, ?)");
                    stmn.setString(1, String.format("%s_%s", s.getName(), subs));
                    stmn.setString(2, subs.getValueType().getName());
                    stmn.setString(3, s.getType());
                    stmn.setBoolean(4, subs.isMultiple());
                    stmn.executeUpdate();
                    if (subs.isMultiple()) {
                        PreparedStatement stmn1 = conn.prepareStatement("INSERT IGNORE INTO Setting(Name, ValueType, Type, Multiple) VALUES(?, ?, ?, ?)");
                        stmn1.setString(1, String.format("%s_%s_on", s.getName(), subs));
                        stmn1.setString(2, Setting.ValueType.BOOLEAN.getName());
                        stmn1.setString(3, s.getType());
                        stmn1.setBoolean(4, false);
                        stmn1.executeUpdate();
                    }
                }
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    //<editor-fold desc="RoleAssign Code">
    public ArrayList<String> getRoleCategories(long guildId) {
        ArrayList<String> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmn = conn.prepareStatement("SELECT Name FROM RoleAssign WHERE GuildId = ?");
        ) {
            stmn.setLong(1, guildId);
            try (ResultSet set = stmn.executeQuery()) {
                while (set.next()) {
                    list.add(set.getString("Name"));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }


    public ArrayList<RoleAssignRole> getRoles(long guildID, String type) {
        ArrayList<RoleAssignRole> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmn = conn.prepareStatement("SELECT Name, Emoji, RoleId FROM Role WHERE GuildId = ? AND Type LIKE ?")) {
            stmn.setLong(1, guildID);
            stmn.setString(2, type);
            try (ResultSet set = stmn.executeQuery()) {
                while (set.next()) {
                    list.add(new RoleAssignRole(set.getString("Emoji"), set.getString("Name"), set.getLong("RoleId")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }

    public boolean setMessage(long guildId, String type, long channelId, long messageId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmnt = conn.prepareStatement("UPDATE RoleAssign SET ChannelId = ?, MessageId = ? WHERE GuildId = ? AND Name LIKE ?")) {
            stmnt.setLong(3, guildId);
            stmnt.setLong(1, channelId);
            stmnt.setLong(2, messageId);
            stmnt.setString(4, type);
            int i = stmnt.executeUpdate();
            return i != 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean setCompacting(long guildId, String type, RoleCommand.Compacting compacting, String sorting) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmnt = conn.prepareStatement("UPDATE RoleAssign SET Compacting = ?, Sorting = ? WHERE GuildId = ? AND Name LIKE ?")) {
            stmnt.setLong(3, guildId);
            stmnt.setString(1, compacting.toString());
            stmnt.setString(2, sorting);
            stmnt.setString(4, type);
            int i = stmnt.executeUpdate();
            return i != 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public RoleAssignData getRoleAssignData(long guildId, String type) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmn = conn.prepareStatement("SELECT ChannelId, MessageId, Compacting, Sorting, Title FROM RoleAssign WHERE GuildId = ? AND Name LIKE ?")) {
            stmn.setLong(1, guildId);
            stmn.setString(2, type);
            try (ResultSet set = stmn.executeQuery()) {
                if (set.next()) {
                    boolean valid = RoleCommand.Sorting.isSort(set.getString(4));
                    RoleAssignData data = new RoleAssignData(set.getLong(1), set.getLong(2), RoleCommand.Compacting.valueOf(set.getString(3)), valid ? RoleCommand.Sorting.valueOf(set.getString(4)) : RoleCommand.Sorting.CUSTOM, set.getString(5));
                    if (!valid) data.setCustomS(set.getString(4));
                    return data;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new RoleAssignData(null, null, RoleCommand.Compacting.NORMAL, RoleCommand.Sorting.NONE, null);
    }

    public String getCategory(long guildId, long channelId, long messageId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stm = conn.prepareStatement("SELECT Name FROM RoleAssign WHERE GuildId = ? AND ChannelId = ? AND MessageId = ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, channelId);
            stm.setLong(3, messageId);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()) {
                    return set.getString(1);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public boolean addRoleAssign(long guildId, String type, String emoji, String name, long roleId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("INSERT IGNORE INTO RoleAssign (Name, GuildId) VALUES (?, ?);");
             PreparedStatement stm2 = conn.prepareStatement("INSERT IGNORE INTO Role VALUES (?, ?, ?, ?, ?);")
        ) {
            stm.setString(1, type);
            stm.setLong(2, guildId);
            stm2.setLong(1, roleId);
            stm2.setString(2, name);
            stm2.setString(3, emoji);
            stm2.setString(4, type);
            stm2.setLong(5, guildId);

            int i = stm.executeUpdate();
            int j = stm2.executeUpdate();
            return i + j > 0;
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public boolean removeRoleAssign(long guildId, String type, String emoji) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("DELETE FROM Role WHERE GuildId = ? AND Type LIKE ? AND Emoji LIKE ?")) {
            stm.setLong(1, guildId);
            stm.setString(2, type);
            stm.setString(3, emoji);
            int i = stm.executeUpdate();
            return i != 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean editRoleName(long guildId, String type, String emoji, String name) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Role SET Name = ? WHERE GuildId = ? AND Type LIKE ? AND Emoji LIKE ?")) {
            stm.setLong(2, guildId);
            stm.setString(1, name);
            stm.setString(3, type);
            stm.setString(4, emoji);
            int a = stm.executeUpdate();
            return a == 1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean setTitle(long guildId, String type, String title) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE RoleAssign SET Title = ? WHERE GuildId = ? AND Name LIKE ?")) {
            stm.setString(1, title);
            stm.setLong(2, guildId);
            stm.setString(3, type);
            int a = stm.executeUpdate();
            return a == 1;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    //</editor-fold>

    //<editor-fold desc="Credits code">

    public int getCredits(long guildID, long userId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Credits FROM Member WHERE GuildId = ? AND UserId = ?");
        ) {
            stm.setLong(1, guildID);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()) {
                    return set.getInt("Credits");
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }


    public HashMap<Long, Integer> getAllCredits(long guildId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Credits, UserId FROM Member WHERE GuildId = ? ORDER BY Credits DESC")
        ) {
            HashMap<Long, Integer> map = new HashMap<>();
            stm.setLong(1, guildId);
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()) {
                    map.put(set.getLong("UserId"), set.getInt("Credits"));
                }
            }
            return map;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public HashMap<Long, Integer> getAllCredits() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Credits, UserId FROM Member ORDER BY Credits DESC")
        ) {
            HashMap<Long, Integer> map = new HashMap<>();
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()) {
                    if (!map.containsKey(set.getLong("UserId")))
                        map.put(set.getLong("UserId"), set.getInt("Credits"));
                }
            }
            return map;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }


    public void setCredits(long guildId, long userId, int credits) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("INSERT IGNORE INTO Member(UserId, GuildId, LastDaily, LastWeekly) VALUES(?, ?, ?, ?);" +
                                                                   "UPDATE Member SET Credits = ? WHERE GuildId = ? AND UserId = ?;" +
                                                                   "INSERT IGNORE INTO UserRecord(UserId, GuildId, Name, Value) VALUES (?, ?, 'highest_credits', 0.0);" +
                                                                   "UPDATE UserRecord SET Value = GREATEST(Value, ?) WHERE UserId = ? AND GuildId = ? AND Name LIKE 'highest_credits'") //TODO: Use insert INto and ON Duplicate Keys
        ) {
            stm.setLong(1, userId);
            stm.setLong(2, guildId);
            stm.setLong(6, guildId);
            stm.setLong(7, userId);
            stm.setLong(8, userId);
            stm.setLong(9, guildId);
            stm.setLong(11, userId);
            stm.setLong(12, guildId);
            LocalDateTime now = LocalDateTime.now();
            stm.setTimestamp(3, Timestamp.valueOf(now.minusDays(1)));
            stm.setTimestamp(4, Timestamp.valueOf(now.minusDays(7)));
            stm.setInt(5, credits);
            stm.setInt(10, credits);
            stm.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public int addCredits(long guildId, long userId, int credits) {
        int creds = getCredits(guildId, userId);
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("INSERT INTO Member(UserId, GuildId, LastDaily, LastWeekly, Credits) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Credits = Credits + ?;" +
                                                                   "INSERT INTO UserRecord(UserId, GuildId, Name, Value) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE Value = GREATEST(Value, ?);");
             PreparedStatement stmnt2 = conn.prepareStatement("SELECT Credits FROM Member  WHERE GuildId = ? AND UserId = ?");
        ) {
            stm.setLong(1, userId);
            stm.setLong(2, guildId);
            stm.setLong(7, userId);
            stm.setLong(8, guildId);
            LocalDateTime now = LocalDateTime.now();
            stm.setTimestamp(3, Timestamp.valueOf(now.minusDays(1)));
            stm.setTimestamp(4, Timestamp.valueOf(now.minusDays(7)));
            stm.setInt(5, credits);
            stm.setInt(6, credits);
            stm.setInt(10, creds + credits);
            stm.setInt(11, creds + credits);
            stm.setString(9, "highest_credits");
            stm.executeUpdate();
            stmnt2.setLong(1, guildId);
            stmnt2.setLong(2, userId);
            try (ResultSet set = stmnt2.executeQuery()) {
                if (set.next()) {
                    return set.getInt("Credits");
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public LocalDateTime getLatestCollect(long guildId, long userId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT LastDaily FROM Member WHERE GuildId = ? AND UserId = ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()) {
                    return set.getTimestamp("LastDaily").toLocalDateTime();
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public LocalDateTime getLatestWeekCollect(long guildId, long userId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT LastWeekly FROM Member WHERE GuildId = ? AND UserId = ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()) {
                    return set.getTimestamp("LastWeekly").toLocalDateTime();
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public void setLatestWeekCollect(long guild, long userId, LocalDateTime time) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Member SET LastWeekly = ? WHERE GuildId = ? AND UserId = ?")) {
            stm.setTimestamp(1, Timestamp.valueOf(time));
            stm.setLong(2, guild);
            stm.setLong(3, userId);
            stm.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void setLatestCollect(long guildId, long userId, LocalDateTime time) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Member SET LastDaily = ? WHERE GuildId = ? AND UserId = ?")) {
            stm.setTimestamp(1, Timestamp.valueOf(time));
            stm.setLong(2, guildId);
            stm.setLong(3, userId);
            stm.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    //</editor-fold>

    //<editor-fold desc = "Experience">
    public void setXP(long guildID, long userId, int xp) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Member SET Experience = ? WHERE GuildId = ? AND UserId = ?")) {
            stm.setInt(1, xp);
            stm.setLong(2, guildID);
            stm.setLong(3, userId);
            stm.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public int addXP(long guildID, long userId, int xp) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Member SET Experience = Experience + ? WHERE GuildId = ? AND UserId = ?");
             PreparedStatement stmn = conn.prepareStatement("SELECT Experience FROM Member WHERE GuildId = ? AND UserId = ?")) {
            stm.setInt(1, xp);
            stm.setLong(2, guildID);
            stm.setLong(3, userId);
            stm.executeUpdate();
            stmn.setLong(1, guildID);
            stmn.setLong(2, userId);
            try (ResultSet set = stmn.executeQuery()) {
                return set.getInt("Experience");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    public int getXP(long guildId, long userId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT Experience FROM Member WHERE GuildId = ? AND UserId = ?")) {
            stmn.setLong(1, guildId);
            stmn.setLong(2, userId);
            try (ResultSet set = stmn.executeQuery()) {
                return set.getInt("Experience");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    public HashMap<Long, Integer> getAllXp(long guildId) {
        HashMap<Long, Integer> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT Experience, UserId FROM Member WHERE GuildId = ?")) {
            stmn.setLong(1, guildId);
            try (ResultSet set = stmn.executeQuery()) {
                while (set.next()) {
                    map.put(set.getLong("UserId"), set.getInt("Experience"));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return map;
    }

    //</editor-fold>

    //<editor-fold desc="Record Code">
    public void setRecord(long guildId, long userId, String record, double value, boolean ignore) {
        setRecord(guildId, userId, record, value, null, ignore);
    }

    public void setRecord(long guildId, long userId, String record, double value, String link, boolean ignore) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("INSERT INTO UserRecord VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Link = IF(Value < ?, ?, Link), Value = " + (ignore ? "?" : "GREATEST(Value, ?)"))) {
            stm.setLong(1, userId);
            stm.setLong(2, guildId);
            stm.setString(3, record);
            stm.setString(4, link);
            stm.setDouble(5, value);
            stm.setDouble(6, value);
            stm.setString(7, link);
            stm.setDouble(8, value);
            stm.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public RecordData getRecord(long guildId, long userId, String record) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Value, Link FROM UserRecord WHERE GuildId = ? AND UserId = ? AND Name LIKE ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            stm.setString(3, record);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()) {
                    return new RecordData(userId, record, set.getDouble("Value"), set.getString("Link"));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public ArrayList<RecordData> getRecords(long guildId, long userId) {
        ArrayList<RecordData> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Name, Value, Link FROM UserRecord INNER JOIN Record USING(Name) WHERE GuildId = ? AND UserId = ? ORDER BY ID")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()) {
                    list.add(new RecordData(userId, set.getString("Name"), set.getDouble("Value"), set.getString("Link")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }


    public ArrayList<RecordData> getRecords(long guildId) {
        ArrayList<RecordData> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT a.UserId, a.Name, a.Value, a.Link FROM (UserRecord a INNER JOIN (SELECT Name, MAX(Value) AS Max, GuildId FROM UserRecord WHERE GuildId = ? GROUP BY Name)  AS m ON a.Name = m.Name and a.Value = m.max and a.GuildId = m.GuildId) INNER JOIN Record r ON r.Name = a.Name ORDER BY ID")) {
            stm.setLong(1, guildId);
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()) {
                    list.add(new RecordData(set.getLong(1), set.getString(2), set.getDouble(3), set.getString(4)));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }

    public ArrayList<RecordData> getRecords(){
        ArrayList<RecordData> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT a.UserId, a.Name, a.Value, a.Link FROM (UserRecord a INNER JOIN (SELECT Name, MAX(Value) AS Max FROM UserRecord GROUP BY Name)  AS m ON a.Name = m.Name and a.Value = m.max) INNER JOIN Record r ON r.Name = a.Name ORDER BY ID")) {
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()) {
                    list.add(new RecordData(set.getLong(1), set.getString(2), set.getDouble(3), set.getString(4)));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }

    public ArrayList<RecordData> getRecords(long guildId, String type) {
        ArrayList<RecordData> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT UserId, Value, Link FROM UserRecord WHERE GuildId = ? AND Name LIKE ? ORDER BY Value DESC")) {
            stm.setLong(1, guildId);
            stm.setString(2, type);
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()) {
                    list.add(new RecordData(set.getLong("UserId"), type, set.getDouble("Value"), set.getString("Link")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }

    public ArrayList<RecordData> getRecords(String type) {
        ArrayList<RecordData> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT UserId, Value, Link FROM UserRecord WHERE Name LIKE ? ORDER BY Value DESC")) {
            stm.setString(1, type);
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()) {
                    long userId = set.getLong(1);
                    if (list.stream().noneMatch(r -> r.getUserId() == userId))
                        list.add(new RecordData(set.getLong("UserId"), type, set.getDouble("Value"), set.getString("Link")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }

    public ArrayList<String> getRecordTypes() {
        ArrayList<String> types = null;
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Name FROM Record");
             ResultSet set = stm.executeQuery()) {
            types = new ArrayList<>();
            while (set.next()) {
                types.add(set.getString(1));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return types;
    }

    public boolean isInt(String record) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT IsInt FROM Record WHERE Name LIKE ?")) {
            stm.setString(1, record);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()) {
                    return set.getBoolean(1);
                }

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public int getStreak(long guildId, long userId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT CurrentStreak FROM Member WHERE GuildId LIKE ? AND UserId LIKE ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()) {
                    return set.getInt(1);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public void setStreak(long guildId, long userId, int value, String link) { //Addstreak would be more useful
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Member SET CurrentStreak = ? WHERE GuildId = ? AND UserId = ?;" +
                                                                   "INSERT INTO UserRecord(UserId, GuildId, Name, Value, Link) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Link = IF(Value < ?, ?, Link), Value = GREATEST(Value, ?);"
             )) {
            stm.setInt(1, value);
            stm.setLong(2, guildId);
            stm.setLong(3, userId);
            stm.setLong(4, userId);
            stm.setLong(5, guildId);
            stm.setString(6, value > 0 ? "bj_win_streak" : "bj_loss_streak");
            stm.setInt(7, Math.abs(value));
            stm.setString(8, link);
            stm.setInt(9, Math.abs(value));
            stm.setString(10, link);
            stm.setInt(11, Math.abs(value));
            stm.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    //</editor-fold>


    //<editor-fold desc="Settings Code">

    private int extra(boolean multiple) {
        return !multiple ? 3 : 0;
    }

    public List<Pair<String, String>> getSetting(long guildId, Setting setting, Setting.SubSetting subSetting) {
        ArrayList<Pair<String, String>> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Value, GuildSetting.Type, Multiple FROM GuildSetting " +
                                                                   "INNER JOIN (SELECT * FROM Setting WHERE Name LIKE ? AND ValueType LIKE ? AND Type LIKE ?) " +
                                                                   "AS setting USING(ID) WHERE GuildId = ?;")) {
            stm.setString(1, setting.name() + (subSetting != null ? "_" + subSetting : ""));
            stm.setString(2, subSetting == null ? setting.getValueType().getName() : subSetting.getValueType().getName());
            stm.setString(3, setting.getType());
            stm.setLong(4, guildId);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                list.add(Pair.of(rs.getString(1), rs.getString(2)));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return list;
    }

    public List<Integer> getIntSetting(long guildId, Setting setting, Setting.SubSetting subSetting) {
        List<Pair<String, String>> resultSet = getSetting(guildId, setting, subSetting);

        if (resultSet.size() != 0) {
            return resultSet.stream().map(pair -> Utils.getInt(pair.getLeft())).collect(Collectors.toList());
        }
        return List.of((int) (subSetting == null ? setting.getDefaultValue() : subSetting.getDefaultValue()));
    }

    public List<Integer> getIntSetting(long guildId, Setting setting) {
        return getIntSetting(guildId, setting, null);
    }

    // Bool setting will never be a list?
    public boolean getBoolSetting(long guildId, Setting setting, Setting.SubSetting subSetting) {
        List<Pair<String, String>> resultSet = getSetting(guildId, setting, subSetting);
        if (resultSet.size() != 0) {
            return "1".equalsIgnoreCase(resultSet.get(0).getLeft());
        }
        return (boolean) (subSetting == null ? setting.getDefaultValue() : subSetting.getDefaultValue());
    }

    public boolean getBoolSetting(long guildId, Setting setting) {
        return getBoolSetting(guildId, setting, null);
    }

    public List<String> getStringSetting(long guildId, Setting setting, Setting.SubSetting subSetting) {
        List<Pair<String, String>> resultSet = getSetting(guildId, setting, subSetting);
        if (resultSet.size() != 0) {
            return resultSet.stream().map(Pair::getLeft).collect(Collectors.toList());
        }
        return List.of((String) (subSetting == null ? setting.getDefaultValue() : subSetting.getDefaultValue()));
    }

    public List<String> getStringSetting(long guildId, Setting setting) {
        return getStringSetting(guildId, setting, null);
    }

    public List<Pair<Long, Setting.LongType>> getLongSetting(long guildId, Setting setting, Setting.SubSetting subSetting) {
        List<Pair<String, String>> resultSet = getSetting(guildId, setting, subSetting);
        if (resultSet.size() != 0) {
            return resultSet.stream().map(pair -> Pair.of(Long.parseLong(pair.getLeft()), Setting.LongType.valueOf(pair.getRight()))).collect(Collectors.toList());
        }
        return null;
    }

    public List<Pair<Long, Setting.LongType>> getLongSetting(long guildId, Setting setting) {
        return getLongSetting(guildId, setting, null);
    }

    public boolean getListEnabled(long guildId, Setting setting, Setting.SubSetting subSetting) throws WrongArgumentException {
        if (!(subSetting == null ? setting.isMultiple() : subSetting.isMultiple())) {
            throw new WrongArgumentException("Setting must be multiple");
        }
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Value FROM GuildSetting " +
                                                                   "INNER JOIN (SELECT * FROM Setting WHERE Name LIKE ? AND ValueType LIKE ? AND Type LIKE ?) " +
                                                                   "AS setting USING(ID) WHERE GuildId = ?;")) {
            stm.setString(1, setting.getName() + (subSetting != null ? "_" + subSetting : "") + "_on");
            stm.setString(2, Setting.ValueType.BOOLEAN.getName());
            stm.setString(3, setting.getType());
            stm.setLong(4, guildId);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
        return false;
    }

    private PreparedStatement prepareSetSetting(Connection conn, Setting setting, Setting.SubSetting subSetting, long guildId, Setting.LongType longType) throws SQLException {
        boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
        PreparedStatement stm = conn.prepareCall("SELECT @id := ID FROM Setting WHERE Name LIKE ? AND Type LIKE ? AND ValueType LIKE ?;" +
                                                         (multiple ? "" : "UPDATE GuildSetting SET Value = ?, Type = ? WHERE  GuildId = ? AND ID = @id;") +
                                                         "INSERT IGNORE INTO GuildSetting(GuildId, ID, Value, Type) VALUES(?, @id, ?, ?);");

        String longTypeName = longType == null ? null : longType.name();
        stm.setString(1, setting.getName() + (subSetting != null ? "_" + subSetting : ""));
        stm.setString(2, setting.getType());
        stm.setString(3, subSetting == null ? setting.getValueType().getName() : subSetting.getValueType().getName());
        if (!multiple) {
            stm.setString(5, longTypeName);
            stm.setLong(6, guildId);
        }
        stm.setLong(4 + extra(multiple), guildId);
        stm.setString(6 + extra(multiple), longTypeName);
        return stm;
    }

    private PreparedStatement prepareRemoveSetting(Connection conn, Setting setting, Setting.SubSetting subSetting, long guildId, boolean clear) throws Exception {
        boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
        if (!multiple) {
            throw new WrongArgumentException("Setting must be multiple");
        }
        PreparedStatement stm = conn.prepareCall("SELECT @id := ID FROM Setting WHERE Name LIKE ? AND Type LIKE ? AND ValueType LIKE ?;" +
                                                         "DELETE FROM GuildSetting WHERE GuildId = ? AND ID = @id" + (clear ? "" : "AND Value = ?;"));
        stm.setString(1, setting.getName() + (subSetting != null ? "_" + subSetting : ""));
        stm.setString(2, setting.getType());
        stm.setString(3, subSetting == null ? setting.getValueType().getName() : subSetting.getValueType().getName());
        stm.setLong(4, guildId);
        return stm;
    }

    public void setIntSetting(long guildId, Setting setting, Setting.SubSetting subSetting, int value, Boolean clear) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = clear == null ? prepareSetSetting(conn, setting, subSetting, guildId, null)
                     : prepareRemoveSetting(conn, setting, subSetting, guildId, clear)) {
            boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
            if (!multiple)
                stm.setInt(4, value);
            if (clear == null || !clear)
                stm.setInt(5 + extra(multiple), value);
            stm.executeQuery();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setIntSetting(long guildId, Setting setting, int value, Boolean clear) throws Exception {
        setIntSetting(guildId, setting, null, value, clear);
    }

    public void setBoolSetting(long guildId, Setting setting, Setting.SubSetting subSetting, boolean value, Boolean clear) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = clear == null ? prepareSetSetting(conn, setting, subSetting, guildId, null)
                     : prepareRemoveSetting(conn, setting, subSetting, guildId, clear)) {
            boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
            if (!multiple)
                stm.setBoolean(4, value);
            if (clear == null || !clear)
                stm.setBoolean(5 + extra(multiple), value);
            stm.executeQuery();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setBoolSetting(long guildId, Setting setting, boolean value, Boolean clear) throws Exception {
        setBoolSetting(guildId, setting, null, value, clear);
    }

    public void setStringSetting(long guildId, Setting setting, Setting.SubSetting subSetting, String value, Boolean clear) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = clear == null ? prepareSetSetting(conn, setting, subSetting, guildId, null)
                     : prepareRemoveSetting(conn, setting, subSetting, guildId, clear)) {
            boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
            if (!multiple)
                stm.setString(4, value);
            if (clear == null || !clear)
                stm.setString(5 + extra(multiple), value);
            stm.executeQuery();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setStringSetting(long guildId, Setting setting, String value, Boolean clear) throws Exception {
        setStringSetting(guildId, setting, null, value, clear);
    }

    public void setLongSetting(long guildId, Setting setting, Setting.SubSetting subSetting, long value, Setting.LongType longType, Boolean clear) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = clear == null ? prepareSetSetting(conn, setting, subSetting, guildId, longType)
                     : prepareRemoveSetting(conn, setting, subSetting, guildId, clear)) {
            boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
            if (!multiple)
                stm.setLong(4, value);
            if (clear == null || !clear)
                stm.setLong(5 + extra(multiple), value);
            stm.executeQuery();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setLongSetting(long guildId, Setting setting, long value, Setting.LongType longType, Boolean clear) throws Exception {
        setLongSetting(guildId, setting, null, value, longType, clear);
    }

    public void setListEnabled(long guildId, Setting setting, Setting.SubSetting subSetting, boolean enabled) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareCall("SELECT @id := ID FROM Setting WHERE Name LIKE ? AND Type LIKE ? AND ValueType LIKE ?;" +
                                                              "UPDATE GuildSetting SET Value = ? WHERE GuildId = ? AND ID = @id;" +
                                                              "INSERT IGNORE INTO GuildSetting(GuildId, ID, Value) VALUES(?, @id, ?);");
        ) {
            stm.setString(1, setting.getName() + (subSetting != null ? "_" + subSetting : "") + "_on");
            stm.setString(2, setting.getType());
            stm.setString(3, Setting.ValueType.BOOLEAN.getName());
            stm.setBoolean(4, enabled);
            stm.setLong(5, guildId);
            stm.setLong(6, guildId);
            stm.setBoolean(7, enabled);
            stm.executeQuery();
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    public void setListEnabled(long guildId, Setting setting, boolean enabled) {
        setListEnabled(guildId, setting, null, enabled);
    }

    public void setCooldown(long guildId, long userId, Setting setting, LocalDateTime time) {
        if (getIntSetting(guildId, setting, Setting.SubSetting.COOLDOWN).get(0) != 0) {
            try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
                 PreparedStatement stm = conn.prepareStatement("SELECT @id := ID FROM Setting WHERE Name LIKE ? AND Type LIKE ? AND ValueType LIKE ?;" +
                                                                       "INSERT INTO Cooldown(GuildId, UserId, Setting, Time) VALUES(?, ?, @id, ?) ON DUPLICATE KEY UPDATE Time = ?;")) {
                stm.setString(1, setting.getName());
                stm.setString(2, setting.getType());
                stm.setString(3, setting.getValueType().getName());
                stm.setLong(4, guildId);
                stm.setLong(5, userId);
                stm.setTimestamp(6, Timestamp.valueOf(time));
                stm.setTimestamp(7, Timestamp.valueOf(time));
                stm.executeQuery();
            } catch (SQLException exc) {
                exc.printStackTrace();
            }
        }
    }

    public LocalDateTime getCooldown(long guildId, long userId, Setting setting) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT @id := ID FROM Setting WHERE Name LIKE ? AND Type LIKE ? AND ValueType LIKE ?;" +
                                                                   "SELECT Time FROM Cooldown WHERE Setting = @id AND GuildId = ? AND UserId = ?")) {
            stm.setString(1, setting.getName());
            stm.setString(2, setting.getType());
            stm.setString(3, setting.getValueType().getName());
            stm.setLong(4, guildId);
            stm.setLong(5, userId);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp(1).toLocalDateTime();
            }
        } catch (SQLException exc) {
            exc.printStackTrace();
        }
        return null;
    }


    //</editor-fold


    public PrettyTable executeQuery(String query) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, nomultiproperties);
             PreparedStatement stm = conn.prepareStatement(query)) {
            try (ResultSet set = stm.executeQuery()) {
                ResultSetMetaData data = set.getMetaData();
                int columnCOunt = data.getColumnCount();
                String[] headers = new String[columnCOunt];
                for (int i = 0; i < columnCOunt; i++) {
                    headers[i] = data.getColumnName(i + 1);
                }
                PrettyTable table = new PrettyTable(headers);
                while (set.next()) {
                    String[] values = new String[columnCOunt];
                    for (int i = 0; i < columnCOunt; i++) {
                        Object obj = set.getObject(i + 1);
                        values[i] = obj == null ? "null" : String.valueOf(obj);
                    }
                    table.addRow(values);
                }

                return table;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public int executeUpdate(String query) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement(query)) {
            return stm.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

}
