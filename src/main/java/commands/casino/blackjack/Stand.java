package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.DataHandler;

public class Stand extends BCommand {

    public Stand(GameHandler gameHandler) {
        super(gameHandler);
        this.name = "Stand";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        if (args.length == 0) {
            BlackJackGame bjg = gameHandler.getBlackJackGame(guildId, id);
            if (bjg != null) {
                bjg.stand();
                updateMessage(e.getChannel(), bjg, new DataHandler(), guildId, id, e.getAuthor().getName());
            }
        }
    }
}
