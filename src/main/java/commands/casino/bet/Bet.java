package commands.casino.bet;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import companions.CustomBet;
import companions.GameHandler;
import data.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.ArrayList;

public class Bet extends Command {

    private final GameHandler gameHandler;

    public Bet(GameHandler gameHandler){
        this.gameHandler = gameHandler;
        this.name = "bet";
        this.arguments = "<bet id> <credits> <answer>";
        this.description = "bet.description";
        this.category = Category.CASINO;
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        CommandState betting = canBeExecuted(guildId, channelId, member, Setting.BETTING);
        CommandState custom = canBeExecuted(guildId, channelId, member, Setting.CUSTOMBET);
        return betting.worst(custom);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        MyResourceBundle language = getLanguage(e);
        int id = args.length == 0 ? -1 : Utils.getInt(args[0]);
        ArrayList<CustomBet> customBet = gameHandler.getCustomBet(e.getGuild().getIdLong());
        if (id == -1 || customBet.size() < id)
            throw new MessageException(language.getString("bet.error.id"));

        CustomBet cbet = customBet.get(id - 1);

        if (cbet.isEnded())
            throw new MessageException(language.getString("bet.error.ended", id));

        if (cbet.didBet(e.getAuthor().getIdLong()))
            throw new MessageException(language.getString("bet.error.already_made"));

        long userId = e.getAuthor().getIdLong();
        int bet = args.length == 1 ? 0 : Utils.getInt(args[1]);
        if (bet < 10) // Should this value be added to properties?
            throw new MessageException(language.getString("credit.error.least"));

        if (new DataHandler().getCredits(e.getGuild().getIdLong(), userId) < bet)
            throw new MessageException(language.getString("credit.error.not_enough", bet));

        if (args.length == 2)
            throw new MessageException(language.getString("bet.error.answer"));


        String answer = Utils.concat(args, 2);
        cbet.addBet(userId, bet, answer);
        e.getMessage().addReaction(Utils.config.getProperty("emoji.checkmark")).queue();
        e.getGuild().getTextChannelById(cbet.getChannelId()).retrieveMessageById(cbet.getMessageId()).queue(m -> {
            MessageEmbed me = m.getEmbeds().get(0);
            EmbedBuilder eb = new EmbedBuilder(me);
            eb.appendDescription("\n").appendDescription(language.getString("bet.success", String.format("<@!%d>", userId), bet, answer));
            m.editMessage(eb.build()).queue();
        });

    }
}
