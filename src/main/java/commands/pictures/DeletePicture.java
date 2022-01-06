package commands.pictures;

import commands.Command;
import listeners.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import utils.MessageException;
import companions.paginators.OpenExplorerData;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DeletePicture extends Command {

    private CommandHandler commandHandler;
    private Random random = new Random();
    private HashMap<String, OpenExplorerData> openExplorers = new HashMap();
    private HashMap<String, ScheduledFuture<?>> autoClosers = new HashMap<>();

    public DeletePicture(CommandHandler commandHandler){
        this.name = "delete";
        this.aliases = new String[]{"del", "deletepicture"};
        this.commandHandler = commandHandler;
        this.category = "Pictures";
        this.description = "Gets the prompt to delete a picture";
        this.arguments = "<command>";
        this.priveligedGuild = 203572340280262657L;
    }

    public OpenExplorerData getExplorerData(String command){
        return openExplorers.getOrDefault(command, null);
    }

    public RestAction<?> deleteMessage(String command, Message message){
        RestAction<Message> getM = openExplorers.get(command).getMessage();
        return message.delete().flatMap(s -> getM.flatMap(Message::delete));
    }

    public void closeExplorer(String command, Message message){
        autoClosers.get(command).cancel(false);
        deleteMessage(command, message).queue();
        openExplorers.remove(command);
        autoClosers.remove(command);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (e.getGuild().getIdLong() != 203572340280262657L) return;
        if (args.length != 1 || !commandHandler.getPcommands().contains(args[0].toLowerCase()))
            throw new MessageException(getUsage());

        if (openExplorers.containsKey(args[0]))
            throw new MessageException("An explorer is already open");

        EmbedBuilder eb = new EmbedBuilder();
        eb.setImage(String.format("http://zwervers.wettinck.be/%s/%d&%d=%d", args[0], 0, random.nextInt(), random.nextInt()));
        eb.setTitle("Delete pictures from " + args[0]);
        eb.setDescription("0.jpg");
        e.getChannel().sendMessage(eb.build()).queue(m -> {
            openExplorers.put(args[0], new OpenExplorerData(e.getAuthor().getId(), e.getChannel().getId(), e.getMessage().getId(), e.getGuild()));
            m.addReaction("U+25C0").queue();
            m.addReaction("U+1F5D1").queue();
            m.addReaction("U+25B6").queue();
            m.addReaction("U+274C").queue();
            autoClosers.put(args[0], deleteMessage(args[0], m).queueAfter(10, TimeUnit.MINUTES));
        });


    }
}
