package Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class CommandHandler {

    private Random random;

    private ArrayList<Command> commands;
    public static final String pathname = "./Pictures";
    private final ArrayList<String> rythmcommands = new ArrayList<>(Arrays.asList("!play", "!stop", "!np", "!queue", "!skipto", "!next", "!skip"));

    public CommandHandler() {
        random = new Random();
        Help hc = new Help();
        commands = new ArrayList<>(Arrays.asList(hc, new AddPicture(this), new FuckPingo(), new DeletePicture(this)));
        hc.setCommands(commands);
    }

    public Set<String> getPcommands(){
        File cdir = new File(pathname);
        Set<String> pcommands = new HashSet<>();
        for (File file : cdir.listFiles()) {
            pcommands.add(file.getName());
        }
        return pcommands;
    }

    public void onCommandReceived(GuildMessageReceivedEvent e) {
        User author = e.getAuthor();
        TextChannel channel = e.getChannel();
        Message message = e.getMessage();

        String[] words = message.getContentRaw().split(" ");
        String command = words[0].substring(1);
        for (Command c : commands){
            if (c.isCommandFor(command)){
                String[] args = Stream.concat(Arrays.stream(words, 1, words.length), Arrays.stream(words, 1, words.length))
                        .toArray(String[]::new);
                c.run(args, e);
            }
        }
        Set<String> pcommands = getPcommands();
        if (pcommands.contains(command.toLowerCase())) {
            try {
                File dir = new File(String.format("%s/%s", pathname, command));
                File foto = new File(String.format("%s/%s/%d.jpg", pathname, command, random.nextInt(dir.listFiles().length)));
                channel.sendFile(foto.getAbsoluteFile()).queue();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
