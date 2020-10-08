package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.DataHandler;
import utils.Utils;

public class BlackJack extends Command {

    private GameHandler gameHandler;
    private DataHandler dataHandler;

    public BlackJack(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.dataHandler = new DataHandler();
        this.name = "casino";
        this.aliases = new String[]{"bj", "21"};
        this.category = "Casino";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        User author = e.getAuthor();
        int bet = args.length == 0 ? 0 : Utils.getInt(args[0]);
        if (args.length != 0 && args[0].matches("(?i)all(-?in)?")) {
            bet = dataHandler.getCredits(author.getId());
        }
        if (bet >= 10) {
            if (dataHandler.getCredits(author.getId()) - bet >= 0) {
                BlackJackGame objg = gameHandler.getBlackJackGame(author.getIdLong());
                if (objg == null) {
                    BlackJackGame bjg = new BlackJackGame(bet);
                    EmbedBuilder eb = bjg.buildEmbed(author.getName());
                    if (!bjg.hasEnded()) {
                        gameHandler.putBlackJackGame(author.getIdLong(), bjg);
                    } else {
                        int credits = dataHandler.addCredits(author.getId(), bjg.getWonCreds());
                        eb.addField("Credits", String.format("You now have %d credits", credits), false);

                    }
                    e.getChannel().sendMessage(eb.build()).queue(m -> {
                        if (!bjg.hasEnded()) bjg.setMessageId(m.getIdLong());
                        else {
                            String id = author.getId();
                            int won_lose = bjg.getWonCreds();
                            dataHandler.setRecord(id, won_lose > 0 ? "biggest_bj_win" : "biggest_bj_lose", won_lose > 0 ? won_lose : won_lose * -1, m.getJumpUrl());
                            Pair<Comparable, String> played_games = dataHandler.getRecord(id, "bj_games_played");
                            Pair<Comparable, String> winrate = dataHandler.getRecord(id, "bj_win_rate");
                            int temp = played_games == null ? 0 : (int) (long) played_games.getLeft();
                            dataHandler.setRecord(id, "bj_games_played", temp + 1);
                            dataHandler.setRecord(id, "bj_win_rate", ((won_lose > 0 ? 1 : 0) - (winrate == null ? 0.0 : (double) winrate.getLeft()))/(temp + 1));
                        }
                    });
                } else {
                    e.getChannel().sendMessage("You're already playing a game").queue();
                }
            } else {
                e.getChannel().sendMessage(String.format("You don't have enough credits to make a %d credits bet", bet)).queue();
            }
        } else {
            e.getChannel().sendMessage("You need to place a bet for at least 10 credits").queue();
        }
    }

    @Override
    public String getDescription() {
        return "Start a blackjack game";
    }
}
