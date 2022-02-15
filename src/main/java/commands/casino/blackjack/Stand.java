package commands.casino.blackjack;

import companions.GameCompanion;
import companions.cardgames.BlackJackGame;
import data.handlers.CreditDataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.Utils;

public class Stand extends BCommand {

    public Stand(GameCompanion gameCompanion){
        super(gameCompanion);
        this.name = "Stand";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        BlackJackGame bjg = gameCompanion.getBlackJackGame(guildId, id);
        if (args.length == 0 && bjg != null){
            bjg.stand();
            updateMessage(e, bjg, new CreditDataHandler(), Utils.getLanguage(guildId));

        }
    }
}
