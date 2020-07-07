package Commands;

import Utils.OpenExplorerData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DeletePicture extends Command{

    private CommandHandler commandHandler;
    private Random random = new Random();
    private HashMap<String, OpenExplorerData> openExplorers = new HashMap();
    private HashMap<String, ScheduledFuture<?>> autoClosers = new HashMap<>();

    public DeletePicture(CommandHandler commandHandler){
        name = "delete";
        aliases = new String[]{"del", "deletepicture"};
        this.commandHandler = commandHandler;
        category = "Pictures";
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
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 2 && commandHandler.getPcommands().contains(args[1].toLowerCase())){
            if (openExplorers.containsKey(args[1])){
                e.getChannel().sendMessage("An explorer is already open").queue();
            } else {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setImage(String.format("http://zwervers.wettinck.be/%s/%d&%d=%d", args[1], 0, random.nextInt(), random.nextInt()));
                eb.setTitle("Delete pictures from " + args[1]);
                eb.setDescription("0.jpg");
                e.getChannel().sendMessage(eb.build()).queue(m -> {
                    openExplorers.put(args[1], new OpenExplorerData(e.getAuthor().getId(),  e.getChannel().getId(), e.getMessage().getId(), e.getGuild()));
                    m.addReaction("U+25C0").queue();
                    m.addReaction("U+1F5D1").queue();
                    m.addReaction("U+25B6").queue();
                    m.addReaction("U+274C").queue();
                    autoClosers.put(args[1], deleteMessage(args[1], m).queueAfter(10, TimeUnit.MINUTES));

                });

            }


        } else {
            e.getChannel().sendMessage("Usage: !delete <command>").queue();
        }
    }

    @Override
    public String getDescription() {
        return "Gets the prompt to delete a picture";
    }
}
