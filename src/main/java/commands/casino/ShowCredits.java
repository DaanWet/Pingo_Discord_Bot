package commands.casino;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import companions.GameHandler;
import companions.paginators.BalancePaginator;
import data.DataHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

public class ShowCredits extends Command {

    private final GameHandler handler;

    public ShowCredits(GameHandler handler){
        this.name = "balance";
        this.aliases = new String[]{"bal", "credits", "ShowCredits"};
        this.category = "Casino";
        this.arguments = "[top|global]";
        this.description = "balance.description";
        this.handler = handler;
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        DataHandler dataHandler = new DataHandler();
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (args.length == 0){
            e.getChannel().sendMessage(language.getString("balance", dataHandler.getCredits(e.getGuild().getIdLong(), e.getAuthor().getIdLong()))).queue();
        } else if (args.length == 1 && args[0].matches("(?i)^(top|global)$")){
            boolean global = args[0].equalsIgnoreCase("global");
            BalancePaginator paginator = new BalancePaginator(global, e.getGuild().getIdLong());
            paginator.sendMessage(e.getChannel(), m -> handler.addEmbedPaginator(e.getGuild().getIdLong(), m.getIdLong(), paginator));
        } else {
            throw new MessageException(this.getUsage());
        }
    }
}
