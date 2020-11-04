package commands.casino.uno;

import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import casino.uno.UnoGame;
import utils.DataHandler;
import utils.Utils;

public class Uno extends Command {

    private GameHandler gameHandler;

    public Uno(GameHandler gameHandler) {
        this.name = "uno";
        this.aliases = new String[]{"playuno"};
        this.category = "Casino";
        this.arguments = "[<bet>]";
        this.description = "Start a game of Uno";
        this.gameHandler = gameHandler;
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        if (gameHandler.getUnoGame() != null) {
            e.getChannel().sendMessage("A game has already started").queue();
            return;
        }
        int bet = 0;
        if (args.length == 1) {
            bet = Utils.getInt(args[0]);
            if (bet >= 100) {
                if (!(new DataHandler().getCredits(e.getGuild().getIdLong(), e.getAuthor().getIdLong()) - bet >= 0)) {
                    e.getChannel().sendMessage(String.format("You don't have enough credits to make a %d credits bet", bet)).queue();
                    return;
                }
            } else {
                e.getChannel().sendMessage("You need to place a bet for at least 10 credits").queue();
                return;
            }
        } else if (args.length > 1) {
            e.getChannel().sendMessage("You need to place a valid bet").queue();
            return;
        }
        UnoGame unogame = new UnoGame(bet, e.getAuthor().getIdLong(), e.getChannel().getIdLong());
        gameHandler.setUnoGame(unogame);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("A game of uno is going to start!");
        if (bet != 0)
            eb.setDescription(String.format("This game requires a %d credits bet.\nThe winner receives the sum of all bets", bet));
        eb.addField("Players", "No Players yet", false);
        eb.setFooter("React with \uD83D\uDD90️ to join, ▶️ to start and ❌ to cancel the game");
        e.getChannel().sendMessage(eb.build()).queue(m -> {
            unogame.setMessageID(m.getIdLong());
            m.addReaction("\uD83D\uDD90️").queue();
            m.addReaction("▶️").queue();
            m.addReaction("❌").queue();
        });
    }
}
