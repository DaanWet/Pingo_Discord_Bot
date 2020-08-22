package commands.casino;

import blackjack.BlackJackGame;
import blackjack.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

public class Stand extends Command {

    private GameHandler gameHandler;
    private DataHandler dataHandler;

    public Stand(GameHandler gameHandler){
        this.name = "Stand";
        this.gameHandler = gameHandler;
        this.dataHandler = new DataHandler();
        this.category = "hidden";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 0){
            BlackJackGame bjg = gameHandler.getBlackJackGame(e.getAuthor().getIdLong());
            if (bjg != null){
                bjg.stand();
                int credits = dataHandler.addCredits(e.getAuthor().getId(), ((Double) (bjg.getBet() * bjg.getEndstate().getReward())).intValue());
                gameHandler.removeBlackJackGame(e.getAuthor().getIdLong());
                EmbedBuilder eb = bjg.buildEmbed(e.getAuthor().getName());
                eb.addField("Credits", String.format("You now have %d credits", credits), false);
                e.getChannel().retrieveMessageById(bjg.getMessageId()).queue(m -> {
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
