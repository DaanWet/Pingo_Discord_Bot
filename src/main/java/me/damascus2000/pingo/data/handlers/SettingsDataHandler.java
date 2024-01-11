package me.damascus2000.pingo.data.handlers;

import com.mysql.cj.exceptions.WrongArgumentException;
import me.damascus2000.pingo.commands.settings.Setting;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import me.damascus2000.pingo.utils.Utils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SettingsDataHandler extends DataHandler {

    public SettingsDataHandler(){
        super();
    }

    private int extra(boolean multiple){
        return !multiple ? 3 : 0;
    }

    public List<Pair<String, String>> getSetting(long guildId, Setting setting, Setting.SubSetting subSetting){
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
            while (rs.next()){
                list.add(Pair.of(rs.getString(1), rs.getString(2)));
            }
        } catch (SQLException exception){
            throw new RuntimeException(exception);
        }
        return list;
    }

    public List<Integer> getIntSetting(long guildId, Setting setting, Setting.SubSetting subSetting){
        List<Pair<String, String>> resultSet = getSetting(guildId, setting, subSetting);

        if (resultSet.size() != 0){
            return resultSet.stream().map(pair -> Utils.getInt(pair.getLeft())).collect(Collectors.toList());
        }
        return List.of((int) (subSetting == null ? setting.getDefaultValue() : subSetting.getDefaultValue()));
    }

    public List<Integer> getIntSetting(long guildId, Setting setting){
        return getIntSetting(guildId, setting, null);
    }

    // Bool setting will never be a list?
    public boolean getBoolSetting(long guildId, Setting setting, Setting.SubSetting subSetting){
        List<Pair<String, String>> resultSet = getSetting(guildId, setting, subSetting);
        if (resultSet.size() != 0){
            return "1".equalsIgnoreCase(resultSet.get(0).getLeft());
        }
        return (boolean) (subSetting == null ? setting.getDefaultValue() : subSetting.getDefaultValue());
    }

    public boolean getBoolSetting(long guildId, Setting setting){
        return getBoolSetting(guildId, setting, null);
    }

    public List<String> getStringSetting(long guildId, Setting setting, Setting.SubSetting subSetting){
        List<Pair<String, String>> resultSet = getSetting(guildId, setting, subSetting);
        if (resultSet.size() != 0){
            return resultSet.stream().map(Pair::getLeft).collect(Collectors.toList());
        }
        return List.of((String) (subSetting == null ? setting.getDefaultValue() : subSetting.getDefaultValue()));
    }

    public List<String> getStringSetting(long guildId, Setting setting){
        return getStringSetting(guildId, setting, null);
    }

    public List<Pair<Long, Setting.LongType>> getLongSetting(long guildId, Setting setting, Setting.SubSetting subSetting){
        List<Pair<String, String>> resultSet = getSetting(guildId, setting, subSetting);
        if (resultSet.size() != 0){
            return resultSet.stream().map(pair -> Pair.of(Long.parseLong(pair.getLeft()), Setting.LongType.valueOf(pair.getRight()))).collect(Collectors.toList());
        }
        return null;
    }

    public List<Pair<Long, Setting.LongType>> getLongSetting(long guildId, Setting setting){
        return getLongSetting(guildId, setting, null);
    }

    public boolean getListEnabled(long guildId, Setting setting, Setting.SubSetting subSetting) throws WrongArgumentException{
        if (!(subSetting == null ? setting.isMultiple() : subSetting.isMultiple())){
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
            if (rs.next()){
                return rs.getBoolean(1);
            }
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return false;
    }

    private PreparedStatement prepareSetSetting(Connection conn, Setting setting, Setting.SubSetting subSetting, long guildId, Setting.LongType longType) throws SQLException{
        boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
        PreparedStatement stm = conn.prepareCall("SELECT @id := ID FROM Setting WHERE Name LIKE ? AND Type LIKE ? AND ValueType LIKE ?;" +
                                                         (multiple ? "" : "UPDATE GuildSetting SET Value = ?, Type = ? WHERE  GuildId = ? AND ID = @id;") +
                                                         "INSERT IGNORE INTO GuildSetting(GuildId, ID, Value, Type) VALUES(?, @id, ?, ?);");

        String longTypeName = longType == null ? null : longType.name();
        stm.setString(1, setting.getName() + (subSetting != null ? "_" + subSetting : ""));
        stm.setString(2, setting.getType());
        stm.setString(3, subSetting == null ? setting.getValueType().getName() : subSetting.getValueType().getName());
        if (!multiple){
            stm.setString(5, longTypeName);
            stm.setLong(6, guildId);
        }
        stm.setLong(4 + extra(multiple), guildId);
        stm.setString(6 + extra(multiple), longTypeName);
        return stm;
    }

    private PreparedStatement prepareRemoveSetting(Connection conn, Setting setting, Setting.SubSetting subSetting, long guildId, boolean clear) throws Exception{
        boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
        if (!multiple){
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

    public void setIntSetting(long guildId, Setting setting, Setting.SubSetting subSetting, int value, Boolean clear) throws Exception{
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = clear == null ? prepareSetSetting(conn, setting, subSetting, guildId, null)
                     : prepareRemoveSetting(conn, setting, subSetting, guildId, clear)) {
            boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
            if (!multiple)
                stm.setInt(4, value);
            if (clear == null || !clear)
                stm.setInt(5 + extra(multiple), value);
            stm.executeQuery();
        } catch (SQLException exception){
            exception.printStackTrace();
        }
    }

    public void setIntSetting(long guildId, Setting setting, int value, Boolean clear) throws Exception{
        setIntSetting(guildId, setting, null, value, clear);
    }

    public void setBoolSetting(long guildId, Setting setting, Setting.SubSetting subSetting, boolean value, Boolean clear) throws Exception{
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = clear == null ? prepareSetSetting(conn, setting, subSetting, guildId, null)
                     : prepareRemoveSetting(conn, setting, subSetting, guildId, clear)) {
            boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
            if (!multiple)
                stm.setBoolean(4, value);
            if (clear == null || !clear)
                stm.setBoolean(5 + extra(multiple), value);
            stm.executeQuery();
        } catch (SQLException exception){
            exception.printStackTrace();
        }
    }

    public void setBoolSetting(long guildId, Setting setting, boolean value, Boolean clear) throws Exception{
        setBoolSetting(guildId, setting, null, value, clear);
    }

    public void setStringSetting(long guildId, Setting setting, Setting.SubSetting subSetting, String value, Boolean clear) throws Exception{
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = clear == null ? prepareSetSetting(conn, setting, subSetting, guildId, null)
                     : prepareRemoveSetting(conn, setting, subSetting, guildId, clear)) {
            boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
            if (!multiple)
                stm.setString(4, value);
            if (clear == null || !clear)
                stm.setString(5 + extra(multiple), value);
            stm.executeQuery();
        } catch (SQLException exception){
            exception.printStackTrace();
        }
    }

    public void setStringSetting(long guildId, Setting setting, String value, Boolean clear) throws Exception{
        setStringSetting(guildId, setting, null, value, clear);
    }

    public void setLongSetting(long guildId, Setting setting, Setting.SubSetting subSetting, long value, Setting.LongType longType, Boolean clear) throws Exception{
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = clear == null ? prepareSetSetting(conn, setting, subSetting, guildId, longType)
                     : prepareRemoveSetting(conn, setting, subSetting, guildId, clear)) {
            boolean multiple = subSetting == null ? setting.isMultiple() : subSetting.isMultiple();
            if (!multiple)
                stm.setLong(4, value);
            if (clear == null || !clear)
                stm.setLong(5 + extra(multiple), value);
            stm.executeQuery();
        } catch (SQLException exception){
            exception.printStackTrace();
        }
    }

    public void setLongSetting(long guildId, Setting setting, long value, Setting.LongType longType, Boolean clear) throws Exception{
        setLongSetting(guildId, setting, null, value, longType, clear);
    }

    public void setListEnabled(long guildId, Setting setting, Setting.SubSetting subSetting, boolean enabled){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareCall("SELECT @id := ID FROM Setting WHERE Name LIKE ? AND Type LIKE ? AND ValueType LIKE ?;" +
                                                              "UPDATE GuildSetting SET Value = ? WHERE GuildId = ? AND ID = @id;" +
                                                              "INSERT IGNORE INTO GuildSetting(GuildId, ID, Value) VALUES(?, @id, ?);")
        ) {
            stm.setString(1, setting.getName() + (subSetting != null ? "_" + subSetting : "") + "_on");
            stm.setString(2, setting.getType());
            stm.setString(3, Setting.ValueType.BOOLEAN.getName());
            stm.setBoolean(4, enabled);
            stm.setLong(5, guildId);
            stm.setLong(6, guildId);
            stm.setBoolean(7, enabled);
            stm.executeQuery();
        } catch (SQLException exc){
            exc.printStackTrace();
        }
    }

    public void setListEnabled(long guildId, Setting setting, boolean enabled){
        setListEnabled(guildId, setting, null, enabled);
    }

    public void setCooldown(long guildId, long userId, Setting setting, LocalDateTime time){
        if (getIntSetting(guildId, setting, Setting.SubSetting.COOLDOWN).get(0) != 0){
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
            } catch (SQLException exc){
                exc.printStackTrace();
            }
        }
    }

    public LocalDateTime getCooldown(long guildId, long userId, Setting setting){
        try (Connection conn = DriverManager.getConnection(JDBC_URL, properties);
             PreparedStatement stm = conn.prepareStatement("SELECT @id := ID FROM Setting WHERE Name LIKE ? AND Type LIKE ? AND ValueType LIKE ?;" +
                                                                   "SELECT Time FROM Cooldown WHERE Setting = @id AND GuildId = ? AND UserId = ?")) {
            stm.setString(1, setting.getName());
            stm.setString(2, setting.getType());
            stm.setString(3, setting.getValueType().getName());
            stm.setLong(4, guildId);
            stm.setLong(5, userId);
            ResultSet rs = stm.executeQuery();
            if (rs.next()){
                return rs.getTimestamp(1).toLocalDateTime();
            }
        } catch (SQLException exc){
            exc.printStackTrace();
        }
        return null;
    }
}
