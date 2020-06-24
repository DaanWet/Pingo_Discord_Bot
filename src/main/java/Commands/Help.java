package Commands;

import net.dv8tion.jda.api.EmbedBuilder;
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
        eb.setTitle("Pingo commands");

        Map<String, StringBuilder> sbs = new HashMap<>();
        for (Command c : commands) {
            if (c.getCategory() == null ||  !c.getCategory().equalsIgnoreCase("moderation")) {
                String cat = c.getCategory();
                if (!sbs.containsKey(cat)) {
                    sbs.put(cat, new StringBuilder());
                }
                StringBuilder sb = sbs.get(cat);
                String[] als = c.getAliases();
                sb.append(String.format("\n!%s%s", c.getName(), als.length > 0 ? "[" : ""));
                Arrays.stream(c.getAliases()).forEach(alias -> sb.append(alias).append(", "));
                if (als.length > 0){
                    sb.delete(sb.length() - 2, sb.length());
                }
                sb.append(String.format("%s: *%s*", als.length > 0 ? "]" : "" ,c.getDescription().trim()));
            }
        }
        File dir = new File(pathname);
        for (File directory : dir.listFiles()) {
            sbs.get("Pictures").append(String.format("\n!%s: *Shows a random picture of %s*", directory.getName(), directory.getName()));
        }
        eb.setColor(Color.BLUE);
        sbs.keySet().forEach(s -> eb.addField(s, sbs.get(s).toString().trim(), false));


        e.getChannel().sendMessage(eb.build()).queue();
    }

    @Override
    public String getDescription() {
        return "Shows this help overview";
    }
}
