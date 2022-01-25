package data.handlers;


import com.mysql.cj.exceptions.WrongArgumentException;
import commands.settings.Setting;
import data.handlers.DataHandler;
import data.models.RecordData;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.sk.PrettyTable;
import utils.Utils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class GeneralDataHandler extends DataHandler {

    public GeneralDataHandler(){
        super();
    }

    public void createDatabase() throws SQLException{
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement setuptable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS Record (ID INT, Name VARCHAR(50) NOT NULL PRIMARY KEY, IsInt BOOLEAN DEFAULT TRUE);" +
                                                                          "CREATE TABLE IF NOT EXISTS Member (UserId BIGINT NOT NULL, GuildId BIGINT NOT NULL, Credits INT DEFAULT 0, LastDaily TIMESTAMP, LastWeekly TIMESTAMP, Experience INT DEFAULT 0, CurrentStreak  INT DEFAULT 0, PRIMARY KEY(UserId, GuildId)); " +
                                                                          "CREATE TABLE IF NOT EXISTS RoleAssign (Name VARCHAR(255) NOT NULL, GuildId BIGINT NOT NULL, ChannelId BIGINT, MessageId BIGINT, Sorting VARCHAR(20) DEFAULT 'NONE', Compacting VARCHAR(20) DEFAULT 'NORMAL', Title VARCHAR(255), PRIMARY KEY(Name, GuildId));" +
                                                                          "CREATE TABLE IF NOT EXISTS Role (RoleId BIGINT NOT NULL, Name VARCHAR(255) NOT NULL, Emoji VARCHAR(255) NOT NULL, Type VARCHAR(255) NOT NULL, GuildId BIGINT NOT NULL, FOREIGN KEY (Type, GuildId) REFERENCES RoleAssign(Name, GuildId), PRIMARY KEY (Emoji, Type, GuildId));" +
                                                                          "CREATE TABLE IF NOT EXISTS UserRecord (UserId BIGINT NOT NULL, GuildId BIGINT NOT NULL, Name VARCHAR(50) NOT NULL, Link VARCHAR(255), Value DOUBLE NOT NULL, PRIMARY KEY(UserId, GuildId, Name), FOREIGN KEY(UserId, GuildId) REFERENCES Member(UserId, GuildId), FOREIGN KEY (Name) REFERENCES Record(Name));" +
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
            for (Setting s : Setting.values()){
                PreparedStatement stm = conn.prepareStatement("INSERT IGNORE INTO Setting(Name, ValueType, Type, Multiple) VALUES(?, ?, ?, ?)");
                stm.setString(1, s.name());
                stm.setString(2, s.getValueType().getName());
                stm.setString(3, s.getType());
                stm.setBoolean(4, s.isMultiple());
                stm.executeUpdate();
                if (s.isMultiple()){
                    PreparedStatement stm1 = conn.prepareStatement("INSERT IGNORE INTO Setting(Name, ValueType, Type, Multiple) VALUES(?, ?, ?, ?)");
                    stm1.setString(1, s.name() + "_on");
                    stm1.setString(2, Setting.ValueType.BOOLEAN.getName());
                    stm1.setString(3, s.getType());
                    stm1.setBoolean(4, false);
                    stm1.executeUpdate();
                }
                for (Setting.SubSetting subs : s.getSubSettings()){
                    PreparedStatement stmn = conn.prepareStatement("INSERT IGNORE INTO Setting(Name, ValueType, Type, Multiple) VALUES(?, ?, ?, ?)");
                    stmn.setString(1, String.format("%s_%s", s.getName(), subs));
                    stmn.setString(2, subs.getValueType().getName());
                    stmn.setString(3, s.getType());
                    stmn.setBoolean(4, subs.isMultiple());
                    stmn.executeUpdate();
                    if (subs.isMultiple()){
                        PreparedStatement stmn1 = conn.prepareStatement("INSERT IGNORE INTO Setting(Name, ValueType, Type, Multiple) VALUES(?, ?, ?, ?)");
                        stmn1.setString(1, String.format("%s_%s_on", s.getName(), subs));
                        stmn1.setString(2, Setting.ValueType.BOOLEAN.getName());
                        stmn1.setString(3, s.getType());
                        stmn1.setBoolean(4, false);
                        stmn1.executeUpdate();
                    }
                }
            }
        }
    }

    //<editor-fold desc = "Experience">
    public void setXP(long guildID, long userId, int xp){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Member SET Experience = ? WHERE GuildId = ? AND UserId = ?")) {
            stm.setInt(1, xp);
            stm.setLong(2, guildID);
            stm.setLong(3, userId);
            stm.executeUpdate();
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
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
            try (ResultSet set = stmn.executeQuery()) {
                return set.getInt("Experience");
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return -1;
    }

    public int getXP(long guildId, long userId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT Experience FROM Member WHERE GuildId = ? AND UserId = ?")) {
            stmn.setLong(1, guildId);
            stmn.setLong(2, userId);
            try (ResultSet set = stmn.executeQuery()) {
                return set.getInt("Experience");
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return -1;
    }

    public HashMap<Long, Integer> getAllXp(long guildId){
        HashMap<Long, Integer> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT Experience, UserId FROM Member WHERE GuildId = ?")) {
            stmn.setLong(1, guildId);
            try (ResultSet set = stmn.executeQuery()) {
                while (set.next()){
                    map.put(set.getLong("UserId"), set.getInt("Experience"));
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return map;
    }

    //</editor-fold>
    public PrettyTable executeQuery(String query){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, nomultiproperties);
             PreparedStatement stm = conn.prepareStatement(query)) {
            try (ResultSet set = stm.executeQuery()) {
                ResultSetMetaData data = set.getMetaData();
                int columnCOunt = data.getColumnCount();
                String[] headers = new String[columnCOunt];
                for (int i = 0; i < columnCOunt; i++){
                    headers[i] = data.getColumnName(i + 1);
                }
                PrettyTable table = new PrettyTable(headers);
                while (set.next()){
                    String[] values = new String[columnCOunt];
                    for (int i = 0; i < columnCOunt; i++){
                        Object obj = set.getObject(i + 1);
                        values[i] = obj == null ? "null" : String.valueOf(obj);
                    }
                    table.addRow(values);
                }

                return table;
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
            return null;
        }
    }

    public int executeUpdate(String query){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement(query)) {
            return stm.executeUpdate();
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return -1;
    }

}
