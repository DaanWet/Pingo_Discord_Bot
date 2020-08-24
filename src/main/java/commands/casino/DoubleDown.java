package commands.casino;

import blackjack.BlackJackGame;
import blackjack.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

public class DoubleDown extends Command {

    private GameHandler gameHandler;

    public DoubleDown(GameHandler gameHandler){
        this.gameHandler = gameHandler;
        this.name = "double";
        this.category = "hidden";

    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 0) {
            BlackJackGame bjg = gameHandler.getBlackJackGame(e.getAuthor().getIdLong());
            if (bjg != null) {
                if (bjg.canDouble()){
                    if (new DataHandler().getCredits(e.getAuthor().getId()) - 2*bjg.getBet() > 0){
                        bjg.doubleDown();
                        int credits = new DataHandler().addCredits(e.getAuthor().getId(), ((Double) (bjg.getBet() * bjg.getEndstate().getReward())).intValue());
                        gameHandler.removeBlackJackGame(e.getAuthor().getIdLong());
                        EmbedBuilder eb = bjg.buildEmbed(e.getAuthor().getName());
                        eb.addField("Credits", String.format("You now have %d credits", credits), false);
                        e.getChannel().retrieveMessageById(bjg.getMessageId()).queue(m -> {
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
