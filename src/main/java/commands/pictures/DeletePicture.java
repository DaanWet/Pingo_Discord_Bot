package commands.pictures;

import commands.Command;
import companions.paginators.OpenExplorerData;
import listeners.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DeletePicture extends Command {

    private final CommandHandler commandHandler;
    private final Random random = new Random();
    private final HashMap<String, OpenExplorerData> openExplorers = new HashMap<>();
    private final HashMap<String, ScheduledFuture<?>> autoClosers = new HashMap<>();

    public DeletePicture(CommandHandler commandHandler){
        this.name = "delete";
        this.aliases = new String[]{"del", "deletepicture"};
        this.commandHandler = commandHandler;
        this.category = Category.PICTURES;
        this.description = "picture.delete.description";
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
        MyResourceBundle language = getLanguage(e);
        if (openExplorers.containsKey(args[0]))
            throw new MessageException(language.getString("picture.delete.error"));

        EmbedBuilder eb = new EmbedBuilder();
        Properties config = Utils.config;
        eb.setImage(String.format("%s/%s/%d&%d=%d", config.getProperty("pictures.url") , args[0], 0, random.nextInt(), random.nextInt()));
        eb.setTitle(language.getString("picture.delete.embed", args[0]));
        eb.setDescription("0.jpg");
        e.getChannel().sendMessage(eb.build()).queue(m -> {
            openExplorers.put(args[0], new OpenExplorerData(e.getAuthor().getId(), e.getChannel().getId(), e.getMessage().getId(), e.getGuild()));
            m.addReaction(config.getProperty("emoji.previous")).queue();
            m.addReaction(config.getProperty("emoji.trash")).queue();
            m.addReaction(config.getProperty("emoji.next")).queue();
            m.addReaction(config.getProperty("emoji.cancel")).queue();
            autoClosers.put(args[0], deleteMessage(args[0], m).queueAfter(10, TimeUnit.MINUTES));// Should this value be added to properties?
        });


    }
}
