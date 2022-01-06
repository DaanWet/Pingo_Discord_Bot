package commands.casino.blackjack;

import companions.cardgames.BlackJackGame;
import companions.GameHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import data.DataHandler;

public class Stand extends BCommand {

    public Stand(GameHandler gameHandler){
        super(gameHandler);
        this.name = "Stand";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        BlackJackGame bjg = gameHandler.getBlackJackGame(guildId, id);
        if (args.length == 0 && bjg != null){
            bjg.stand();
            updateMessage(e.getChannel(), bjg, new DataHandler(), guildId, id, e.getAuthor().getName());

        }
    }
}
