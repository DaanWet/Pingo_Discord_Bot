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
        this.description = "Shows a help overview";
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
        if (args.length == 1){
            if (args[0].equalsIgnoreCase("moderation")){
                if (!e.getMember().hasPermission(Permission.ADMINISTRATOR))
                    throw new EmbedException("❌ You don't have permission to run this command");

                eb.setTitle("Pingo Moderation commands");
                fillCommands(eb, true, guildId, prefix);

            } else if (args[0].equalsIgnoreCase("pictures") && guildId == 203572340280262657L){
                File dir = new File(pathname);
                eb.setTitle("Pictures Commands");
                StringBuilder sb = new StringBuilder();
                for (File directory : dir.listFiles()){
                    sb.append(String.format("\n%s%s: *Shows a random picture of %s*", prefix, directory.getName(), directory.getName()));
                }
                eb.setDescription(sb.toString());
            } else {
                boolean found = false;
                Iterator<Command> iterator = commands.iterator();
                while (!found && iterator.hasNext()){
                    Command command = iterator.next();
                    if (command.isCommandFor(args[0])){
                        eb.setTitle(String.format("%s Command Help", StringUtils.capitalize(command.getName())));
                        eb.setDescription(command.getDescription());
                        eb.addField("Usage", String.format("%s%s %s", prefix, command.getName(), command.getArguments()), false);
                        if (command.getAliases().length != 0){
                            eb.addField("Aliases", String.join(", ", command.getAliases()), false);
                        }
                        found = true;
                    }
                }
                if (!found){
                    throw new MessageException(String.format("No such command named %s", args[0]));
                }
            }
        } else if (gameHandler.isUnoChannel(guildId, e.getChannel().getIdLong())){
            eb.setTitle("Uno commands");
            StringBuilder sb = new StringBuilder();
            for (Command c : commands){
                if (c.getCategory() != null && c.getCategory().equalsIgnoreCase("Uno")){
                    sb.append(String.format("\n%s%s %s: *%s*", prefix, c.getName(), c.getArguments(), c.getDescription() == null ? "No help availabe" : c.getDescription().trim()));
                }
            }
            eb.setDescription(sb.toString());
        } else {
            eb.setTitle("Pingo commands");
            fillCommands(eb, false, guildId, prefix);
        }
        e.getChannel().sendMessage(eb.build()).queue();
    }

    public void fillCommands(EmbedBuilder eb, boolean moderation, long guildId, String prefix){
        Map<String, StringBuilder> sbs = new HashMap<>();
        for (Command c : commands){
            if ((c.getCategory() == null || ((!c.isHidden() && c.getCategory().equalsIgnoreCase("moderation") == moderation))) && (c.getPriveligedGuild() == -1 || c.getPriveligedGuild() == guildId)){
                String cat = c.getCategory();
                if (!sbs.containsKey(cat)){
                    sbs.put(cat, new StringBuilder());
                }
                StringBuilder sb = sbs.get(cat);
                sb.append(String.format("\n%S%s: *%s*", prefix, c.getName(), c.getDescription() == null ? "No help availabe" : c.getDescription().trim()));

            }
        }
        if (guildId == 203572340280262657L && !moderation) sbs.get("Pictures").append("\nFor a full list of all pictures command run !help pictures");
        sbs.keySet().forEach(s -> eb.addField(s, sbs.get(s).toString().trim(), false));
    }
}
