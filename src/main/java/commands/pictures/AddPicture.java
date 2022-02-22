package commands.pictures;

import commands.Command;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static listeners.CommandHandler.pathname;

public class AddPicture extends Command {


    public AddPicture(){
        name = "add";
        aliases = new String[]{"addpicture"};
        category = Category.PICTURES;
        this.description = "picture.add.description";
        this.arguments = "<name> <picture>";
        this.priveligedGuild = Utils.config.get("special.guild");
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        Message message = e.getMessage();
        String[] words = message.getContentRaw().split(" ");
        if (words.length != 2 || message.getAttachments().isEmpty())
            throw new MessageException(getUsage(e.getGuild().getIdLong()));
        try {
            String path = String.format("%s/%s", pathname, words[1].toLowerCase());

            Files.createDirectories(Paths.get(path));
            int i = new File(path).listFiles().length;
            String p = String.format("%s/%s/%d.jpg", pathname, words[1].toLowerCase(), i);
            message.getAttachments().get(0).downloadToFile(p).exceptionally(t -> {
                t.printStackTrace();
                throw new MessageException("Something went wrong");
            }).thenAccept(a -> e.getMessage().addReaction(Utils.config.getProperty("emoji.green_tick")).queue());
        } catch (IOException exc1){
            exc1.printStackTrace();
        }
    }
}
