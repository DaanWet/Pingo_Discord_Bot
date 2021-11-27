package commands.casino;

import casino.BalancePaginator;
import casino.GameHandler;
import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShowCredits extends Command {

    private GameHandler handler;

    public ShowCredits(GameHandler handler) {
        this.name = "Balance";
        this.aliases = new String[]{"bal", "credits", "ShowCredits"};
        this.category = "Casino";
        this.arguments = "[top|global]";
        this.description = "Show your current credit balance";
        this.handler = handler;
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        DataHandler dataHandler = new DataHandler();
        if (args.length == 0) {
            e.getChannel().sendMessage(String.format("Your current balance is **%d**", dataHandler.getCredits(e.getGuild().getIdLong(), e.getAuthor().getIdLong()))).queue();
        } else if (args.length == 1 && args[0].matches("(?i)^(top|global)$") ) {
            boolean global = args[0].equalsIgnoreCase("global");
            BalancePaginator paginator = new BalancePaginator(global, e.getGuild().getIdLong());
            paginator.sendMessage(e.getChannel(), m -> handler.addEmbedPaginator(e.getGuild().getIdLong(), m.getIdLong(), paginator));
        } else {
            e.getChannel().sendMessage(this.getUsage()).queue();
        }
    }
}
