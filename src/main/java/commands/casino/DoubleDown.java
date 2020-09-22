package commands.casino;

import blackjack.BlackJackGame;
import blackjack.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

import javax.xml.crypto.Data;

public class DoubleDown extends Command {

    private GameHandler gameHandler;
    private DataHandler dataHandler;

    public DoubleDown(GameHandler gameHandler){
        this.gameHandler = gameHandler;
        this.name = "double";
        this.category = "hidden";
        dataHandler = new DataHandler();

    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 0) {
            BlackJackGame bjg = gameHandler.getBlackJackGame(e.getAuthor().getIdLong());
            if (bjg != null) {
                if (bjg.canDouble()){
                    if (new DataHandler().getCredits(e.getAuthor().getId()) - 2*bjg.getBet() >= 0){
                        bjg.doubleDown();
                        e.getChannel().retrieveMessageById(bjg.getMessageId()).queue(m -> {
                            EmbedBuilder eb = bjg.buildEmbed(e.getAuthor().getName());
                            if (bjg.hasEnded()) {
                                int won_lose = bjg.getWonCreds();
                                int credits = new DataHandler().addCredits(e.getAuthor().getId(), won_lose);
                                eb.addField("Credits", String.format("You now have %d credits", credits), false);
                                gameHandler.removeBlackJackGame(e.getAuthor().getIdLong());
                                dataHandler.setRecord(e.getAuthor().getId(), won_lose > 0 ? "biggest_bj_win" : "biggest_bj_lose", won_lose > 0 ? won_lose : won_lose * -1, m.getJumpUrl());
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

    @Override
    public String getDescription() {
        return null;
    }
}
