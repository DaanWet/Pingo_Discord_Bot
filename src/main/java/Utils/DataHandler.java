package utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;


@SuppressWarnings("unchecked")
public class DataHandler {

    final private String PATH = "./Data.json"; //"src/main/resources/Data.json"; //
    private JSONObject jsonObject;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-dd-MM HH-mm-ss");


    public DataHandler() {
        openfile();
    }

    private void openfile() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(PATH)) {
            jsonObject = ((JSONObject) parser.parse(reader));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<JSONObject> getGameRoles() {
        openfile();
        return (JSONArray) ((JSONObject) jsonObject.get("gaming-roles")).get("roles");

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

    public long[] getMessage(String type) {
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        if (!jsonObject.containsKey(key)) return null;
        JSONObject roleobject = (JSONObject) jsonObject.get(key);
        if (!roleobject.containsKey("channel")) return null;
        return new long[]{(long) roleobject.get("channel"), (long) roleobject.get("message")};
    }


    public void addRoleAssign(String type, String emoji, String name, long roleId) {
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        if (jsonObject.containsKey(key)) {
            JSONArray roles = (JSONArray) ((JSONObject) jsonObject.get(key)).get("roles");
            JSONObject ra = new JSONObject();
            ra.put("emoji", emoji);
            ra.put("name", name);
            ra.put("role", roleId);
            roles.add(ra);
            save();
        }
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

    public int addCredits(String userid, int credits) {
        int c = getCredits(userid) + credits;
        setCredits(userid, c);
        return c;
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

    public void setLatestWeekCollect(String userid, LocalDateTime time) {
        openfile();
        if (!jsonObject.containsKey("casino") || !((JSONObject) jsonObject.get("casino")).containsKey(userid)) {
            createUser(userid);
        }
        JSONObject user = (JSONObject) ((JSONObject) jsonObject.get("casino")).get(userid);
        user.put("last_weekly_collect", dtf.format(time));
        save();
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


    public void setXP(String userid, int xp) {
        openfile();
        if (!jsonObject.containsKey("casino") || !((JSONObject) jsonObject.get("casino")).containsKey(userid)) {
            createUser(userid);
        }

        JSONObject user = (JSONObject) ((JSONObject) jsonObject.get("casino")).get(userid);
        user.put("experience", xp);
        save();
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

    public int getXP(String userid) {
        openfile();
        if (!jsonObject.containsKey("casino") || !((JSONObject) jsonObject.get("casino")).containsKey(userid)) {
            createUser(userid);
        }
        JSONObject user = (JSONObject) ((JSONObject) jsonObject.get("casino")).get(userid);
        return (int) user.getOrDefault("experience", 0);
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

    public void setRecord(String userid, String record, Comparable value) {
        setRecord(userid, record, value, null);
    }

    public void setRecord(String userid, String record, Comparable value, String link) {
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

        if (put) {
            if (link == null) {
                records.put(record, new JSONObject(Map.of("value", value)));
            } else {
                records.put(record, new JSONObject(Map.of("value", value, "link", link)));
            }
        }
        save();
    }

    public Pair<Comparable, String> getRecord(String userid, String record) {
        JSONObject ur = getUserRecords(userid);
        if (ur.containsKey(record)){
            JSONObject r = (JSONObject) ur.get(record);
            return Pair.of((Comparable) r.get("value"), (String) r.getOrDefault("link", null));
        }
        return null;
    }

    public HashMap<String, Pair<Comparable, String>> getRecords(String userid) {
        JSONObject records = getUserRecords(userid);
        HashMap<String, Pair<Comparable, String>> map = new HashMap<>();
        records.forEach((key, value) -> map.put((String) key, Pair.of((Comparable) ((JSONObject) value).get("value"), (String) ((JSONObject) value).get("link"))));
        return map;
    }

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


    void save() {
        try (FileWriter file = new FileWriter(PATH)) {
            file.write(jsonObject.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
