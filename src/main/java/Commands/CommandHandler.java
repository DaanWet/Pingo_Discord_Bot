package Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandHandler {

    private Random random;
    private Set<String> commands;
    private final String pathname = "./Pictures";
    private final ArrayList<String> rythmcommands = new ArrayList<>(Arrays.asList("!play", "!stop", "!np", "!queue", "!skipto", "!next", "!skip"));

    public CommandHandler() {
        random = new Random();
        File dir = new File(pathname);
        commands = new HashSet<>();
        for (File file : dir.listFiles()) {
            commands.add(file.getName());
        }
    }


    public void onCommandReceived(GuildMessageReceivedEvent e) {
        User author = e.getAuthor();
        TextChannel channel = e.getChannel();
        Message message = e.getMessage();

        String[] words = message.getContentRaw().split(" ");
        String command = words[0].substring(1);
        if (commands.contains(command.toLowerCase())) {
            try {
                File dir = new File(String.format("%s/%s", pathname, command));
                File foto = new File(String.format("%s/%s/%d.jpg", pathname, command, random.nextInt(dir.listFiles().length)));
                channel.sendFile(foto.getAbsoluteFile()).queue();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        } else if (command.equalsIgnoreCase("add")) {
            if (words.length == 2 && !message.getAttachments().isEmpty()) {
                try {
                    String path = String.format("%s/%s", pathname, words[1].toLowerCase());

                    Files.createDirectories(Paths.get(path));
                    int i = new File(path).listFiles().length;
                    String p = String.format("%s/%s/%d.jpg", pathname, words[1].toLowerCase(), i);
                    message.getAttachments().get(0).downloadToFile(p).exceptionally(t -> {
                        t.printStackTrace();
                        return null;
                    }).thenAccept(a -> {
                        if (i > 0) {
                            commands.add(words[1].toLowerCase());
                        }

                    });
                } catch (IOException exc1) {
                    exc1.printStackTrace();
                }
            } else {
                channel.sendMessage("Usage: !add <name> <picture>").queue();
            }
        } else if (command.equalsIgnoreCase("help")) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Pingo commands");
            eb.addField("!add <name> <picture>", "Adds a picture to the !<name> command", false);
            File dir = new File(pathname);
            for (File directory : dir.listFiles()) {
                eb.addField(String.format("!%s", directory.getName()), String.format("Shows a random picture of %s", directory.getName()), false);
            }
            channel.sendMessage(eb.build()).queue();
        } else if (command.equalsIgnoreCase("fuckpingo")) {
            channel.sendMessage("No, Fuck You").queue();
        }
    }
}
