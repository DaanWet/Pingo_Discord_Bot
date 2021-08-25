package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.DataHandler;
import utils.Utils;

import java.time.LocalDateTime;

public class BlackJack extends Command {

    private GameHandler gameHandler;

    public BlackJack(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.name = "blackjack";
        this.aliases = new String[]{"bj", "21"};
        this.category = "Casino";
        this.arguments = "<bet>";
        this.description = "Start a blackjack game";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        CommandState betting = canBeExecuted(guildId, channelId, member, Setting.BETTING);
        CommandState blackjack = canBeExecuted(guildId, channelId, member, Setting.BLACKJACK);
        return betting.worst(blackjack);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        User author = e.getAuthor();
        long guildId = e.getGuild().getIdLong();
        long playerId = author.getIdLong();
        int bet = args.length == 0 ? 0 : Utils.getInt(args[0]);
        DataHandler dataHandler = new DataHandler();
        if (args.length != 0 && args[0].matches("(?i)all(-?in)?")) {
            bet = dataHandler.getCredits(guildId, playerId);
        }
        if (bet >= 10) {
            if (dataHandler.getCredits(guildId, playerId) - bet >= 0) {
                BlackJackGame objg = gameHandler.getBlackJackGame(playerId);
                if (objg == null) {
                    dataHandler.setCooldown(guildId, playerId, Setting.BLACKJACK, LocalDateTime.now());
                    BlackJackGame bjg = new BlackJackGame(bet);
                    EmbedBuilder eb = bjg.buildEmbed(author.getName());
                    if (!bjg.hasEnded()) {
                        gameHandler.putBlackJackGame(playerId, bjg);
                    } else {
                        int credits = dataHandler.addCredits(guildId, playerId, bjg.getWonCreds());
                        eb.addField("Credits", String.format("You now have %d credits", credits), false);

                    }
                    e.getChannel().sendMessage(eb.build()).queue(m -> {
                        if (!bjg.hasEnded()) bjg.setMessageId(m.getIdLong());
                        else {
                            int won_lose = bjg.getWonCreds();
                            dataHandler.setRecord(guildId, playerId, won_lose > 0 ? "biggest_bj_win" : "biggest_bj_lose", won_lose > 0 ? won_lose : won_lose * -1, m.getJumpUrl(), false);
                            Pair<Double, String> played_games = dataHandler.getRecord(guildId, playerId, "bj_games_played");
                            Pair<Double, String> winrate = dataHandler.getRecord(guildId, playerId, "bj_win_rate");
                            int temp = played_games == null ? 0 : played_games.getLeft().intValue();
                            double tempw = winrate == null ? 0.0 : winrate.getLeft();
                            dataHandler.setRecord(guildId, playerId, "bj_games_played", temp + 1, false);
                            dataHandler.setRecord(guildId, playerId, "bj_win_rate", tempw + (((won_lose > 0 ? 1.0 : won_lose == 0 ? 0.5 : 0.0) - tempw)/(temp + 1.0)), true);
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
}
