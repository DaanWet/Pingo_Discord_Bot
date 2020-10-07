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

    public BlackJack(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.dataHandler = new DataHandler();
        this.name = "blackjack";
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
                            int won_lose = bjg.getWonCreds();
                            dataHandler.setRecord(author.getId(), won_lose > 0 ? "biggest_bj_win" : "biggest_bj_lose", won_lose > 0 ? won_lose : won_lose * -1, m.getJumpUrl());
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
