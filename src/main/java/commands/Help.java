package commands;

import commands.settings.Setting;
import companions.GameCompanion;
import data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Category;
import utils.*;

import java.io.File;
import java.util.*;

import static listeners.CommandHandler.pathname;

public class Help extends Command {

    private final GameCompanion gameCompanion;
    private Collection<Command> commands;

    public Help(GameCompanion gameCompanion){
        this.name = "help";
        this.aliases = new String[]{"commands", "command", "h"};
        this.description = "help.description";
        this.gameCompanion = gameCompanion;
    }

    public void setCommands(Map<String, Command> comm){
        commands = comm.values();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(e.getGuild().getSelfMember().getColor());
        long guildId = e.getGuild().getIdLong();
        String prefix = new SettingsDataHandler().getStringSetting(guildId, Setting.PREFIX).get(0);
        MyResourceBundle language = Utils.getLanguage(guildId);
        if (args.length == 1){
            if (args[0].equalsIgnoreCase("moderation")){
                if (!e.getMember().hasPermission(Permission.ADMINISTRATOR))
                    throw new EmbedException(language.getString("help.error.perm"));

                eb.setTitle(language.getString("help.moderation"));
                fillCommands(eb, true, guildId, prefix, language);

            } else if (args[0].equalsIgnoreCase("pictures") && guildId == Utils.config.get("special.guild")){
                File dir = new File(pathname);
                eb.setTitle(language.getString("help.pictures"));
                StringBuilder sb = new StringBuilder();
                for (File directory : dir.listFiles()){
                    sb.append("\n").append(language.getString("help.pictures.desc", prefix, directory.getName(), directory.getName()));
                }
                eb.setDescription(sb.toString());
            } else {
                boolean found = false;
                Iterator<Command> iterator = commands.iterator();
                while (!found && iterator.hasNext()){
                    Command command = iterator.next();
                    if (command.isCommandFor(args[0])){
                        eb.setTitle(language.getString("help.command", StringUtils.capitalize(command.getName())));
                        eb.addField(language.getString("help.usage"), String.format("%s%s %s", prefix, command.getName(), command.getArguments()), false);
                        eb.setDescription(command.getDescription(language));
                        if (command.getAliases().length != 0){
                            eb.addField(language.getString("help.aliases"), String.join(", ", command.getAliases()), false);
                        }
                        eb.setFooter(language.getString("help.embed.cmd.footer"));
                        found = true;
                    }
                }
                if (!found){
                    throw new MessageException(language.getString("help.error.command", args[0]));
                }
            }
        } else if (gameCompanion.isUnoChannel(guildId, e.getChannel().getIdLong())){
            eb.setTitle(language.getString("help.uno"));
            StringBuilder sb = new StringBuilder();
            for (Command c : commands){
                if (c.getCategory() != null && c.getCategory() == Category.UNO){
                    sb.append(String.format("\n%s%s %s: *%s*", prefix, c.getName(), c.getArguments(), language.getString(c.getDescription() == null ? "help.error" : c.getDescription().trim())));
                }
            }
            eb.setDescription(sb.toString());
        } else {
            eb.setTitle(language.getString("help.commands"));
            fillCommands(eb, false, guildId, prefix, language);
        }
        e.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    public void fillCommands(EmbedBuilder eb, boolean moderation, long guildId, String prefix, MyResourceBundle language){
        eb.setDescription(language.getString("help.embed.description", prefix));
        Map<Category, StringBuilder> sbs = new HashMap<>();
        MyProperties config = Utils.config;
        for (Command c : commands){
            Category cat = c.getCategory();
            if ((cat == null || ((!c.isHidden() && cat == Category.MODERATION == moderation))) && (c.getPriveligedGuild() == -1 || c.getPriveligedGuild() == guildId)){
                if (!sbs.containsKey(cat)){
                    sbs.put(cat, new StringBuilder());
                }
                StringBuilder sb = sbs.get(cat);
                sb.append(String.format("• %s%s\n", c.getName(), c.getAliases().length > 0 ? " / " + c.getAliases()[0] : ""));
            }
        }
        if (guildId == (long) config.get("special.guild") && !moderation)
            sbs.get(Category.PICTURES).append("\n").append(language.getString("help.pictures.list", prefix + "help"));
        Arrays.stream(Category.values()).forEachOrdered(c -> {
            if (sbs.containsKey(c))
                eb.addField(c.getDisplay(), sbs.get(c).toString().trim(), true);
        });
        eb.addField(language.getString("help.embed.links"), language.getString("help.embed.link.2", config.getProperty("github"), config.getProperty("bot.invite"), config.getProperty("server.invite")), false);
        eb.setFooter(language.getString("help.embed.footer"));
    }
}
