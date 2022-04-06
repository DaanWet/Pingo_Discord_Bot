package commands;

import commands.settings.Setting;
import data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyProperties;
import utils.MyResourceBundle;
import utils.Utils;

import java.time.LocalDateTime;
import java.util.Properties;

public class Poll extends Command {

    public Poll(){
        this.name = "poll";
        this.aliases = new String[]{"strawpoll"};
        this.arguments = new String[]{"<question> [<option>,...]"};
        this.description = "poll.description";
        this.example = "\"Who is the richest man on earth?\" \"Jef Bezos\" \"I am\" \"Notch\"";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        MyResourceBundle language = getLanguage(e);
        if (args.length == 0 || args.length == 2)
            throw new MessageException(language.getString("poll.error.least"));
        if (args.length == 1){
            MyProperties config = Utils.config;
            e.getChannel().sendMessage(String.format("**%s#%s: %s**", e.getAuthor().getName(), e.getAuthor().getDiscriminator(), Utils.upperCaseFirst(args[0]))).queue(m -> {
                m.addReaction(config.getProperty("emoji.green_tick")).queue();
                m.addReaction(config.getProperty("emoji.indifferent_tick")).queue();
                m.addReaction(config.getProperty("emoji.red_tick")).queue();
            });
        } else {
            if (args.length > 21)
                throw new MessageException(language.getString("poll.error.max"));
            StringBuilder sb = new StringBuilder();
            sb.append("**").append(e.getAuthor().getName()).append("#").append(e.getAuthor().getDiscriminator()).append(": ").append(Utils.upperCaseFirst(args[0])).append("**");
            for (int i = 1; i < args.length; i++){
                sb.append("\n").append(Utils.regionalEmoji(i - 1)).append(" ").append(Utils.upperCaseFirst(args[i]));
            }
            e.getChannel().sendMessage(sb.toString()).queue(m -> {
                for (int i = 0; i < args.length - 1; i++){
                    m.addReaction(Utils.regionalUnicode(i)).queue();
                }
            });

        }
        e.getMessage().delete().queue();
        new SettingsDataHandler().setCooldown(e.getGuild().getIdLong(), e.getAuthor().getIdLong(), Setting.POLL, LocalDateTime.now());
    }
}
