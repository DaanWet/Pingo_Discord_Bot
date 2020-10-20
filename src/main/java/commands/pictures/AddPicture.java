package commands.pictures;

import commands.Command;
import commands.CommandHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static commands.CommandHandler.pathname;

public class AddPicture extends Command {

    private CommandHandler commandHandler;


    public AddPicture(CommandHandler commandHandler) {
        name = "add";
        aliases = new String[]{"addpicture"};
        category = "Pictures";
        this.commandHandler = commandHandler;
        this.description = "Adds a picture to the !<name> command";
        this.arguments = "<name> <picture>";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        Message message = e.getMessage();
        String[] words = message.getContentRaw().split(" ");
        if (words.length == 2 && !message.getAttachments().isEmpty()) {
            try {
                String path = String.format("%s/%s", pathname, words[1].toLowerCase());

                Files.createDirectories(Paths.get(path));
                int i = new File(path).listFiles().length;
                String p = String.format("%s/%s/%d.jpg", pathname, words[1].toLowerCase(), i);
                message.getAttachments().get(0).downloadToFile(p).exceptionally(t -> {
                    t.printStackTrace();
                    e.getChannel().sendMessage("Something went wrong").queue();
                    return null;
                }).thenAccept(a -> e.getMessage().addReaction(":green_tick:667450925677543454").queue());
            } catch (IOException exc1) {
                exc1.printStackTrace();
            }
        } else {
            e.getChannel().sendMessage("Usage: !add <name> <picture>").queue();
        }
    }
}
