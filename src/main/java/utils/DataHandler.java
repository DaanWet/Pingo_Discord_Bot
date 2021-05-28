package utils;

import commands.settings.Setting;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.sk.PrettyTable;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;


@SuppressWarnings("unchecked")
public class DataHandler {

    private final String JDBC_URL = "jdbc:mysql://localhost:3306/pingo?character_set_server=utf8mb4";
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
        createDatabase();
    }

    public static void setUserId(String userId) {
        USER_ID = userId;
    }

    public static void setPASSWD(String PASSWD) {
        DataHandler.PASSWD = PASSWD;
    }

    private void createDatabase() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement setuptable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS Record (Type VARCHAR(50) NOT NULL PRIMARY KEY, IsInt BOOLEAN DEFAULT TRUE);" +
                                                                          "CREATE TABLE IF NOT EXISTS Member (UserId BIGINT NOT NULL, GuildId BIGINT NOT NULL, Credits INT DEFAULT 0, LastDaily TIMESTAMP, LastWeekly TIMESTAMP, Experience INT DEFAULT 0, PRIMARY KEY(UserId, GuildId)); " +
                                                                          "CREATE TABLE IF NOT EXISTS RoleAssign (Name VARCHAR(255) NOT NULL, GuildId BIGINT NOT NULL, ChannelId BIGINT, MessageId BIGINT, PRIMARY KEY(Name, GuildId));" +
                                                                          "CREATE TABLE IF NOT EXISTS Role (RoleId BIGINT NOT NULL, Name VARCHAR(255) NOT NULL, Emoji VARCHAR(255) NOT NULL, Type VARCHAR(255) NOT NULL, GuildId BIGINT NOT NULL, FOREIGN KEY (Type, GuildId) REFERENCES RoleAssign(Name, GuildId), PRIMARY KEY (Emoji, Type, GuildId));" +
                                                                          "CREATE TABLE IF NOT EXISTS UserRecord (UserId BIGINT NOT NULL, GuildId BIGINT NOT NULL, Name VARCHAR(50) NOT NULL, Link VARCHAR(255), Value DOUBLE NOT NULL, PRIMARY KEY(UserId, GuildId, Name), FOREIGN KEY(UserId, GuildId) REFERENCES Member(UserId, GuildId), FOREIGN KEY (Name) REFERENCES Record(Type));" +
                                                                          "CREATE TABLE IF NOT EXISTS Setting (ID INT AUTO_INCREMENT PRIMARY KEY,  Name VARCHAR(50) NOT NULL, ValueType VARCHAR(10) NOT NULL, Type VARCHAR(50), Multiple BOOLEAN NOT NULL,  UNIQUE (Name, Type));" +
                                                                          "CREATE TABLE IF NOT EXISTS GuildSetting (GuildId BIGINT NOT NULL, ID INT NOT NULL, Value VARCHAR(255) NOT NULL, Type VARCHAR(50), FOREIGN KEY(ID) REFERENCES Settings(ID), PRIMARY KEY(GuildId, ID, Value));" +
                                                                          "INSERT IGNORE INTO Record VALUES ('highest_credits', TRUE);" +
                                                                          "INSERT IGNORE INTO Record VALUES ('biggest_bj_win', TRUE);" +
                                                                          "INSERT IGNORE INTO Record VALUES ('biggest_bj_lose', TRUE);" +
                                                                          "INSERT IGNORE INTO Record VALUES ('bj_win_rate', FALSE);" +
                                                                          "INSERT IGNORE INTO Record VALUES ('bj_games_played', TRUE);")
        ) {
            setuptable.executeUpdate();
            for (Setting s : Setting.values()) {
                PreparedStatement stm = conn.prepareStatement("INSERT IGNORE INTO Setting(Name, ValueType, Type, Multiple) VALUES(?, ?, ?, ?)");
                stm.setString(1, s.name());
                stm.setString(2, s.getValueType());
                stm.setString(3, s.getType());
                stm.setBoolean(4, s.isMultiple());
                stm.executeUpdate();
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

    //TODO: Change return type
    public ArrayList<Triple<String, String, Long>> getRoles(long guildID, String type) {
        ArrayList<Triple<String, String, Long>> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmn = conn.prepareStatement("SELECT Name, Emoji, RoleId FROM Role WHERE GuildId = ? AND Type LIKE ?")) {
            stmn.setLong(1, guildID);
            stmn.setString(2, type);
            try (ResultSet set = stmn.executeQuery()) {
                while (set.next()) {
                    list.add(Triple.of(set.getString("Emoji"), set.getString("Name"), set.getLong("RoleId")));
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

    public long[] getMessage(long guildId, String type) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmn = conn.prepareStatement("SELECT ChannelId, MessageId FROM RoleAssign WHERE GuildId = ? AND Name LIKE ? AND ChannelId IS NOT NULL AND MessageId IS NOT NULL")) {
            stmn.setLong(1, guildId);
            stmn.setString(2, type);
            try (ResultSet set = stmn.executeQuery()) {
                if (set.next()) {
                    return new long[]{set.getLong("ChannelId"), set.getLong("MessageId")};
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public boolean addRoleAssign(long guildId, String type, String emoji, String name, long roleId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("INSERT IGNORE INTO RoleAssign (Name, GuildId) VALUES (?, ?);" +
                                                                   "INSERT IGNORE INTO Role VALUES (?, ?, ?, ?, ?)")
        ) {
            stm.setLong(2, guildId);
            stm.setLong(3, roleId);
            stm.setLong(7, guildId);
            stm.setString(1, type);
            stm.setString(4, name);
            stm.setString(5, emoji);
            stm.setString(6, type);
            int i = stm.executeUpdate();
            return i != 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
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
             PreparedStatement stm = conn.prepareStatement("SELECT Credits, UserId FROM Member WHERE GuildId = ?")
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
             PreparedStatement stm = conn.prepareStatement("INSERT INTO UserRecord VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Value = " + (ignore ? "?" : "GREATEST(Value, ?)"))) {
            stm.setLong(1, userId);
            stm.setLong(2, guildId);
            stm.setString(3, record);
            stm.setString(4, link);
            stm.setDouble(5, value);
            stm.setDouble(6, value);
            stm.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Pair<Double, String> getRecord(long guildId, long userId, String record) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Value, Link FROM UserRecord WHERE GuildId = ? AND UserId = ? AND Name LIKE ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            stm.setString(3, record);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()) {
                    return Pair.of(set.getDouble("Value"), set.getString("Link"));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public HashMap<String, Pair<Double, String>> getRecords(long guildId, long userId) {
        HashMap<String, Pair<Double, String>> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Name, Value, Link FROM UserRecord WHERE GuildId = ? AND UserId = ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()) {
                    map.put(set.getString("Name"), Pair.of(set.getDouble("Value"), set.getString("Link")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return map;
    }

    //TODO: Rework return type
    //current type: record, <uuid, value, link>
    public HashMap<String, Triple<Long, Double, String>> getRecords(long guildId) {
        HashMap<String, Triple<Long, Double, String>> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT a.UserId, a.Name, a.Value, a.Link FROM UserRecord a INNER JOIN (SELECT Name, MAX(Value) AS Max FROM UserRecord WHERE GuildId = ? GROUP BY Name)  AS m ON a.Name = m.Name and a.Value = m.max")) {
            stm.setLong(1, guildId);
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()) {
                    map.put(set.getString(2), Triple.of(set.getLong(1), set.getDouble(3), set.getString(4)));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return map;
    }

    public HashMap<Long, Pair<Double, String>> getRecords(long guildId, String type) {
        HashMap<Long, Pair<Double, String>> map = null;
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT UserId, Value, Link FROM UserRecord WHERE GuildId = ? AND Name LIKE ?")) {
            stm.setLong(1, guildId);
            stm.setString(2, type);
            try (ResultSet set = stm.executeQuery()) {
                map = new HashMap<>();
                while (set.next()) {
                    map.put(set.getLong("UserId"), Pair.of(set.getDouble("Value"), set.getString("Link")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return map;
    }

    public HashMap<Long, Pair<Double, String>> getRecord(long guildId, String record) {
        HashMap<Long, Pair<Double, String>> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Name, Value, Link, UserId FROM UserRecord WHERE GuildId = ? AND Name = ?")) {
            stm.setLong(1, guildId);
            stm.setString(2, record);
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()) {
                    map.put(set.getLong("UserId"), Pair.of(set.getDouble("Value"), set.getString("Link")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return map;
    }

    public ArrayList<String> getRecordTypes() {
        ArrayList<String> types = null;
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Type FROM Record");
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
             PreparedStatement stm = conn.prepareStatement("SELECT IsInt FROM Record WHERE Type LIKE ?")) {
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

    //</editor-fold>


    //<editor-fold desc="Settings Code">

    public ResultSet getSetting(long guildId, Setting setting) throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Value, GuildSetting.Type, Multiple FROM GuildSetting " +
                                                                   "INNER JOIN (SELECT * FROM Setting WHERE Name LIKE ? AND ValueType LIKE ? AND Type LIKE ?) " +
                                                                   "AS setting USING(ID) WHERE GuildId = ?;")) {
            stm.setString(1, setting.name());
            stm.setString(2, setting.getValueType());
            stm.setString(3, setting.getType());
            stm.setLong(4, guildId);
            return stm.executeQuery();
        }
    }

    public Integer getIntSetting(long guildId, Setting setting) {
        try (ResultSet resultSet = getSetting(guildId, setting)) {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public Boolean getBoolSetting(long guildId, Setting setting) {
        try (ResultSet resultSet = getSetting(guildId, setting)) {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getBoolean(1);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public String getStringSetting(long guildId, Setting setting) {
        try (ResultSet resultSet = getSetting(guildId, setting)) {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public Pair<Long, String> getLongSetting(long guildId, Setting setting) {
        try (ResultSet resultSet = getSetting(guildId, setting)) {
            if (resultSet != null && resultSet.next()) {
                return Pair.of(resultSet.getLong(1), resultSet.getString(2));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private PreparedStatement prepareSetSetting(Connection conn) throws SQLException {
        return conn.prepareCall("SELECT @id := ID FROM Setting WHERE Name LIKE ? AND Type LIKE ? AND ValueType LIKE ?" +
                                        ";UPDATE GuildSetting SET Value = ? WHERE  GuildId = ? AND ID = @id;" +
                                        "INSERT IGNORE INTO GuildSetting(GuildId, ID, Value, Type) VALUES(?, @id, ?, null);");
    }

    public void setIntSetting(long guildId, Setting setting, int value) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = prepareSetSetting(conn)) {
            stm.setString(1, setting.getName());
            stm.setString(2, setting.getType());
            stm.setString(3, setting.getValueType());
            stm.setInt(4, value);
            stm.setLong(5, guildId);
            stm.setLong(6, guildId);
            stm.setInt(7, value);
            stm.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setBoolSetting(long guildId, Setting setting, boolean value) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = prepareSetSetting(conn)) {
            stm.setString(1, setting.getName());
            stm.setString(2, setting.getType());
            stm.setString(3, setting.getValueType());
            stm.setBoolean(4, value);
            stm.setLong(5, guildId);
            stm.setLong(6, guildId);
            stm.setBoolean(7, value);
            stm.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setStringSetting(long guildId, Setting setting, String value) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = prepareSetSetting(conn)) {
            stm.setString(1, setting.getName());
            stm.setString(2, setting.getType());
            stm.setString(3, setting.getValueType());
            stm.setString(4, value);
            stm.setLong(5, guildId);
            stm.setLong(6, guildId);
            stm.setString(7, value);
            stm.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setLongSetting(long guildId, Setting setting, long value) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = prepareSetSetting(conn)) {
            stm.setString(1, setting.getName());
            stm.setString(2, setting.getType());
            stm.setString(3, setting.getValueType());
            stm.setLong(4, value);
            stm.setLong(5, guildId);
            stm.setLong(6, guildId);
            stm.setLong(7, value);
            stm.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
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
