package Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.util.Random;

import static Commands.CommandHandler.pathname;

public class DeletePicture extends Command{

    private CommandHandler commandHandler;
    private Random random = new Random();

    public DeletePicture(CommandHandler commandHandler){
        name = "delete";
        aliases = new String[]{"del", "deletepicture"};
        this.commandHandler = commandHandler;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 2 && commandHandler.getPcommands().contains(args[1])){
            EmbedBuilder eb = new EmbedBuilder();
            eb.setImage(String.format("http://zwervers.wettinck.be/%s/%d&%d=%d", args[1], 0, random.nextInt(), random.nextInt()));
            eb.setTitle("Delete pictures from " + args[1]);
            eb.setDescription("0.jpg");
            e.getChannel().sendMessage(eb.build()).queue(m -> {
                m.addReaction("U+25C0").queue();
                m.addReaction("U+1F5D1").queue();
                m.addReaction("U+25B6").queue();
                m.addReaction("U+274C").queue();
            });

        } else {
            e.getChannel().sendMessage("Usage: !delete <command>").queue();
        }
    }

    @Override
    public String getDescription() {
        return "Gets the prompt to delete a picture";
    }
}
