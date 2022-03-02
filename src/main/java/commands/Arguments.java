package commands;

import commands.settings.Setting;
import data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MyResourceBundle;
import utils.Utils;

import static utils.Utils.config;

public class Arguments extends Command{

    public Arguments(){
        this.name = "commands";
        this.aliases = new String[]{"args", "arguments"};
        this.description = "arguments.description";
        this.category = Category.OTHER;
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(e.getGuild().getSelfMember().getColor());
        MyResourceBundle language = getLanguage(e);
        eb.setTitle(language.getString("arguments.title"));
        eb.addField(language.getString("arguments.token"), language.getString("arguments.tokens"), false);
        eb.addField(language.getString("arguments.multiple"), language.getString("arguments.quotes"), false);
        String prefix = new SettingsDataHandler().getStringSetting(e.getGuild().getIdLong(), Setting.PREFIX).get(0);
        eb.addField(language.getString("help.commands"), language.getString("help.embed.description", prefix), false);
        eb.addField(language.getString("help.embed.links"), language.getString("help.embed.link.2", config.getProperty("github"), config.getProperty("bot.invite"), config.getProperty("server.invite")), false);
        eb.setFooter(language.getString("help.embed.footer"));
        e.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
}
