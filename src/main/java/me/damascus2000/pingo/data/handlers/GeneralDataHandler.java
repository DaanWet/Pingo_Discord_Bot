package me.damascus2000.pingo.data.handlers;


import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.commands.settings.Setting;
import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;
import org.sk.PrettyTable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.HashMap;


public class GeneralDataHandler extends DataHandler {

    public GeneralDataHandler(){
        super();
    }

    public void createDatabase() throws SQLException, IOException, SqlToolError{

        try (InputStream is = GeneralDataHandler.class.getClassLoader().getResourceAsStream("create.sql");
             Connection conn = DriverManager.getConnection(JDBC_URL, properties)) {
            SqlFile sqlFile = new SqlFile(new InputStreamReader(is), "init", System.out, "UTF-8", false, new File("."));
            sqlFile.setConnection(conn);
            sqlFile.execute();


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
        if (!Command.betaGuilds.contains(guildID))
            return -1;
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
                if (set.next()){
                    return set.getInt("Experience");
                }
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
                if (set.next()){
                    return set.getInt("Experience");
                }
            }
        } catch (SQLException throwables){
            throwables.printStackTrace();
        }
        return -1;
    }

    public HashMap<Long, Integer> getAllXp(long guildId){
        HashMap<Long, Integer> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT Experience, UserId FROM Member WHERE GuildId = ? AND Experience != 0 ORDER BY Experience DESC")) {
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

    public HashMap<Long, Integer> getAllXp(){
        HashMap<Long, Integer> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stmn = conn.prepareStatement("SELECT Experience, UserId FROM Member WHERE Experience != 0 ORDER BY Experience DESC")) {
            try (ResultSet set = stmn.executeQuery()) {
                while (set.next()){
                    if (!map.containsKey(set.getLong("UserId")))
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
