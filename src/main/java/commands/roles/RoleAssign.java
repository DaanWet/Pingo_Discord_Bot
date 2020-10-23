package commands.roles;

import commands.Command;
import utils.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class RoleAssign extends Command {

    private DataHandler dataHandler;

    public RoleAssign() {
        category = "Moderation";
        name = "roleassign";
        dataHandler = new DataHandler();
        this.arguments = "<category>";
        this.description = "Display Role picker";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 1) {
            ArrayList<JSONObject> roles = dataHandler.getRoles(args[0]);
            if (roles == null) {
                e.getChannel().sendMessage(String.format("%s is not an existing category", args[0])).queue();
                return;
            }
            long[] message = dataHandler.getMessage(args[0]);
            if (message != null) {
                e.getGuild().getTextChannelById(message[0]).retrieveMessageById(message[1]).queue(m -> m.delete().queue());
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(String.format("%s Roles", args[0]));
            StringBuilder sb = new StringBuilder(String.format("Get your %s roles here, react to get the role", args[0]));
            for (JSONObject s : roles) {
                sb.append("\n\n<").append(s.get("emoji")).append(">\t").append(s.get("name"));
            }
            eb.setDescription(sb.toString());
            e.getChannel().sendMessage(eb.build()).queue(m -> {
                dataHandler.setMessage(args[0], m.getTextChannel().getIdLong(), m.getIdLong());
                for (JSONObject obj : roles) {
                    m.addReaction((String) obj.get("emoji")).queue();
                }
            });
        }

        e.getMessage().delete().queue();
    }
}
