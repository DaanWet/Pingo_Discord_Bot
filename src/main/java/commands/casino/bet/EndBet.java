package commands.casino.bet;

import casino.CustomBet;
import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.DataHandler;
import utils.MessageException;
import utils.Utils;

import java.util.ArrayList;
import java.util.Map;

public class EndBet extends Command {

    private GameHandler gameHandler;

    public EndBet(GameHandler gameHandler){
        this.gameHandler = gameHandler;
        this.name = "endbet";
        this.aliases = new String[]{"ebet"};
        this.arguments = "<bet id> <winners>";
        this.description = "Ends a running bet";
        this.category = "Casino";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {

        long guildId = e.getGuild().getIdLong();
        int id = args.length == 0 ? -1 : Utils.getInt(args[0]);
        ArrayList<CustomBet> customBet = gameHandler.getCustomBet(guildId);
        if (id == -1 || customBet.size() < id)
            throw new MessageException("Please give a valid ID");

        CustomBet bet = customBet.get(id - 1);
        if (bet.isEnded())
            throw new MessageException("This bet has already ended");

        if (bet.getUserId() != e.getAuthor().getIdLong())
            throw new MessageException("You can't end this bet");

        if (args.length == 1 || e.getMessage().getMentionedMembers().size() == 0)
            throw new MessageException("You need to select someone who won");


        ArrayList<Long> winners = new ArrayList<>();
        for (Member m : e.getMessage().getMentionedMembers()){
            if (!bet.didBet(m.getIdLong()))
                throw new MessageException(String.format("%s did not bet and can't win"));

            winners.add(m.getIdLong());
        }
        bet.end();
        int winnerTotal = 0;
        int prize = 0;
        DataHandler dh = new DataHandler();
        for (Map.Entry<Long, Pair<Integer, String>> entry : bet.getBets().entrySet()){
            if (winners.contains(entry.getKey()))
                winnerTotal += entry.getValue().getLeft();
            else {
                dh.addCredits(guildId, entry.getKey(), -entry.getValue().getLeft());
                prize += entry.getValue().getLeft();
            }
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(String.format("Bet #%d has ended", bet.getID()), String.format("https://discord.com/channels/%d/%d/%d", guildId, bet.getChannelId(), bet.getMessageId()));
        eb.appendDescription(String.format("There was a prize pool of %d credits", prize + winnerTotal));

        for(long winner : winners){
            double percentage = (double) bet.getBet(winner) / winnerTotal;
            int won = (int) (percentage * prize);
            dh.addCredits(guildId, winner, won);
            eb.appendDescription(String.format("\n<@!%d> won %d credits with %s", winner, won, bet.getAnswer(winner)));
        }
        e.getChannel().sendMessage(eb.build()).queue();
        e.getMessage().delete().queue();


    }
}
