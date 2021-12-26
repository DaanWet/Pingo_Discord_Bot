package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;
import utils.MessageException;

public class Split extends BCommand {

    public Split(GameHandler gameHandler){
        super(gameHandler);
        this.name = "split";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        BlackJackGame bjg = gameHandler.getBlackJackGame(guildId, id);
        if (args.length == 0 && bjg != null){
            DataHandler dataHandler = new DataHandler();
            if (dataHandler.getCredits(guildId, id) < 2 * bjg.getBet())
                throw new MessageException("You have not enough credits");

            bjg.split();
            updateMessage(e.getChannel(), bjg, dataHandler, guildId, id, e.getAuthor().getName());
        }
    }
}
