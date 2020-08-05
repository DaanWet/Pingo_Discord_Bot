package Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.io.File;
import java.util.*;

import static Commands.CommandHandler.pathname;

public class Help extends Command{

    private Collection<Command> commands;

    public Help(){
        name = "help";
        aliases = new String[]{"commands", "command", "h", "test"};
    }

    public void setCommands(Map<String, Command> comm){
        commands = comm.values();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        EmbedBuilder eb = new EmbedBuilder();
        if (args.length == 1 && args[0].equalsIgnoreCase("moderation")){
            if (e.getMember().hasPermission(Permission.ADMINISTRATOR)){
                eb.setTitle("Pingo Moderation commands");
                fillCommands(eb, true);
                e.getChannel().sendMessage(eb.build()).queue();
            } else {
                e.getChannel().sendMessage("❌ You don't have permission to run this command").queue();
            }
        } else {
            eb.setTitle("Pingo commands");
            fillCommands(eb, false);
            e.getChannel().sendMessage(eb.build()).queue();
        }


    }

    public void fillCommands(EmbedBuilder eb, boolean moderation){
        Map<String, StringBuilder> sbs = new HashMap<>();
        for (Command c : commands) {
            if (c.getCategory() == null || c.getCategory().equalsIgnoreCase("moderation") == moderation) {
                String cat = c.getCategory();
                if (!sbs.containsKey(cat)) {
                    sbs.put(cat, new StringBuilder());
                }
                StringBuilder sb = sbs.get(cat);
                String[] als = c.getAliases();
                sb.append(String.format("\n!%s%s", c.getName(), als.length > 0 ? "[" : ""));
                Arrays.stream(c.getAliases()).forEach(alias -> sb.append(alias).append(", "));
                if (als.length > 0) {
                    sb.delete(sb.length() - 2, sb.length());
                }
                sb.append(String.format("%s: *%s*", als.length > 0 ? "]" : "", c.getDescription().trim()));
            }
        }
        if (!moderation){
            File dir = new File(pathname);
            for (File directory : dir.listFiles()) {
                sbs.get("Pictures").append(String.format("\n!%s: *Shows a random picture of %s*", directory.getName(), directory.getName()));
            }
        }

        eb.setColor(Color.BLUE);
        sbs.keySet().forEach(s -> eb.addField(s, sbs.get(s).toString().trim(), false));
    }


    @Override
    public String getDescription() {
        return "Shows this help overview";
    }
}
