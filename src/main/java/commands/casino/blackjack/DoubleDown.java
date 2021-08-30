package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.DataHandler;

public class DoubleDown extends Command {

    private GameHandler gameHandler;

    public DoubleDown(GameHandler gameHandler){
        this.gameHandler = gameHandler;
        this.name = "double";
        this.category = "Blackjack";
        this.hidden = true;

    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        if (args.length == 0) {
            BlackJackGame bjg = gameHandler.getBlackJackGame(guildId, id);
            if (bjg != null) {
                if (bjg.canDouble()){
                    DataHandler dataHandler = new DataHandler();
                    if (new DataHandler().getCredits(guildId, id) - 2*bjg.getBet() >= 0){
                        bjg.doubleDown();
                        e.getChannel().retrieveMessageById(bjg.getMessageId()).queue(m -> {
                            EmbedBuilder eb = bjg.buildEmbed(e.getAuthor().getName());
                            if (bjg.hasEnded()) {
                                int won_lose = bjg.getWonCreds();
                                int credits = dataHandler.addCredits(guildId, id, won_lose);
                                eb.addField("Credits", String.format("You now have %d credits", credits), false);
                                gameHandler.removeBlackJackGame(guildId, id);
                                dataHandler.setRecord(guildId, id, won_lose > 0 ? "biggest_bj_win" : "biggest_bj_lose", won_lose > 0 ? won_lose : won_lose * -1, m.getJumpUrl(), false);
                                Pair<Double, String> played_games = dataHandler.getRecord(guildId, id, "bj_games_played");
                                Pair<Double, String> winrate = dataHandler.getRecord(guildId, id, "bj_win_rate");
                                int temp = played_games == null ? 0 : played_games.getLeft().intValue();
                                double tempw = winrate == null ? 0.0 : winrate.getLeft();
                                dataHandler.setRecord(guildId, id, "bj_games_played", temp + 1, false);
                                dataHandler.setRecord(guildId, id, "bj_win_rate", tempw + (((won_lose > 0 ? 1.0 : won_lose == 0 ? 0.5 : 0.0) - tempw)/(temp + 1.0)), true);
                                int streak = dataHandler.getStreak(guildId, id);
                                int newstreak = 0;
                                if (won_lose > 0){
                                    newstreak = streak < 0 ? 1 : streak + 1;
                                } else if (won_lose < 0){
                                    newstreak = streak > 0 ? -1 : streak - 1;
                                }
                                dataHandler.setStreak(guildId, id, newstreak, m.getJumpUrl());
                            }
                            m.editMessage(eb.build()).queue();
                        });
                    } else {
                        e.getChannel().sendMessage("You have not enough credits").queue();
                    }
                } else {
                    e.getChannel().sendMessage("You can't do that").queue();
                }
            }
        }
    }
}
