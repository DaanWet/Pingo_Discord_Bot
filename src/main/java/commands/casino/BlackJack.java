package commands.casino;

import blackjack.BlackJackGame;
import blackjack.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;
import utils.Utils;

import java.awt.*;

public class BlackJack extends Command {

    private GameHandler gameHandler;
    private DataHandler dataHandler;
    public BlackJack(GameHandler gameHandler){
        this.gameHandler = gameHandler;
        this.dataHandler = new DataHandler();
        this.name = "blackjack";
        this.aliases = new String[]{"bj", "21"};
        this.category = "Casino";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        User author = e.getAuthor();
        int bet = Utils.getInt(args[0]);
        if (args.length == 1 &&  bet >= 10 && dataHandler.getCredits(author.getId()) - bet >= 0){
            BlackJackGame objg = gameHandler.getBlackJackGame(author.getIdLong());
            if (objg == null){
                BlackJackGame bjg = new BlackJackGame(bet);
                if (!bjg.hasEnded()){
                    gameHandler.putBlackJackGame(author.getIdLong(), bjg);
                } else {
                    dataHandler.addCredits(author.getId(), ((Double) (bjg.getBet() * bjg.getEndstate().getReward())).intValue());
                }
                e.getChannel().sendMessage(bjg.buildEmbed(author.getName())).queue(m -> bjg.setMessageId(m.getIdLong()));
            } else {
                e.getChannel().sendMessage("You're already playing a game").queue();
            }
        }

    }

    @Override
    public String getDescription() {
        return "Start a blackjack game";
    }
}
