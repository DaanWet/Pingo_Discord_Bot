package commands.suggestion;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Suggest extends Command {

    public Suggest(){
        this.name = "suggest";
        this.aliases = new String[]{"issue", "suggestion"};
        this.arguments = "{**bot** | **plugin** | **discord**} <title> **-d** <description>";
        this.description = "suggestion.description";
        this.priveligedGuild = 203572340280262657L;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long guildId = e.getGuild().getIdLong();
        if (args.length < 4)
            throw new MessageException(getUsage(guildId));
        String repo = null;
        if (args[0].equalsIgnoreCase("bot")){
            repo = "repo.bot";
        } else if (args[0].equalsIgnoreCase("plugin")){
            repo = "repo.plugin";
        }
        boolean t = true;
        StringBuilder title = new StringBuilder();
        StringBuilder descript = new StringBuilder();
        for (int i = 1; i < args.length; i++){
            if (t){
                if (args[i].equalsIgnoreCase("-d")){
                    t = false;
                } else {
                    title.append(args[i]).append(" ");
                }
            } else {
                descript.append(args[i]).append(" ");
            }
        }
        // If no description is given send error
        MyResourceBundle language = getLanguage(e);
        if (t)
            throw new MessageException(language.getString("suggestion.error"));

        EmbedBuilder eb = new EmbedBuilder();
        Properties config = Utils.config;

        eb.setAuthor(e.getAuthor().getName(), null, e.getAuthor().getAvatarUrl());
        eb.setTitle(title.toString());
        eb.setDescription(descript.toString());
        eb.setFooter(repo != null ? language.getString("suggestion.footer", config.getProperty(repo)) : "");
        e.getGuild().getTextChannelById(747228850353733739L).sendMessageEmbeds(eb.build()).queue(m -> {
            m.addReaction(config.getProperty("emoji.green_tick")).queue();
            m.addReaction(config.getProperty("emoji.indifferent_tick")).queue();
            m.addReaction(config.getProperty("emoji.red_tick")).queue();
        });
        e.getMessage().delete().queueAfter((int) config.get("timeout"), TimeUnit.SECONDS);


    }
}
