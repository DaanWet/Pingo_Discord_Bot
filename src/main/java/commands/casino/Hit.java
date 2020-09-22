package commands.casino;

import blackjack.BlackJackGame;
import blackjack.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
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
                        int won_lose = bjg.getWonCreds();
                        int credits = dataHandler.addCredits(e.getAuthor().getId(), won_lose);
                        eb.addField("Credits", String.format("You now have %d credits", credits), false);
                        gameHandler.removeBlackJackGame(e.getAuthor().getIdLong());
                        dataHandler.setRecord(e.getAuthor().getId(), won_lose > 0 ? "biggest_bj_win" : "biggest_bj_lose", won_lose > 0 ? won_lose : won_lose * -1, m.getJumpUrl());

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
