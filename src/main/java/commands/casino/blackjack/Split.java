package commands.casino.blackjack;

import companions.GameHandler;
import companions.cardgames.BlackJackGame;
import data.handlers.CreditDataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

public class Split extends BCommand {

    public Split(GameHandler gameHandler){
        super(gameHandler);
        this.name = "split";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        BlackJackGame bjg = gameHandler.getBlackJackGame(guildId, id);
        if (args.length == 0 && bjg != null){
            CreditDataHandler dataHandler = new CreditDataHandler();
            if (dataHandler.getCredits(guildId, id) < 2 * bjg.getBet())
                throw new MessageException(language.getString("credit.error.not_enough.short"));

            bjg.split();
            updateMessage(e, bjg, dataHandler, language);
        }
    }
}
