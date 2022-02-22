package commands.suggestion;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyProperties;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Suggest extends Command {

    public Suggest(){
        this.name = "suggest";
        this.aliases = new String[]{"issue", "suggestion"};
        this.arguments = "{**bot** | **plugin** | **discord**} <title> <description>";
        this.description = "suggestion.description";
        this.priveligedGuild = Utils.config.get("special.guild");
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long guildId = e.getGuild().getIdLong();
        if (args.length < 2)
            throw new MessageException(getUsage(guildId));
        String repo = null;
        if (args[0].equalsIgnoreCase("bot")){
            repo = "repo.bot";
        } else if (args[0].equalsIgnoreCase("plugin")){
            repo = "repo.plugin";
        }
        // If no description is given send error
        MyResourceBundle language = getLanguage(e);
        if (args.length == 2){
            throw new MessageException(language.getString("suggestion.error"));
        }

        EmbedBuilder eb = new EmbedBuilder();
        MyProperties config = Utils.config;

        eb.setAuthor(e.getAuthor().getName(), null, e.getAuthor().getAvatarUrl());
        eb.setTitle(args[1]);
        eb.setDescription(args[2]);
        eb.setFooter(repo != null ? language.getString("suggestion.footer", config.getProperty(repo)) : "");
        e.getGuild().getTextChannelById(Utils.config.get("special.suggestion")).sendMessageEmbeds(eb.build()).queue(m -> {
            m.addReaction(config.getProperty("emoji.green_tick")).queue();
            m.addReaction(config.getProperty("emoji.indifferent_tick")).queue();
            m.addReaction(config.getProperty("emoji.red_tick")).queue();
        });
        e.getMessage().delete().queueAfter((int) config.get("timeout"), TimeUnit.SECONDS);


    }
}
