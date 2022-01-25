package data.handlers;

import commands.roles.RoleCommand;
import data.handlers.DataHandler;
import data.models.RoleAssignData;
import data.models.RoleAssignRole;

import java.sql.*;
import java.util.ArrayList;

public class RRDataHandler extends DataHandler {




    public RRDataHandler(){
        super();
    }


    public ArrayList<String> getRoleCategories(long guildId) throws SQLException{
        ArrayList<String> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmn = conn.prepareStatement("SELECT Name FROM RoleAssign WHERE GuildId = ?")
        ) {
            stmn.setLong(1, guildId);
            try (ResultSet set = stmn.executeQuery()) {
                while (set.next()){
                    list.add(set.getString("Name"));
                }
            }
        }
        return list;
    }


    public ArrayList<RoleAssignRole> getRoles(long guildID, String type){
        ArrayList<RoleAssignRole> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmn = conn.prepareStatement("SELECT Name, Emoji, RoleId FROM Role WHERE GuildId = ? AND Type LIKE ?")) {
            stmn.setLong(1, guildID);
            stmn.setString(2, type);
            try (ResultSet set = stmn.executeQuery()) {
                while (set.next()){
                    list.add(new RoleAssignRole(set.getString("Emoji"), set.getString("Name"), set.getLong("RoleId")));
                }
            }
        } catch (SQLException exc){
            throw new RuntimeException(exc);
        }
        return list;
    }

    public boolean setMessage(long guildId, String type, long channelId, long messageId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmnt = conn.prepareStatement("UPDATE RoleAssign SET ChannelId = ?, MessageId = ? WHERE GuildId = ? AND Name LIKE ?")) {
            stmnt.setLong(3, guildId);
            stmnt.setLong(1, channelId);
            stmnt.setLong(2, messageId);
            stmnt.setString(4, type);
            int i = stmnt.executeUpdate();
            return i != 0;
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean setCompacting(long guildId, String type, RoleCommand.Compacting compacting, String sorting){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmnt = conn.prepareStatement("UPDATE RoleAssign SET Compacting = ?, Sorting = ? WHERE GuildId = ? AND Name LIKE ?")) {
            stmnt.setLong(3, guildId);
            stmnt.setString(1, compacting.toString());
            stmnt.setString(2, sorting);
            stmnt.setString(4, type);
            int i = stmnt.executeUpdate();
            return i != 0;
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return false;
    }

    public RoleAssignData getRoleAssignData(long guildId, String type){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stmn = conn.prepareStatement("SELECT ChannelId, MessageId, Compacting, Sorting, Title FROM RoleAssign WHERE GuildId = ? AND Name LIKE ?")) {
            stmn.setLong(1, guildId);
            stmn.setString(2, type);
            try (ResultSet set = stmn.executeQuery()) {
                if (set.next()){
                    boolean valid = RoleCommand.Sorting.isSort(set.getString(4));
                    RoleAssignData data = new RoleAssignData(set.getLong(1), set.getLong(2), RoleCommand.Compacting.valueOf(set.getString(3)), valid ? RoleCommand.Sorting.valueOf(set.getString(4)) : RoleCommand.Sorting.CUSTOM, set.getString(5));
                    if (!valid) data.setCustomS(set.getString(4));
                    return data;
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return new RoleAssignData(null, null, RoleCommand.Compacting.NORMAL, RoleCommand.Sorting.NONE, null);
    }

    public String getCategory(long guildId, long channelId, long messageId){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER_ID, PASSWD);
             PreparedStatement stm = conn.prepareStatement("SELECT Name FROM RoleAssign WHERE GuildId = ? AND ChannelId = ? AND MessageId = ?")) {
            stm.setLong(1, guildId);
            stm.setLong(2, channelId);
            stm.setLong(3, messageId);
            try (ResultSet set = stm.executeQuery()) {
                if (set.next()){
                    return set.getString(1);
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return null;
    }

    public boolean addRoleAssign(long guildId, String type, String emoji, String name, long roleId){
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
        } catch (Exception throwables){
            throwables.printStackTrace();
            return false;
        }
    }

    public boolean removeRoleAssign(long guildId, String type, String emoji){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("DELETE FROM Role WHERE GuildId = ? AND Type LIKE ? AND Emoji LIKE ?")) {
            stm.setLong(1, guildId);
            stm.setString(2, type);
            stm.setString(3, emoji);
            int i = stm.executeUpdate();
            return i != 0;
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean editRoleName(long guildId, String type, String emoji, String name){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE Role SET Name = ? WHERE GuildId = ? AND Type LIKE ? AND Emoji LIKE ?")) {
            stm.setLong(2, guildId);
            stm.setString(1, name);
            stm.setString(3, type);
            stm.setString(4, emoji);
            int a = stm.executeUpdate();
            return a == 1;
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean setTitle(long guildId, String type, String title){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("UPDATE RoleAssign SET Title = ? WHERE GuildId = ? AND Name LIKE ?")) {
            stm.setString(1, title);
            stm.setLong(2, guildId);
            stm.setString(3, type);
            int a = stm.executeUpdate();
            return a == 1;
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return false;
    }
}
