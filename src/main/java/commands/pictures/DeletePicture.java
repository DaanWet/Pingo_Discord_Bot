package commands.pictures;

import commands.Command;
import companions.DataCompanion;
import companions.paginators.OpenExplorerData;
import listeners.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DeletePicture extends Command {

    private final CommandHandler commandHandler;
    private final DataCompanion dataCompanion;
    private final Random random = new Random();

    public DeletePicture(CommandHandler commandHandler, DataCompanion dataCompanion){
        this.name = "delete";
        this.aliases = new String[]{"del", "deletepicture"};
        this.commandHandler = commandHandler;
        this.category = Category.PICTURES;
        this.description = "picture.delete.description";
        this.arguments = "<command>";
        this.priveligedGuild = 203572340280262657L;
        this.dataCompanion = dataCompanion;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (e.getGuild().getIdLong() != 203572340280262657L) return;
        if (args.length != 1 || !commandHandler.getPcommands().contains(args[0].toLowerCase()))
            throw new MessageException(getUsage(e.getGuild().getIdLong()));
        MyResourceBundle language = getLanguage(e);
        if (dataCompanion.getExplorerData(args[0]) == null)
            throw new MessageException(language.getString("picture.delete.error"));

        EmbedBuilder eb = new EmbedBuilder();
        Properties config = Utils.config;
        eb.setImage(String.format("%s/%s/%d&%d=%d", config.getProperty("pictures.url"), args[0], 0, random.nextInt(), random.nextInt()));
        eb.setTitle(language.getString("picture.delete.embed", args[0]));
        eb.setDescription("0.jpg");
        e.getChannel().sendMessageEmbeds(eb.build()).queue(m -> {
            dataCompanion.putExplorer(args[0], new OpenExplorerData(e.getAuthor().getIdLong(), e.getChannel().getIdLong(), e.getMessage().getIdLong(), e.getGuild(), args[0]));
            m.addReaction(config.getProperty("emoji.previous")).queue();
            m.addReaction(config.getProperty("emoji.trash")).queue();
            m.addReaction(config.getProperty("emoji.next")).queue();
            m.addReaction(config.getProperty("emoji.cancel")).queue();
            dataCompanion.putAutoCloser(args[0], dataCompanion.deleteMessage(args[0], m).queueAfter((int) config.get("timeout"), TimeUnit.MINUTES));
        });


    }
}
