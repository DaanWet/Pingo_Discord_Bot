package commands;

import commands.settings.Setting;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;
import utils.Utils;

import java.time.LocalDateTime;

public class Poll extends Command{

    public Poll(){
        this.name = "poll";
        this.aliases = new String[]{"strawpoll"};
        this.arguments = "\"<question>\" [\"<option>\"]";
        this.description = "Creates a poll";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        if (args.length == 1){
            e.getChannel().sendMessage(String.format("**%s**", Utils.upperCaseFirst(args[0]))).queue(m -> {
                m.addReaction(":greentick:804432208483844146").queue();
                m.addReaction(":indifftick:804432286455169044").queue();
                m.addReaction(":redtick:804432244469923890").queue();
            });
        } else if (args.length >= 3){
            if (args.length <= 21){
                StringBuilder sb = new StringBuilder();
                sb.append("**").append(Utils.upperCaseFirst(args[0])).append("**");
                for (int i = 1; i < args.length; i++){
                    sb.append("\n").append(Utils.regionalEmoji(i - 1)).append(" ").append(Utils.upperCaseFirst(args[i]));
                }
                e.getChannel().sendMessage(sb.toString()).queue(m -> {
                    for (int i = 0; i < args.length - 1; i++){
                        System.out.println(Utils.regionalUnicode(i));
                        m.addReaction(Utils.regionalUnicode(i)).queue();
                    }
                });
            } else {
                e.getChannel().sendMessage("More than 20 options is not allowed").queue();
                return;
            }
        } else {
            e.getChannel().sendMessage("You have to give a question and/or at least 2 options").queue();
            return;
        }
        e.getMessage().delete().queue();
        new DataHandler().setCooldown(e.getGuild().getIdLong(), e.getAuthor().getIdLong(), Setting.POLL, LocalDateTime.now());
    }
}
