package commands.casino.blackjack;

import companions.GameCompanion;
import companions.cardgames.BlackJackGame;
import data.handlers.CreditDataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.concurrent.TimeUnit;

public class Stand extends BCommand {

    public Stand(GameCompanion gameCompanion){
        super(gameCompanion);
        this.name = "stand";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        BlackJackGame bjg = gameCompanion.getBlackJackGame(guildId, id);
        if (args.length == 0 && bjg != null){
            MyResourceBundle language = Utils.getLanguage(guildId);
            if (bjg.getMessageId() == null){
                e.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                throw new MessageException(language.getString("bj.error.fast"), 5);
            }
            bjg.stand();
            updateMessage(e, bjg, new CreditDataHandler(), language);

        }
    }
}
