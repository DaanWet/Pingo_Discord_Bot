package commands.casino.bet;

import casino.CustomBet;
import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
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
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {

        int id = args.length == 0 ? -1 : Utils.getInt(args[0]);
        ArrayList<CustomBet> customBet = gameHandler.getCustomBet(e.getGuild().getIdLong());
        if (id == -1 || customBet.size() < id)
            throw new MessageException("Please give a valid ID");

        if (args.length == 1)
            throw new MessageException("You need to select someone who won");

        CustomBet bet = customBet.get(id - 1);
        ArrayList<Long> winners = new ArrayList<>();
        for (Member m : e.getMessage().getMentionedMembers()){
            if (!bet.didBet(m.getIdLong()))
                throw new MessageException(String.format("%s did not bet and can't win"));

            winners.add(m.getIdLong());
        }

        int winnerTotal = 0;
        int prize = 0;
        for (Map.Entry<Long, Integer> entry : bet.getBets().entrySet()){
            if (winners.contains(entry.getKey()))
                winnerTotal += entry.getValue();
            else
                prize += entry.getValue();
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(String.format("Bet #%d has ended", bet.getID()), String.format("https://discord.com/channels/%d/%d/%d", e.getGuild().getIdLong(), bet.getChannelId(), bet.getMessageId()));
        eb.appendDescription(String.format("There was a prize pool of %d credits", prize + winnerTotal));
        for(long winner : winners){
            double percentage = (double) bet.getBet(winner) / winnerTotal;
            int won = (int) (percentage * prize);
            eb.appendDescription(String.format("<@!%d> won %d credits", winner, won));
        }
        e.getChannel().sendMessage(eb.build());


    }
}
