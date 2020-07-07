package Utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;


@SuppressWarnings("unchecked")
public class DataHandler {

    final private String PATH = "src/main/resources/Data.json";
    private JSONObject jsonObject;

    public DataHandler() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(PATH)) {
            jsonObject = ((JSONObject) parser.parse(reader));
        } catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }

    public ArrayList<JSONObject> getGameRoles(){
        return ((JSONArray) jsonObject.get("gaming-roles"));

    }

    public void addRoleAssign(String type, String emoji, String name, long roleId){
        String key = String.format("%s-roles", type.toLowerCase());
        if (jsonObject.containsKey(key)){
            JSONArray roles = (JSONArray) jsonObject.get(key);
            JSONObject ra = new JSONObject();
            ra.put("emoji", emoji);
            ra.put("name", name);
            ra.put("role", roleId);
            roles.add(ra);
            save();
        }
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
