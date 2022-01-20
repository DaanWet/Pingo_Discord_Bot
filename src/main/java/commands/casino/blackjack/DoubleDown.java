package commands.casino.blackjack;

import companions.GameHandler;
import companions.cardgames.BlackJackGame;
import data.DataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

public class DoubleDown extends BCommand {


    public DoubleDown(GameHandler gameHandler){
        super(gameHandler);
        this.name = "double";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        BlackJackGame bjg = gameHandler.getBlackJackGame(guildId, id);
        if (args.length == 0 && bjg != null){
            if (!bjg.canDouble()){
                throw new MessageException(language.getString("bj.error.invalid"));
            }
            DataHandler dataHandler = new DataHandler();
            if (new DataHandler().getCredits(guildId, id) < 2 * bjg.getBet()){
                throw new MessageException(language.getString("credit.error.not_enough.short"));
            }

            bjg.doubleDown();
            updateMessage(e.getChannel(), bjg, dataHandler, guildId, id, e.getAuthor().getName(), language);
        }
    }
}
