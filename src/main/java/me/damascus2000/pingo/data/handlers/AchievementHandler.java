package me.damascus2000.pingo.data.handlers;

import me.damascus2000.pingo.companions.Achievement;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;

public class AchievementHandler extends DataHandler {

    public AchievementHandler(){
        super();
    }

    public boolean hasAchieved(long guildId, long userId, Achievement achievement){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT Achieved FROM UserAchievement WHERE GuildId = ? AND UserId = ? AND Achievement LIKE ?")) {
            stmn.setLong(1, guildId);
            stmn.setLong(2, userId);
            stmn.setString(3, achievement.name());
            try (ResultSet set = stmn.executeQuery()) {
                if (set.next()){
                    return set.getBoolean("Achieved");
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return false;
    }

    public void setAchieved(long guildId, long userId, Achievement achievement){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("INSERT IGNORE INTO UserAchievement(GuildId, UserId, Achievement, Achieved, Time) VALUES(?, ?, ?, ?, ?);")) {
            stmn.setLong(1, guildId);
            stmn.setLong(2, userId);
            stmn.setString(3, achievement.name());
            stmn.setBoolean(4, true);
            stmn.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmn.executeUpdate();
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
    }

    public HashMap<Achievement, Integer> getAchievementCount(){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT COUNT(*), Achievement FROM UserAchievement WHERE Achieved = true GROUP BY Achievement")) {
            HashMap<Achievement, Integer> map = new HashMap<>();
            try (ResultSet set = stmn.executeQuery()) {
                while (set.next()){
                    map.put(Achievement.valueOf(set.getString(2)), set.getInt(1));
                }
                return map;
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return null;
    }

    public HashMap<Achievement, Integer> getAchievementCount(long guildId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT COUNT(*), Achievement FROM UserAchievement WHERE GuildId = ? AND Achieved = true GROUP BY Achievement")) {
            stmn.setLong(1, guildId);
            HashMap<Achievement, Integer> map = new HashMap<>();
            try (ResultSet set = stmn.executeQuery()) {
                while (set.next()){
                    map.put(Achievement.valueOf(set.getString(2)), set.getInt(1));
                }
                return map;
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return null;
    }

    public int getAchievementCount(long guildId, Achievement achievement){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT COUNT(*) FROM UserAchievement WHERE GuildId = ? AND Achievement LIKE ? AND Achieved = true")) {
            stmn.setLong(1, guildId);
            stmn.setString(2, achievement.name());
            try (ResultSet set = stmn.executeQuery()) {
                if (set.next()){
                    return set.getInt(1);
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return -1;
    }

    public int getAchievementCount(Achievement achievement){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT COUNT(*) FROM UserAchievement WHERE Achievement LIKE ? AND Achieved = true")) {
            stmn.setString(1, achievement.name());
            try (ResultSet set = stmn.executeQuery()) {
                if (set.next()){
                    return set.getInt(1);
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return -1;
    }


}
