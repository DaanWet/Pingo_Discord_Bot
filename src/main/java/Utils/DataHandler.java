package Utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;


@SuppressWarnings("unchecked")
public class DataHandler {

    final private String PATH = "./Data.json"; // "src/main/resources/Data.json";
    private JSONObject jsonObject;

    public DataHandler() {
        openfile();
    }

    private void openfile(){
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(PATH)) {
            jsonObject = ((JSONObject) parser.parse(reader));
        } catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }


    public ArrayList<JSONObject> getGameRoles(){
        openfile();
        return (JSONArray) ((JSONObject) jsonObject.get("gaming-roles")).get("roles");

    }

    public boolean setMessage(String type, long channelId, long messageId){
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        if (!jsonObject.containsKey(key)) return false;
        JSONObject object = (JSONObject) jsonObject.get(key);
        object.put("channel", channelId);
        object.put("message", messageId);
        save();
        return true;
    }

    public long[] getMessage(String type){
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        if (!jsonObject.containsKey(key)) return null;
        JSONObject roleobject = (JSONObject) jsonObject.get(key);
        if (!roleobject.containsKey("channel")) return null;
        return new long[]{(long)roleobject.get("channel"), (long)roleobject.get("message")};
    }


    public void addRoleAssign(String type, String emoji, String name, long roleId){
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        if (jsonObject.containsKey(key)){
            JSONArray roles = (JSONArray) ((JSONObject) jsonObject.get(key)).get("roles");
            JSONObject ra = new JSONObject();
            ra.put("emoji", emoji);
            ra.put("name", name);
            ra.put("role", roleId);
            roles.add(ra);
            save();
        }
    }
    public boolean removeRoleAssign(String type, String emoji){
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        boolean found = false;
        if (jsonObject.containsKey(key)){
            JSONArray roles = (JSONArray) ((JSONObject) jsonObject.get(key)).get("roles");
            int i = 0;
            while (!found && i < roles.size()){
                if (((JSONObject) roles.get(i)).get("emoji").equals(emoji)){
                    found = true;
                }   else {
                    i++;
                }
            }
            if (found){
                roles.remove(i);
            }
        }
        return found;
    }
    public boolean removeRoleAssign(String type, long roleid){
        openfile();
        String key = String.format("%s-roles", type.toLowerCase());
        boolean found = false;
        if (jsonObject.containsKey(key)){
            JSONArray roles = (JSONArray) ((JSONObject) jsonObject.get(key)).get("roles");
            int i = 0;
            while (!found && i < roles.size()){
                if (((long) ((JSONObject) roles.get(i)).get("role")) == (roleid)){
                    found = true;
                }   else {
                    i++;
                }
            }
            if (found){
                roles.remove(i);
            }
        }
        return found;
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
