package commands.casino;

import blackjack.BlackJackGame;
import blackjack.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

public class Split extends Command {

    private DataHandler dataHandler;
    private GameHandler gameHandler;

    public Split(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.name = "split";
        this.category = "hidden";
        dataHandler = new DataHandler();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 0) {
            BlackJackGame bjg = gameHandler.getBlackJackGame(e.getAuthor().getIdLong());
            if (bjg != null) {

                if (dataHandler.getCredits(e.getAuthor().getId()) - 2*bjg.getBet() > 0){
                    bjg.split();
                    e.getChannel().retrieveMessageById(bjg.getMessageId()).queue(m -> {
                        EmbedBuilder eb = bjg.buildEmbed(e.getAuthor().getName());
                        if (bjg.hasEnded()) {
                            int credits = dataHandler.addCredits(e.getAuthor().getId(), bjg.getWonCreds());
                            eb.addField("Credits", String.format("You now have %d credits", credits), false);
                            gameHandler.removeBlackJackGame(e.getAuthor().getIdLong());
                        }
                        m.editMessage(eb.build()).queue();
                    });
                } else {
                    e.getChannel().sendMessage("You have not enough credits").queue();
                }

            }
        }
    }

        @Override
        public String getDescription () {
            return null;
        }
    }
