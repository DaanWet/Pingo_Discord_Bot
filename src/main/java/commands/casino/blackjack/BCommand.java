package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.DataHandler;

public abstract class BCommand extends Command {

    protected GameHandler gameHandler;

    public BCommand(GameHandler gameHandler){
        this.gameHandler = gameHandler;
        this.category = "Casino";
        this.hidden = true;
    }

    protected void updateMessage(TextChannel channel, BlackJackGame bjg, DataHandler dataHandler, long guildId, long id, String author){
        channel.retrieveMessageById(bjg.getMessageId()).queue(m -> {
            EmbedBuilder eb = bjg.buildEmbed(author);
            if (bjg.hasEnded()){
                int won_lose = bjg.getWonCreds();
                int credits = dataHandler.addCredits(guildId, id, won_lose);
                eb.addField("Credits", String.format("You now have %d credits", credits), false);
                gameHandler.removeBlackJackGame(guildId, id);
                updateRecords(guildId, id, dataHandler, won_lose, m.getJumpUrl());
            }
            m.editMessage(eb.build()).queue();
        });
    }

    protected void updateRecords(long guildId, long playerId, DataHandler dataHandler, int won_lose, String jumpurl){
        dataHandler.setRecord(guildId, playerId, won_lose > 0 ? "biggest_bj_win" : "biggest_bj_lose", won_lose > 0 ? won_lose : won_lose * -1, jumpurl, false);
        Pair<Double, String> played_games = dataHandler.getRecord(guildId, playerId, "bj_games_played");
        Pair<Double, String> winrate = dataHandler.getRecord(guildId, playerId, "bj_win_rate");
        int temp = played_games == null ? 0 : played_games.getLeft().intValue();
        double tempw = winrate == null ? 0.0 : winrate.getLeft();
        dataHandler.setRecord(guildId, playerId, "bj_games_played", temp + 1, false);
        dataHandler.setRecord(guildId, playerId, "bj_win_rate", tempw + (((won_lose > 0 ? 1.0 : won_lose == 0 ? 0.5 : 0.0) - tempw) / (temp + 1.0)), true);
        int streak = dataHandler.getStreak(guildId, playerId);
        int newstreak = 0;
        if (won_lose > 0){
            newstreak = streak < 0 ? 1 : streak + 1;
        } else if (won_lose < 0){
            newstreak = streak > 0 ? -1 : streak - 1;
        }
        dataHandler.setStreak(guildId, playerId, newstreak, jumpurl);
    }




}
