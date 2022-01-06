package commands.casino.blackjack;

import companions.GameHandler;
import companions.cardgames.BlackJackGame;
import data.DataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
            updateMessage(e.getChannel(), bjg, new DataHandler(), guildId, id, e.getAuthor().getName());
        }
    }
}
