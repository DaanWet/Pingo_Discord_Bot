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
import utils.Utils;

import java.util.ArrayList;

public class Bet extends Command {

    private GameHandler gameHandler;

    public Bet(GameHandler gameHandler){
        this.gameHandler = gameHandler;
        this.name = "bet";
        this.arguments = "<bet id> <credits> <answer>";
        this.description = "Bet on a running bet";
        this.category = "Casino";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        CommandState betting = canBeExecuted(guildId, channelId, member, Setting.BETTING);
        CommandState custom = canBeExecuted(guildId, channelId, member, Setting.CUSTOMBET);
        return betting.worst(custom);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{

        int id = args.length == 0 ? -1 : Utils.getInt(args[0]);
        ArrayList<CustomBet> customBet = gameHandler.getCustomBet(e.getGuild().getIdLong());
        if (id == -1 || customBet.size() < id)
            throw new MessageException("Please give a valid ID");

        CustomBet cbet = customBet.get(id - 1);

        if (cbet.isEnded())
            throw new MessageException("This bet has already ended");

        if (cbet.didBet(e.getAuthor().getIdLong()))
            throw new MessageException("You already made a bet");

        long userId = e.getAuthor().getIdLong();
        int bet = args.length == 1 ? 0 : Utils.getInt(args[1]);
        if (bet < 10)
            throw new MessageException("You need to place a bet for at least 10 credits");

        if (new DataHandler().getCredits(e.getGuild().getIdLong(), userId) < bet)
            throw new MessageException(String.format("You don't have enough credits to make a %d credits bet", bet));

        if (args.length == 2)
            throw new MessageException("You need to provide an answer to the question");


        String answer = Utils.concat(args, 2);
        cbet.addBet(userId, bet, answer);
        e.getMessage().addReaction("✅").queue();
        e.getGuild().getTextChannelById(cbet.getChannelId()).retrieveMessageById(cbet.getMessageId()).queue(m -> {
            MessageEmbed me = m.getEmbeds().get(0);
            EmbedBuilder eb = new EmbedBuilder(me);
            eb.appendDescription(String.format("\n<@!%d> bet %d on %s", userId, bet, answer));
            m.editMessage(eb.build()).queue();
        });

    }
}
