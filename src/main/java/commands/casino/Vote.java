package commands.casino;

import commands.Command;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.discordbots.api.client.DiscordBotListAPI;

public class Vote extends Command {



    private DiscordBotListAPI api;

    public Vote(DiscordBotListAPI botListAPI){
        this.name = "vote";
        this.description = "";
        this.api = botListAPI;
    }



    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        api.hasVoted(e.getAuthor().getId()).whenComplete((hasVoted, err)  -> {
           if (hasVoted){
               e.getChannel().sendMessage("Voted").queue();
           }
        });
    }
}
