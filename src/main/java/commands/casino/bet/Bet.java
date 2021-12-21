package commands.casino.bet;

import casino.CustomBet;
import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.Utils;

import java.util.ArrayList;

public class Bet extends Command {

    private GameHandler gameHandler;

    public Bet(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.name = "bet";
        this.arguments = "<bet id> <credits> <answer>";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {

        int id = args.length == 0 ? -1 : Utils.getInt(args[0]);
        ArrayList<CustomBet> customBet = gameHandler.getCustomBet(e.getGuild().getIdLong());
        if (id == -1 || customBet.size() < id)
            throw new MessageException("Please give a valid ID");


        int bet = args.length == 1 ? 0 : Utils.getInt(args[1]);
        if (bet < 10)
            throw new MessageException("You need to place a bet for at least 10 credits");

        if (args.length == 2)
            throw new MessageException("You need to provide an answer to the question");

        CustomBet cbet = customBet.get(id);
        cbet.addBet(e.getAuthor().getIdLong(), bet);
        e.getMessage().addReaction("✅").queue();

    }
}
