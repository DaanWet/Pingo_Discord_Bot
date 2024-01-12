package me.damascus2000.pingo.data.handlers;

import me.damascus2000.pingo.companions.Record;
import me.damascus2000.pingo.data.models.RecordData;

import java.sql.*;
import java.util.ArrayList;

@Deprecated(forRemoval = true)
public class RecordDataHandler extends DataHandler {

    public RecordDataHandler(){
        super();
    }

    public void setRecord(long guildId, long userId, Record record, double value, boolean ignore){
        setRecord(guildId, userId, record, value, null, ignore);
    }

    public void setRecord(long guildId, long userId, Record record, double value, String link, boolean ignore){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("INSERT INTO UserRecord VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Link = IF(Value < ?, ?, Link), Value = " + (ignore ? "?" : "GREATEST(Value, ?)"))) {
            stm.setLong(1, userId);
            stm.setLong(2, guildId);
            stm.setString(3, record.getName());
            stm.setString(4, link);
            stm.setDouble(5, value);
            stm.setDouble(6, value);
            stm.setString(7, link);
            stm.setDouble(8, value);
            stm.executeUpdate();
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
    }

    public RecordData getRecord(long guildId, long userId, Record record){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Value, Link FROM UserRecord WHERE GuildId = ? AND UserId = ? AND Name LIKE ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            stm.setString(3, record.getName());
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()){
                    return new RecordData(userId, record, set.getDouble("Value"), set.getString("Link"));
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return null;
    }

    public ArrayList<RecordData> getRecords(long guildId, long userId){
        ArrayList<RecordData> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Name, Value, Link FROM UserRecord INNER JOIN Record USING(Name) WHERE GuildId = ? AND UserId = ? ORDER BY ID")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()){
                    list.add(new RecordData(userId, Record.getRecord(set.getString("Name")).get(), set.getDouble("Value"), set.getString("Link")));
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return list;
    }


    public ArrayList<RecordData> getRecords(long guildId){
        ArrayList<RecordData> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT UID, M.Name, Max, Link  FROM (SELECT Name, " +
                                                                   "max(Value) as Max, " +
                                                                   "CAST(SUBSTRING(MAX(CONCAT(LPAD(Value, 11, '0'), UserId)), 12) AS UNSIGNED INTEGER) AS UID," +
                                                                   "SUBSTRING(MAX(CONCAT(LPAD(Value, 11, '0'), Link)), 12) AS Link " +
                                                                   "FROM UserRecord  WHERE GuildId = ? GROUP BY Name) as M INNER JOIN Record USING(Name) ORDER BY ID")) {
            stm.setLong(1, guildId);
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()){
                    list.add(new RecordData(set.getLong(1), Record.getRecord(set.getString(2)).get(), set.getDouble(3), set.getString(4)));
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return list;
    }

    public ArrayList<RecordData> getRecords(){
        ArrayList<RecordData> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT UID, M.Name, Max, Link  FROM (SELECT Name, " +
                                                                   "max(Value) as Max, " +
                                                                   "CAST(SUBSTRING(MAX(CONCAT(LPAD(Value, 11, '0'), UserId)), 12) AS UNSIGNED INTEGER) AS UID," +
                                                                   "SUBSTRING(MAX(CONCAT(LPAD(Value, 11, '0'), Link)), 12) AS Link " +
                                                                   "FROM UserRecord GROUP BY Name) as M INNER JOIN Record USING(Name) ORDER BY ID")) {
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()){
                    list.add(new RecordData(set.getLong(1), Record.getRecord(set.getString(2)).get(), set.getDouble(3), set.getString(4)));
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return list;
    }

    public ArrayList<RecordData> getRecords(long guildId, Record record){
        ArrayList<RecordData> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT UserId, Value, Link FROM UserRecord WHERE GuildId = ? AND Name LIKE ? ORDER BY Value DESC")) {
            stm.setLong(1, guildId);
            stm.setString(2, record.getName());
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()){
                    list.add(new RecordData(set.getLong("UserId"), record, set.getDouble("Value"), set.getString("Link")));
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return list;
    }

    public ArrayList<RecordData> getRecords(Record record){
        ArrayList<RecordData> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT UserId, Value, Link FROM UserRecord WHERE Name LIKE ? ORDER BY Value DESC")) {
            stm.setString(1, record.getName());
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()){
                    long userId = set.getLong(1);
                    if (list.stream().noneMatch(r -> r.getUserId() == userId))
                        list.add(new RecordData(set.getLong("UserId"), record, set.getDouble("Value"), set.getString("Link")));
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return list;
    }

    public int getStreak(long guildId, long userId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT CurrentStreak FROM Member WHERE GuildId LIKE ? AND UserId LIKE ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()){
                    return set.getInt(1);
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return 0;
    }

    public void setStreak(long guildId, long userId, int value, String link){ //Addstreak would be more useful
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Member SET CurrentStreak = ? WHERE GuildId = ? AND UserId = ?;" +
                                                                   "INSERT INTO UserRecord(UserId, GuildId, Name, Value, Link) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Link = IF(Value < ?, ?, Link), Value = GREATEST(Value, ?);"
             )) {
            stm.setInt(1, value);
            stm.setLong(2, guildId);
            stm.setLong(3, userId);
            stm.setLong(4, userId);
            stm.setLong(5, guildId);
            stm.setString(6, (value > 0 ? Record.WIN_STREAK : Record.LOSS_STREAK).getName());
            stm.setInt(7, Math.abs(value));
            stm.setString(8, link);
            stm.setInt(9, Math.abs(value));
            stm.setString(10, link);
            stm.setInt(11, Math.abs(value));
            stm.executeUpdate();
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
    }

}
