package Commands;

import Utils.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class RoleAssign extends Command{

    public RoleAssign() {
        category = "Moderation";
        name = "roleassign";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Gaming Roles");
        StringBuilder sb = new StringBuilder("Get your gaming roles here, react to get the role");
        ArrayList<JSONObject> gameroles = new DataHandler().getGameRoles();
        for (JSONObject s : gameroles){
            sb.append("\n\n<").append(s.get("emoji")).append(">\t").append(s.get("name"));
        }
        eb.setDescription(sb.toString());
        e.getChannel().sendMessage(eb.build()).queue(m -> {
            for (JSONObject obj : gameroles){
                m.addReaction((String) obj.get("emoji")).queue();
            }
        });
        e.getMessage().delete().queue();
    }

    @Override
    public String getDescription() {
        return "Game role picker";
    }
}
