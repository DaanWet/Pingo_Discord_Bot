package me.damascus2000.pingo.commands;

import me.damascus2000.pingo.data.handlers.GeneralDataHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.sk.PrettyTable;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.Utils;

public class Eval extends Command {

    public Eval(){
        this.name = "eval";
        this.arguments = new String[]{"<query>"};
        this.hidden = true;
        this.description = "eval.description";
        this.priveligedGuild = Utils.config.get("special.guild");
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (args.length == 0)
            throw new MessageException("You need to provide a SQL query");


        String query = e.getMessage().getContentRaw().substring(5);
        String lquery = query.trim().toLowerCase();
        StringBuilder sb = new StringBuilder();
        if (lquery.startsWith("select") || lquery.startsWith("show")){
            PrettyTable table = new GeneralDataHandler().executeQuery(query);
            if (table == null)
                throw new MessageException(String.format("Invalid SQL Query Noob: `%s`", query));
            sb.append("```").append(table).append("```");
        } else {
            if (!e.getMember().hasPermission(Permission.ADMINISTRATOR))
                throw new MessageException("You don't have permission to update the db");

            int i = new GeneralDataHandler().executeUpdate(query);
            if (i < 0)
                throw new MessageException(String.format("Invalid SQL Query Noob: `%s`", query));

            sb.append("Updated ").append(i).append(" rows");
        }

        e.getChannel().sendMessage(sb.toString()).queue();
    }
}
