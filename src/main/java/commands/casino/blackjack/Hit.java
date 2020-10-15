package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.DataHandler;

public class Hit extends Command {

    private GameHandler gameHandler;
    private DataHandler dataHandler;

    public Hit(GameHandler gameHandler){
        this.name = "Hit";
        this.gameHandler = gameHandler;
        this.category = "hidden";
        this.dataHandler = new DataHandler();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 0) {
            BlackJackGame bjg = gameHandler.getBlackJackGame(e.getAuthor().getIdLong());
            if (bjg != null) {
                bjg.hit();
                e.getChannel().retrieveMessageById(bjg.getMessageId()).queue(m -> {
                    EmbedBuilder eb = bjg.buildEmbed(e.getAuthor().getName());
                    if (bjg.hasEnded()){
                        String id = e.getAuthor().getId();
                        int won_lose = bjg.getWonCreds();
                        int credits = dataHandler.addCredits(id, won_lose);
                        eb.addField("Credits", String.format("You now have %d credits", credits), false);
                        gameHandler.removeBlackJackGame(e.getAuthor().getIdLong());
                        dataHandler.setRecord(id, won_lose > 0 ? "biggest_bj_win" : "biggest_bj_lose", won_lose > 0 ? won_lose : won_lose * -1, m.getJumpUrl());
                        Pair<Comparable, String> played_games = dataHandler.getRecord(id, "bj_games_played");
                        Pair<Comparable, String> winrate = dataHandler.getRecord(id, "bj_win_rate");
                        int temp = played_games == null ? 0 : (int) (long) played_games.getLeft();
                        dataHandler.setRecord(id, "bj_games_played", temp + 1);
                        dataHandler.setRecord(id, "bj_win_rate", ((won_lose > 0 ? 1 : 0) - (winrate == null ? 0.0 : (double) winrate.getLeft()))/(temp + 1));
                    }
                    m.editMessage(eb.build()).queue();
                });
            }
        }
    }

    @Override
    public String getDescription() {
        return "this shouldn't happen";
    }
}