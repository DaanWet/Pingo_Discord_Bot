package data.handlers;


import companions.Record;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;

public class CreditDataHandler extends DataHandler {

    public CreditDataHandler(){
        super();
    }

    public int getPlayers(){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Count(*) FROM Member")
        ) {

            try (ResultSet set = stm.executeQuery()) {
                if (set.next()){
                    return set.getInt(1);
                }
            }
        } catch (SQLException throwable){
            throwable.printStackTrace();
        }
        return -1;
    }

    public int getPlayers(long guildId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Count(*) FROM Member WHERE guildId = ?")
        ) {
            stm.setLong(1, guildId);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()){
                    return set.getInt(1);
                }
            }
        } catch (SQLException throwable){
            throwable.printStackTrace();
        }
        return -1;
    }




    public int getCredits(long guildID, long userId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Credits FROM Member WHERE GuildId = ? AND UserId = ?")
        ) {
            stm.setLong(1, guildID);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()){
                    return set.getInt("Credits");
                }
            }
        } catch (SQLException throwable){
            throwable.printStackTrace();
        }
        return 0;
    }


    public HashMap<Long, Integer> getAllCredits(long guildId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Credits, UserId FROM Member WHERE GuildId = ? ORDER BY Credits DESC")
        ) {
            HashMap<Long, Integer> map = new HashMap<>();
            stm.setLong(1, guildId);
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()){
                    map.put(set.getLong("UserId"), set.getInt("Credits"));
                }
            }
            return map;
        } catch (SQLException throwable){
            throwable.printStackTrace();
            return null;
        }
    }

    public HashMap<Long, Integer> getAllCredits(){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT Credits, UserId FROM Member ORDER BY Credits DESC")
        ) {
            HashMap<Long, Integer> map = new HashMap<>();
            try (ResultSet set = stm.executeQuery()) {
                while (set.next()){
                    if (!map.containsKey(set.getLong("UserId")))
                        map.put(set.getLong("UserId"), set.getInt("Credits"));
                }
            }
            return map;
        } catch (SQLException throwable){
            throwable.printStackTrace();
            return null;
        }
    }


    public void setCredits(long guildId, long userId, int credits){
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
        } catch (SQLException throwable){
            throwable.printStackTrace();
        }
    }

    public int addCredits(long guildId, long userId, int credits){
        int creds = getCredits(guildId, userId);
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("INSERT INTO Member(UserId, GuildId, LastDaily, LastWeekly, Credits) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Credits = Credits + ?;" +
                                                                   "INSERT INTO UserRecord(UserId, GuildId, Name, Value) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE Value = GREATEST(Value, ?);");
             PreparedStatement stmnt2 = conn.prepareStatement("SELECT Credits FROM Member  WHERE GuildId = ? AND UserId = ?")
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
            stm.setString(9, Record.CREDITS.getName());
            stm.executeUpdate();
            stmnt2.setLong(1, guildId);
            stmnt2.setLong(2, userId);
            try (ResultSet set = stmnt2.executeQuery()) {
                if (set.next()){
                    return set.getInt("Credits");
                }
            }
        } catch (SQLException throwable){
            throwable.printStackTrace();
        }
        return 0;
    }

    public LocalDateTime getLatestCollect(long guildId, long userId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT LastDaily FROM Member WHERE GuildId = ? AND UserId = ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()){
                    return set.getTimestamp("LastDaily").toLocalDateTime();
                }
            }
        } catch (SQLException throwable){
            throwable.printStackTrace();
        }
        return null;
    }

    public LocalDateTime getLatestWeekCollect(long guildId, long userId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT LastWeekly FROM Member WHERE GuildId = ? AND UserId = ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, userId);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()){
                    return set.getTimestamp("LastWeekly").toLocalDateTime();
                }
            }
        } catch (SQLException throwable){
            throwable.printStackTrace();
        }
        return null;
    }

    public void setLatestWeekCollect(long guild, long userId, LocalDateTime time){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Member SET LastWeekly = ? WHERE GuildId = ? AND UserId = ?")) {
            stm.setTimestamp(1, Timestamp.valueOf(time));
            stm.setLong(2, guild);
            stm.setLong(3, userId);
            stm.executeUpdate();
        } catch (SQLException throwable){
            throwable.printStackTrace();
        }
    }

    public void setLatestCollect(long guildId, long userId, LocalDateTime time){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Member SET LastDaily = ? WHERE GuildId = ? AND UserId = ?")) {
            stm.setTimestamp(1, Timestamp.valueOf(time));
            stm.setLong(2, guildId);
            stm.setLong(3, userId);
            stm.executeUpdate();
        } catch (SQLException throwable){
            throwable.printStackTrace();
        }
    }
}
