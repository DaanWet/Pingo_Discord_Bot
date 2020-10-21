package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static commands.CommandHandler.pathname;

public class Help extends Command{

    private Collection<Command> commands;

    public Help(){
        this.name = "help";
        this.aliases = new String[]{"commands", "command", "h"};
        this.description = "Shows a help overview";
    }

    public void setCommands(Map<String, Command> comm){
        commands = comm.values();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(e.getGuild().getSelfMember().getColor());
        if (args.length == 1){
            if (args[0].equalsIgnoreCase("moderation")){
                if (e.getMember().hasPermission(Permission.ADMINISTRATOR)){
                    eb.setTitle("Pingo Moderation commands");
                    fillCommands(eb, true);
                } else {
                    eb.setTitle("❌ You don't have permission to run this command");
                    eb.setColor(Color.RED);
                }
            } else if (args[0].equalsIgnoreCase("pictures")){
                File dir = new File(pathname);
                eb.setTitle("Pictures Commands");
                StringBuilder sb = new StringBuilder();
                for (File directory : dir.listFiles()) {
                    sb.append(String.format("\n!%s: *Shows a random picture of %s*", directory.getName(), directory.getName()));
                }
                eb.setDescription(sb.toString());
            } else {
                for (Command command : commands){
                    if (command.isCommandFor(args[0])){
                        eb.setTitle(String.format("%s Command Help", StringUtils.capitalize(command.getName())));
                        eb.setDescription(command.getDescription());
                        eb.addField("Usage", String.format("!%s %s", command.getName(), command.getArguments()), false);
                        if (command.getAliases().length != 0) {
                            eb.addField("Aliases", String.join(", ", command.getAliases()), false);
                        }
                        break;
                    }
                }
            }

        } else {
            eb.setTitle("Pingo commands");
            fillCommands(eb, false);
        }
        e.getChannel().sendMessage(eb.build()).queue();
    }

    public void fillCommands(EmbedBuilder eb, boolean moderation){
        Map<String, StringBuilder> sbs = new HashMap<>();
        for (Command c : commands) {
            if (c.getCategory() == null || (!c.getCategory().equalsIgnoreCase("hidden") && c.getCategory().equalsIgnoreCase("moderation") == moderation)) {
                String cat = c.getCategory();
                if (!sbs.containsKey(cat)) {
                    sbs.put(cat, new StringBuilder());
                }
                StringBuilder sb = sbs.get(cat);
                sb.append(String.format("\n!%s: *%s*", c.getName(), c.getDescription() == null ? "No help availabe" :  c.getDescription().trim()));
            }
        }
        sbs.get("Pictures").append("\nFor a full list of all pictures command run !help pictures");
        sbs.keySet().forEach(s -> eb.addField(s, sbs.get(s).toString().trim(), false));
    }
}
