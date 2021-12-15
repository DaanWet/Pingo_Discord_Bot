package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;
import utils.MessageException;

public class DoubleDown extends BCommand {


    public DoubleDown(GameHandler gameHandler) {
        super(gameHandler);
        this.name = "double";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        if (args.length == 0) {
            BlackJackGame bjg = gameHandler.getBlackJackGame(guildId, id);
            if (bjg != null) {
                if (!bjg.canDouble()) {
                    throw new MessageException("You can't do that");
                }
                DataHandler dataHandler = new DataHandler();
                if (new DataHandler().getCredits(guildId, id) < 2*bjg.getBet()) {
                    throw new MessageException("You have not enough credits");
                }
                
                bjg.doubleDown();
                updateMessage(e.getChannel(), bjg, dataHandler, guildId, id, e.getAuthor().getName());

            }
        }
    }
}
