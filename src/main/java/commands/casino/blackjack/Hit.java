package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

public class Hit extends BCommand {


    public Hit(GameHandler gameHandler){
        super(gameHandler);
        this.name = "Hit";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        if (args.length == 0) {
            BlackJackGame bjg = gameHandler.getBlackJackGame(guildId, id);
            if (bjg != null) {
                bjg.hit();
                updateMessage(e.getChannel(), bjg, new DataHandler(), guildId, id, e.getAuthor().getName());
            }
        }
    }
}
