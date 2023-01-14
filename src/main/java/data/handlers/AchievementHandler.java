package data.handlers;

import companions.Achievement;

import java.sql.*;
import java.time.LocalDateTime;

public class AchievementHandler extends DataHandler{

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


}
