package commands.casino.blackjack;

import companions.GameHandler;
import companions.cardgames.BlackJackGame;
import data.handlers.CreditDataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.Utils;

public class Hit extends BCommand {


    public Hit(GameHandler gameHandler){
        super(gameHandler);
        this.name = "Hit";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        BlackJackGame bjg = gameHandler.getBlackJackGame(guildId, id);
        if (args.length == 0 && bjg != null){
            bjg.hit();
            updateMessage(e, bjg, new CreditDataHandler(), Utils.getLanguage(guildId));
        }
    }
}
