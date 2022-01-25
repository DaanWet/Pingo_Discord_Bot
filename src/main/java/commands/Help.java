package commands;

import commands.settings.Setting;
import companions.GameHandler;
import data.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import utils.EmbedException;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static listeners.CommandHandler.pathname;

public class Help extends Command {

    private final GameHandler gameHandler;
    private Collection<Command> commands;

    public Help(GameHandler gameHandler){
        this.name = "help";
        this.aliases = new String[]{"commands", "command", "h"};
        this.description = "help.description";
        this.gameHandler = gameHandler;
    }

    public void setCommands(Map<String, Command> comm){
        commands = comm.values();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(e.getGuild().getSelfMember().getColor());
        long guildId = e.getGuild().getIdLong();
        String prefix = new DataHandler().getStringSetting(guildId, Setting.PREFIX).get(0);
        MyResourceBundle language = Utils.getLanguage(guildId);
        if (args.length == 1){
            if (args[0].equalsIgnoreCase("moderation")){
                if (!e.getMember().hasPermission(Permission.ADMINISTRATOR))
                    throw new EmbedException(language.getString("help.error.perm"));

                eb.setTitle(language.getString("help.moderation"));
                fillCommands(eb, true, guildId, prefix, language);

            } else if (args[0].equalsIgnoreCase("pictures") && guildId == 203572340280262657L){
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
                        eb.setDescription(command.getDescription());
                        eb.addField(language.getString("help.usage"), String.format("%s%s %s", prefix, command.getName(), command.getArguments()), false);
                        if (command.getAliases().length != 0){
                            eb.addField(language.getString("help.aliases"), String.join(", ", command.getAliases()), false);
                        }
                        found = true;
                    }
                }
                if (!found){
                    throw new MessageException(language.getString("help.error.command", args[0]));
                }
            }
        } else if (gameHandler.isUnoChannel(guildId, e.getChannel().getIdLong())){
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
        Map<Category, StringBuilder> sbs = new HashMap<>();
        for (Command c : commands){
            Category cat = c.getCategory();
            if ((cat == null || ((!c.isHidden() && cat == Category.MODERATION == moderation))) && (c.getPriveligedGuild() == -1 || c.getPriveligedGuild() == guildId)){
                if (!sbs.containsKey(cat)){
                    sbs.put(cat, new StringBuilder());
                }
                StringBuilder sb = sbs.get(cat);
                sb.append(String.format("\n%s%s: *%s*", prefix, c.getName(), language.getString(c.getDescription() == null ? "help.error" : c.getDescription().trim())));

            }
        }
        if (guildId == 203572340280262657L && !moderation)
            sbs.get(Category.PICTURES).append("\n").append(language.getString("help.pictures.list", prefix + "help"));
        sbs.keySet().forEach(s -> eb.addField(s.getDisplay(), sbs.get(s).toString().trim(), false));
    }
}
