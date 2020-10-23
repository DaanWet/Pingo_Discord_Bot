package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.DataHandler;

public class Stand extends Command {

    private GameHandler gameHandler;
    private DataHandler dataHandler;

    public Stand(GameHandler gameHandler) {
        this.name = "Stand";
        this.gameHandler = gameHandler;
        this.dataHandler = new DataHandler();
        this.category = "Blackjack";
        this.hidden = true;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 0) {
            BlackJackGame bjg = gameHandler.getBlackJackGame(e.getAuthor().getIdLong());
            if (bjg != null) {
                bjg.stand();
                e.getChannel().retrieveMessageById(bjg.getMessageId()).queue(m -> {
                    EmbedBuilder eb = bjg.buildEmbed(e.getAuthor().getName());
                    if (bjg.hasEnded()) {
                        int won_lose = bjg.getWonCreds();
                        User member = e.getAuthor();
                        String id = member.getId();
                        int credits = dataHandler.addCredits(id, won_lose);
                        eb.addField("Credits", String.format("You now have %d credits", credits), false);
                        gameHandler.removeBlackJackGame(member.getIdLong());
                        dataHandler.setRecord(id, won_lose > 0 ? "biggest_bj_win" : "biggest_bj_lose", won_lose > 0 ? won_lose : won_lose * -1, m.getJumpUrl(), false);
                        Pair<Comparable, String> played_games = dataHandler.getRecord(id, "bj_games_played");
                        Pair<Comparable, String> winrate = dataHandler.getRecord(id, "bj_win_rate");
                        int temp = played_games == null ? 0 : (int) (long) played_games.getLeft();
                        double tempw = winrate == null ? 0.0 : (double) winrate.getLeft();
                        dataHandler.setRecord(id, "bj_games_played", temp + 1, false);
                        dataHandler.setRecord(id, "bj_win_rate", tempw + (((won_lose > 0 ? 1.0 : won_lose == 0 ? 0.5 : 0.0) - tempw) / (temp + 1.0)), true);
                    }
                    m.editMessage(eb.build()).queue();
                });
            }
        }
    }
}
