package commands.casino.bet;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import companions.CustomBet;
import companions.GameCompanion;
import data.handlers.CreditDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.ArrayList;
import java.util.Map;

public class EndBet extends Command {

    private final GameCompanion gameCompanion;

    public EndBet(GameCompanion gameCompanion){
        this.gameCompanion = gameCompanion;
        this.name = "endbet";
        this.aliases = new String[]{"ebet"};
        this.arguments = "<bet id> <winners>";
        this.description = "end_bet.description";
        this.example = "2 @Jef @Peter";
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
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        int id = args.length == 0 ? -1 : Utils.getInt(args[0]);
        ArrayList<CustomBet> customBet = gameCompanion.getCustomBet(guildId);
        if (id == -1 || customBet.size() < id)
            throw new MessageException(language.getString("bet.error.id"));

        CustomBet bet = customBet.get(id - 1);
        if (bet.isEnded())
            throw new MessageException(language.getString("bet.error.ended", id));

        if (bet.getUserId() != e.getAuthor().getIdLong())
            throw new MessageException(language.getString("end_bet.error.perm"));

        if (args.length == 1 || e.getMessage().getMentionedMembers().size() == 0)
            throw new MessageException(language.getString("end_bet.error.no_winner"));


        ArrayList<Long> winners = new ArrayList<>();
        for (Member m : e.getMessage().getMentionedMembers()){
            if (!bet.didBet(m.getIdLong()))
                throw new MessageException(language.getString("end_bet.error.no_bet", m.getEffectiveName()));
            winners.add(m.getIdLong());
        }
        bet.end();
        int winnerTotal = 0;
        int prize = 0;
        CreditDataHandler dh = new CreditDataHandler();
        for (Map.Entry<Long, Pair<Integer, String>> entry : bet.getBets().entrySet()){
            if (winners.contains(entry.getKey()))
                winnerTotal += entry.getValue().getLeft();
            else {
                dh.addCredits(guildId, entry.getKey(), -entry.getValue().getLeft());
                prize += entry.getValue().getLeft();
            }
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(language.getString("end_bet.ended", bet.getID()), String.format("https://discord.com/channels/%d/%d/%d", guildId, bet.getChannelId(), bet.getMessageId()));
        eb.appendDescription(language.getString("end_bet.prize", prize + winnerTotal));

        for (long winner : winners){
            double percentage = (double) bet.getBet(winner) / winnerTotal;
            int won = (int) (percentage * prize);
            dh.addCredits(guildId, winner, won);
            eb.appendDescription("\n").appendDescription(language.getString("end_bet.winner", String.format("<@!%d>", winner), won, bet.getAnswer(winner)));
        }
        e.getChannel().sendMessageEmbeds(eb.build()).queue();
        e.getMessage().delete().queue();


    }
}
