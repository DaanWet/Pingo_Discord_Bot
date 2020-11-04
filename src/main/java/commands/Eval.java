package commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.sk.PrettyTable;
import utils.DataHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;

public class Eval extends Command{

    public Eval(){
        this.name = "eval";
        this.arguments = "<query>";
        this.hidden = true;
        this.description = "executes a SQL query";
        this.priveligedGuild = 203572340280262657L;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        if (args.length >= 1){
            String query = String.join(" ", args);
            String lquery = query.trim().toLowerCase();
            StringBuilder sb = new StringBuilder();
            if (query.trim().toLowerCase().startsWith("select")){
                PrettyTable table = new DataHandler().executeQuery(query);
                if (table != null){
                    sb.append("```").append(table).append("```");
                } else {
                    sb.append("Invalid SQL Query Noob: `").append(query).append("`");
                }
            } else if (lquery.startsWith("insert") || lquery.startsWith("update") || lquery.startsWith("drop") || lquery.startsWith("create") || lquery.startsWith("delete")){
                if (e.getMember().hasPermission(Permission.ADMINISTRATOR)){
                    int i = new DataHandler().executeUpdate(query);
                    if (i < 0){
                        sb.append("Invalid SQL Query Noob: `").append(query).append("`");

                    } else {
                        sb.append("Updated ").append(i).append(" rows");
                    }

                } else {
                    sb.append("You don't have permission to update the db");
                }
            }

            e.getChannel().sendMessage(sb.toString()).queue();
        }
    }
}
