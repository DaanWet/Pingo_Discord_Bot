package utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.sk.PrettyTable;


@SuppressWarnings("unchecked")
public class DataHandler {

    private final String PATH = "./Data.json"; //"src/main/resources/Data.json"; //
    private final String JDBC_URL = "jdbc:mysql://localhost:3306/pingo";
    private JSONObject jsonObject;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-dd-MM HH-mm-ss");
    private static String USER_ID;
    private static String PASSWD;
    private Properties properties;

    public DataHandler() {
        //openfile();
        properties = new Properties();
        properties.setProperty("user", USER_ID);
        properties.setProperty("password", PASSWD);
        properties.setProperty("allowMultiQueries", "true");
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
                     "INSERT IGNORE INTO Record VALUES ('highest_credits', TRUE);" +
                     "INSERT IGNORE INTO Record VALUES ('biggest_bj_win', TRUE);" +
                     "INSERT IGNORE INTO Record VALUES ('biggest_bj_lose', TRUE);" +
                     "INSERT IGNORE INTO Record VALUES ('bj_win_rate', FALSE);" +
                     "INSERT IGNORE INTO Record VALUES ('bj_games_played', TRUE);")
        ) {
            setuptable.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void openfile() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(PATH)) {
            jsonObject = ((JSONObject) parser.parse(reader));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    //<editor-fold desc="RoleAssign Code">

    public ArrayList<String> getRoleCategories() {
        openfile();
        ArrayList<String> list = new ArrayList<>();
        for (Object key : jsonObject.keySet()) {
            String k = ((String) key);
            if (k.contains("-roles")) {
                list.add(k.replace("-roles", ""));
            }
        }
        return list;
    }

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

    public ArrayList<Pair<Long, Long>> getRoleMessages() {
        openfile();
        ArrayList<Pair<Long, Long>> list = new ArrayList<>();
        for (Object key : jsonObject.keySet()) {
            String k = ((String) key);
            if (k.contains("-roles")) {
                JSONObject roleobject = (JSONObject) jsonObject.get(key);
                if (roleobject.containsKey("channel"))
                    list.add(Pair.of((long) roleobject.get("channel"), (long) roleobject.get("message")));
            }
        }
        return list;
    }

    public ArrayList<JSONObject> getRoles(String type) {
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        if (!jsonObject.containsKey(key)) return null;
        return (JSONArray) ((JSONObject) jsonObject.get(key)).get("roles");

    }
    //TODO: Change return type
    public ArrayList<Triple<String, String, Long>> getRoles(long guildID, String type){
        ArrayList<Triple<String, String, Long>> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmn = conn.prepareStatement("SELECT Name, Emoji, RoleId FROM Role WHERE GuildId = ? AND Type LIKE ?")){
            stmn.setLong(1, guildID);
            stmn.setString(2, type);
            try (ResultSet set = stmn.executeQuery()){
                while (set.next()){
                    list.add(Triple.of(set.getString("Emoji"), set.getString("Name"), set.getLong("RoleId")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }

    public boolean setMessage(String type, long channelId, long messageId) {
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        if (!jsonObject.containsKey(key)) return false;
        JSONObject object = (JSONObject) jsonObject.get(key);
        object.put("channel", channelId);
        object.put("message", messageId);
        save();
        return true;
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

    public long[] getMessage(String type) {
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        if (!jsonObject.containsKey(key)) return null;
        JSONObject roleobject = (JSONObject) jsonObject.get(key);
        if (!roleobject.containsKey("channel")) return null;
        return new long[]{(long) roleobject.get("channel"), (long) roleobject.get("message")};
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

    public void addRoleAssign(String type, String emoji, String name, long roleId) {
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        jsonObject.putIfAbsent(key, new JSONObject(Map.of("roles", new JSONArray())));
        JSONArray roles = (JSONArray) ((JSONObject) jsonObject.get(key)).get("roles");
        JSONObject ra = new JSONObject();
        ra.put("emoji", emoji);
        ra.put("name", name);
        ra.put("role", roleId);
        roles.add(ra);
        save();

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

    public boolean removeRoleAssign(String type, String emoji) {
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        boolean found = false;
        if (jsonObject.containsKey(key)) {
            JSONArray roles = (JSONArray) ((JSONObject) jsonObject.get(key)).get("roles");
            int i = 0;
            while (!found && i < roles.size()) {
                if (((JSONObject) roles.get(i)).get("emoji").equals(emoji)) {
                    found = true;
                } else {
                    i++;
                }
            }
            if (found) {
                roles.remove(i);
            }
        }
        save();
        return found;
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

    public boolean removeRoleAssign(String type, long roleid) {
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        boolean found = false;
        if (jsonObject.containsKey(key)) {
            JSONArray roles = (JSONArray) ((JSONObject) jsonObject.get(key)).get("roles");
            int i = 0;
            while (!found && i < roles.size()) {
                if (((long) ((JSONObject) roles.get(i)).get("role")) == (roleid)) {
                    found = true;
                } else {
                    i++;
                }
            }
            if (found) {
                roles.remove(i);
            }
        }
        save();
        return found;

    }
    //</editor-fold>

    public void createUser(String userid) {
        openfile();
        jsonObject.putIfAbsent("casino", new JSONObject());
        JSONObject casino = (JSONObject) jsonObject.get("casino");
        JSONObject user = (JSONObject) casino.getOrDefault(userid, new JSONObject());
        user.putIfAbsent("credits", 0);
        user.putIfAbsent("last_cred_collect", dtf.format(LocalDateTime.now().minusDays(1)));
        user.putIfAbsent("last_weekly_collect", dtf.format(LocalDateTime.now().minusDays(7)));
        user.putIfAbsent("experience", 0);
        user.putIfAbsent("records", new JSONObject());
        casino.put(userid, user);
        save();
    }
    //<editor-fold desc="Credits code">
    public int getCredits(String userid) {
        openfile();
        int credits = 0;
        if (jsonObject.containsKey("casino")) {
            JSONObject casino = (JSONObject) jsonObject.get("casino");
            if (casino.containsKey(userid)) {
                JSONObject userobject = (JSONObject) casino.get(userid);
                if (userobject.containsKey("credits")) {
                    credits = (int) (long) userobject.get("credits");
                }
            }
        }
        return credits;
    }

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

    public HashMap<String, Integer> getAllCredits() {
        openfile();
        HashMap<String, Integer> map = new HashMap<>();
        if (jsonObject.containsKey("casino")) {
            JSONObject casino = (JSONObject) jsonObject.get("casino");
            for (Object key : casino.keySet()) {
                map.put((String) key, (int) (long) ((JSONObject) casino.get(key)).get("credits"));
            }
        }
        return map;
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

    public void setCredits(String userid, int credits) {
        openfile();
        if (!jsonObject.containsKey("casino") || !((JSONObject) jsonObject.get("casino")).containsKey(userid)) {
            createUser(userid);
        }

        JSONObject user = (JSONObject) ((JSONObject) jsonObject.get("casino")).get(userid);
        user.putIfAbsent("records", new JSONObject());
        JSONObject records = ((JSONObject) user.get("records"));
        if ((int) (long) ((JSONObject) records.getOrDefault("highest_credits", new JSONObject(Map.of("value", 0L)))).get("value") < credits) {

            records.put("highest_credits", new JSONObject(Map.of("value", credits)));
            ;
        }
        user.put("credits", credits);
        save();
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

    public int addCredits(String userid, int credits) {
        int c = getCredits(userid) + credits;
        setCredits(userid, c);
        return c;
    }

    public int addCredits(long guildId, long userId, int credits) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("INSERT IGNORE INTO Member(UserId, GuildId, LastDaily, LastWeekly) VALUES(?, ?, ?, ?);" + //TODO Fix on duplicate
                     "UPDATE Member SET Credits = Credits + ? WHERE GuildId = ? AND UserId = ?;" +
                     "INSERT INTO UserRecord(UserId, GuildId, Name, Value) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE Value = GREATEST(Value, VALUE + ?);");
             PreparedStatement stmnt2 = conn.prepareStatement("SELECT Credits FROM Member  WHERE GuildId = ? AND UserId = ?");
        ) {
            stm.setLong(1, userId);
            stm.setLong(2, guildId);
            stm.setLong(6, guildId);
            stm.setLong(7, userId);
            stm.setLong(8, userId);
            stm.setLong(9, guildId);
            LocalDateTime now = LocalDateTime.now();
            stm.setTimestamp(3, Timestamp.valueOf(now.minusDays(1)));
            stm.setTimestamp(4, Timestamp.valueOf(now.minusDays(7)));
            stm.setInt(5, credits);
            stm.setInt(11, credits);
            stm.setInt(12, credits);
            stm.setString(10, "highest_credits");
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

    public LocalDateTime getLatestCollect(String userid) {
        openfile();
        LocalDateTime date = LocalDateTime.now().minusDays(1).minusMinutes(1);
        if (jsonObject.containsKey("casino")) {
            JSONObject casino = (JSONObject) jsonObject.get("casino");
            if (casino.containsKey(userid)) {
                JSONObject userobject = (JSONObject) casino.get(userid);
                date = LocalDateTime.from(dtf.parse((String) userobject.get("last_cred_collect")));
            }
        }
        return date;
    }

    public LocalDateTime getLatestCollect(long guildId, long userId) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT LastDaily FROM Member WHERE GuildId = ? AND UserId = ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try(ResultSet set = stm.executeQuery()){
                if (set.next()){
                    return set.getTimestamp("LastDaily").toLocalDateTime();
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public LocalDateTime getLatestWeekCollect(String userid) {
        openfile();
        LocalDateTime date = LocalDateTime.now().minusDays(7).minusMinutes(1);
        if (jsonObject.containsKey("casino")) {
            JSONObject casino = (JSONObject) jsonObject.get("casino");
            if (casino.containsKey(userid)) {
                JSONObject userobject = (JSONObject) casino.get(userid);
                if (userobject.containsKey("last_weekly_collect")) {
                    date = LocalDateTime.from(dtf.parse((String) userobject.get("last_weekly_collect")));
                }

            }
        }
        return date;
    }
    public LocalDateTime getLatestWeekCollect(long guildId, long userId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT LastWeekly FROM Member WHERE GuildId = ? AND UserId = ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try(ResultSet set = stm.executeQuery()){
                if (set.next()){
                    return set.getTimestamp("LastWeekly").toLocalDateTime();
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public void setLatestWeekCollect(String userid, LocalDateTime time) {
        openfile();
        if (!jsonObject.containsKey("casino") || !((JSONObject) jsonObject.get("casino")).containsKey(userid)) {
            createUser(userid);
        }
        JSONObject user = (JSONObject) ((JSONObject) jsonObject.get("casino")).get(userid);
        user.put("last_weekly_collect", dtf.format(time));
        save();
    }
    public void setLatestWeekCollect(long guild, long userId, LocalDateTime time){
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

    public void setLatestCollect(String userid, LocalDateTime time) {
        openfile();
        if (!jsonObject.containsKey("casino") || !((JSONObject) jsonObject.get("casino")).containsKey(userid)) {
            createUser(userid);
        }

        JSONObject user = (JSONObject) ((JSONObject) jsonObject.get("casino")).get(userid);
        user.put("last_cred_collect", dtf.format(time));
        save();
    }
    public void setLatestCollect(long guildId, long userId, LocalDateTime time){
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
    public void setXP(String userid, int xp) {
        openfile();
        if (!jsonObject.containsKey("casino") || !((JSONObject) jsonObject.get("casino")).containsKey(userid)) {
            createUser(userid);
        }

        JSONObject user = (JSONObject) ((JSONObject) jsonObject.get("casino")).get(userid);
        user.put("experience", xp);
        save();
    }
    public void setXP(long guildID, long userId, int xp){
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

    public int addXP(String userid, int xp) {
        openfile();
        if (!jsonObject.containsKey("casino") || !((JSONObject) jsonObject.get("casino")).containsKey(userid)) {
            createUser(userid);
        }

        JSONObject user = (JSONObject) ((JSONObject) jsonObject.get("casino")).get(userid);
        int x = (int) user.getOrDefault("experience", 0) + xp;
        user.put("experience", x);
        return x;
    }
    public int addXP(long guildID, long userId, int xp){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Member SET Experience = Experience + ? WHERE GuildId = ? AND UserId = ?");
             PreparedStatement stmn = conn.prepareStatement("SELECT Experience FROM Member WHERE GuildId = ? AND UserId = ?")) {
            stm.setInt(1, xp);
            stm.setLong(2, guildID);
            stm.setLong(3, userId);
            stm.executeUpdate();
            stmn.setLong(1, guildID);
            stmn.setLong(2, userId);
            try (ResultSet set = stmn.executeQuery()){
                return set.getInt("Experience");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    public int getXP(String userid) {
        openfile();
        if (!jsonObject.containsKey("casino") || !((JSONObject) jsonObject.get("casino")).containsKey(userid)) {
            createUser(userid);
        }
        JSONObject user = (JSONObject) ((JSONObject) jsonObject.get("casino")).get(userid);
        return (int) user.getOrDefault("experience", 0);
    }
    public int getXP(long guildId, long userId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT Experience FROM Member WHERE GuildId = ? AND UserId = ?")) {
            stmn.setLong(1, guildId);
            stmn.setLong(2, userId);
            try (ResultSet set = stmn.executeQuery()){
                return set.getInt("Experience");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    public HashMap<String, Integer> getAllXP() {
        openfile();
        JSONObject casino = (JSONObject) jsonObject.getOrDefault("casino", new JSONObject());
        HashMap<String, Integer> xp = new HashMap<>();
        for (Object key : casino.keySet()) {
            JSONObject user = (JSONObject) casino.get(key);
            xp.put((String) key, (int) user.getOrDefault("experience", 0));
        }
        return xp;
    }
    public HashMap<Long, Integer> getAllXp(long guildId){
        HashMap<Long, Integer> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT Experience, UserId FROM Member WHERE GuildId = ?")) {
            stmn.setLong(1, guildId);
            try (ResultSet set = stmn.executeQuery()){
                while (set.next()){
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
    public void setRecord(String userid, String record, Comparable value, boolean ignore) {
        setRecord(userid, record, value, null, ignore);
    }
    public void setRecord(long guildId, long userId, String record, double value, boolean ignore){
        setRecord(guildId, userId, record, value, null, ignore);
    }

    public void setRecord(String userid, String record, Comparable value, String link, boolean ignore) {
        JSONObject records = getUserRecords(userid);
        boolean put = false;
        if (!records.containsKey(record)) {
            put = true;
        } else {
            Comparable oldv = (Comparable) ((JSONObject) records.get(record)).get("value");
            if (oldv instanceof Long) {
                oldv = (int) (long) oldv;
            }
            if (oldv.compareTo(value) < 0) {
                put = true;
            }
        }

        if (put || ignore) {
            if (link == null) {
                records.put(record, new JSONObject(Map.of("value", value)));
            } else {
                records.put(record, new JSONObject(Map.of("value", value, "link", link)));
            }
        }
        save();
    }
    public void setRecord(long guildId, long userId, String record, double value, String link, boolean ignore){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("INSERT INTO UserRecord VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Value = " + (ignore ? "?" : "GREATEST(Value, ?)"))){
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

    public Pair<Comparable, String> getRecord(String userid, String record) {
        JSONObject ur = getUserRecords(userid);
        if (ur.containsKey(record)) {
            JSONObject r = (JSONObject) ur.get(record);
            return Pair.of((Comparable) r.get("value"), (String) r.getOrDefault("link", null));
        }
        return null;
    }
    public Pair<Double, String> getRecord(long guildId, long userId, String record){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Value, Link FROM UserRecord WHERE GuildId = ? AND UserId = ? AND Name LIKE ?")){
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            stm.setString(3, record);
            try (ResultSet set = stm.executeQuery()){
                if (set.next()){
                    return Pair.of(set.getDouble("Value"), set.getString("Link"));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public HashMap<String, Pair<Comparable, String>> getRecords(String userid) {
        JSONObject records = getUserRecords(userid);
        HashMap<String, Pair<Comparable, String>> map = new HashMap<>();
        records.forEach((key, value) -> map.put((String) key, Pair.of((Comparable) ((JSONObject) value).get("value"), (String) ((JSONObject) value).get("link"))));
        return map;
    }
    public HashMap<String, Pair<Double, String>> getRecords(long guildId, long userId){
        HashMap<String, Pair<Double, String>> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Name, Value, Link FROM UserRecord WHERE GuildId = ? AND UserId = ?")){
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()){
                while (set.next()){
                    map.put(set.getString("Name"), Pair.of(set.getDouble("Value"), set.getString("Link")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return map;
    }

    //only for DataHandler
    private JSONObject getUserRecords(String userid) {
        openfile();
        if (!jsonObject.containsKey("casino") || !((JSONObject) jsonObject.get("casino")).containsKey(userid)) {
            createUser(userid);
        }
        JSONObject user = (JSONObject) ((JSONObject) jsonObject.get("casino")).get(userid);
        user.putIfAbsent("records", new JSONObject());
        return (JSONObject) user.get("records");
    }


    public HashMap<String, Triple<String, Comparable, String>> getRecords() {
        openfile();
        HashMap<String, Triple<String, Comparable, String>> map = new HashMap<>();
        JSONObject casino = (JSONObject) jsonObject.getOrDefault("casino", new JSONObject());
        for (Object uuid : casino.keySet()) {
            JSONObject records = (JSONObject) ((JSONObject) casino.get(uuid)).getOrDefault("records", new JSONObject());
            for (Object record : records.keySet()) {
                Comparable value = (Comparable) ((JSONObject) records.get(record)).get("value");
                if (!map.containsKey(record) || value.compareTo(map.get(record).getMiddle()) > 0) {
                    map.put((String) record, Triple.of((String) uuid, value, (String) ((JSONObject) records.get(record)).get("link")));
                }
            }
        }
        return map;
    }
    //TODO: Rework return type
    //current type: record, <uuid, value, link>
    public HashMap<String, Triple<Long, Double, String>> getRecords(long guildId){
        HashMap<String, Triple<Long, Double, String>> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT a.UserId, a.Name, a.Value, a.Link FROM UserRecord a INNER JOIN (SELECT Name, MAX(Value) AS Max FROM UserRecord WHERE GuildId = ? GROUP BY Name)  AS m ON a.Name = m.Name and a.Value = m.max")){
            stm.setLong(1, guildId);
            try (ResultSet set = stm.executeQuery()){
                while (set.next()){
                    map.put(set.getString(2), Triple.of(set.getLong(1), set.getDouble(3), set.getString(4)));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return map;
    }

    public ArrayList<Triple<String, Comparable, String>> getRecord(String record) {
        openfile();
        ArrayList<Triple<String, Comparable, String>> list = new ArrayList<>();
        JSONObject casino = (JSONObject) jsonObject.getOrDefault("casino", new JSONObject());
        for (Object uuid : casino.keySet()) {
            JSONObject records = (JSONObject) ((JSONObject) casino.get(uuid)).getOrDefault("records", new JSONObject());
            if (records.containsKey(record)) {
                JSONObject r = (JSONObject) records.get(record);
                list.add(Triple.of((String) uuid, (Comparable) r.get("value"), (String) r.get("link")));
            }
        }
        return list;
    }
    public HashMap<Long, Pair<Double, String>> getRecord(long guildId, String record){
        HashMap<Long, Pair<Double, String>> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Name, Value, Link, UserId FROM UserRecord WHERE GuildId = ? AND Name = ?")){
            stm.setLong(1, guildId);
            stm.setString(2, record);
            try (ResultSet set = stm.executeQuery()){
                while (set.next()){
                    map.put(set.getLong("UserId"), Pair.of(set.getDouble("Value"), set.getString("Link")));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return map;
    }

    public boolean isInt(String record){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT IsInt FROM Record WHERE Type LIKE ?")){
            stm.setString(1, record);
            try(ResultSet set = stm.executeQuery()){
                if (set.next()){
                    return set.getBoolean(1);
                }

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    //</editor-fold>

    void save() {
        try (FileWriter file = new FileWriter(PATH)) {
            file.write(jsonObject.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
