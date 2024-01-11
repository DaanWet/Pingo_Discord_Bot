package me.damascus2000.pingo.commands.casino;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.commands.settings.CommandState;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.companions.DataCompanion;
import me.damascus2000.pingo.companions.paginators.BalancePaginator;
import me.damascus2000.pingo.data.handlers.CreditDataHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;

public class ShowCredits extends Command {

    private final DataCompanion handler;

    public ShowCredits(DataCompanion handler){
        this.name = "balance";
        this.aliases = new String[]{"bal", "credits", "ShowCredits"};
        this.category = Category.CASINO;
        this.arguments = new String[]{"[**top**|**global**]"};
        this.description = "balance.description";
        this.example = "top";
        this.handler = handler;
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        CreditDataHandler dataHandler = new CreditDataHandler();
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (args.length == 0){
            e.getChannel().sendMessage(language.getString("balance", dataHandler.getCredits(e.getGuild().getIdLong(), e.getAuthor().getIdLong()))).queue();
        } else if (args.length == 1 && args[0].matches("(?i)^(top|global)$")){
            boolean global = args[0].equalsIgnoreCase("global");
            BalancePaginator paginator = new BalancePaginator(global, e.getGuild().getIdLong());
            paginator.sendMessage(e.getChannel(), m -> handler.addEmbedPaginator(e.getGuild().getIdLong(), m.getIdLong(), paginator));
        } else {
            throw new MessageException(this.getUsage(e.getGuild().getIdLong()));
        }
    }
}
