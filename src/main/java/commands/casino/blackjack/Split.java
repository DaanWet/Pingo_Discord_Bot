package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.DataHandler;

public class Split extends BCommand {

    public Split(GameHandler gameHandler) {
        super(gameHandler);
        this.name = "split";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        if (args.length == 0) {
            BlackJackGame bjg = gameHandler.getBlackJackGame(guildId, id);
            if (bjg != null) {
                DataHandler dataHandler = new DataHandler();
                if (dataHandler.getCredits(guildId, id) - 2 * bjg.getBet() > 0) {
                    bjg.split();
                    updateMessage(e.getChannel(), bjg, dataHandler, guildId, id, e.getAuthor().getName());
                } else {
                    e.getChannel().sendMessage("You have not enough credits").queue();
                }

            }
        }
    }
}
