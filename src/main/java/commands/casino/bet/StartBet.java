package commands.casino.bet;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import companions.CustomBet;
import companions.GameHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

public class StartBet extends Command {

    private final GameHandler gameHandler;

    public StartBet(GameHandler gameHandler){
        this.gameHandler = gameHandler;
        this.name = "startbet";
        this.aliases = new String[]{"sbet"};
        this.category = "Casino";
        this.arguments = "<question>";
        this.description = "start_bet.description";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        CommandState betting = canBeExecuted(guildId, channelId, member, Setting.BETTING);
        CommandState custom = canBeExecuted(guildId, channelId, member, Setting.CUSTOMBET);
        return betting.worst(custom);
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (args.length == 0)
            throw new MessageException(language.getString("start_bet.error.no_question"));

        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(e.getAuthor().getName(), null, e.getAuthor().getAvatarUrl());
        eb.setTitle(Utils.concat(args, 0));
        CustomBet bet = gameHandler.addCustomBet(e.getGuild().getIdLong(), e.getAuthor().getIdLong());
        eb.setFooter(language.getString("start_bet.footer", bet.getID()));
        e.getChannel().sendMessage(eb.build()).queue(m -> {
            bet.setIds(m.getChannel().getIdLong(), m.getIdLong());
        });
        e.getMessage().delete().queue();

    }
}
