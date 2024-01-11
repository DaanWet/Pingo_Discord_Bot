package me.damascus2000.pingo.commands.casino.blackjack;

import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.cardgames.BlackJackGame;
import me.damascus2000.pingo.data.handlers.CreditDataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;

import java.util.concurrent.TimeUnit;

public class DoubleDown extends BCommand {


    public DoubleDown(GameCompanion gameCompanion){
        super(gameCompanion);
        this.name = "double";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        BlackJackGame bjg = gameCompanion.getBlackJackGame(guildId, id);
        if (args.length == 0 && bjg != null){
            if (!bjg.canDouble()){
                throw new MessageException(language.getString("bj.error.invalid"));
            }
            CreditDataHandler dataHandler = new CreditDataHandler();
            if (dataHandler.getCredits(guildId, id) < 2 * bjg.getBet()){
                throw new MessageException(language.getString("credit.error.not_enough.short"));
            }
            if (bjg.getMessageId() == null){
                e.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                throw new MessageException(language.getString("bj.error.fast"), 5);
            }
            bjg.doubleDown();
            updateMessage(e, bjg, dataHandler, language);
        }
    }
}
