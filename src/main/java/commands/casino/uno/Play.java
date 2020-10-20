package commands.casino.uno;

import casino.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import casino.uno.UnoCard;
import casino.uno.UnoGame;
import casino.uno.UnoHand;
import utils.DataHandler;
import utils.ImageHandler;
import utils.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Play extends Command {

    private GameHandler gameHandler;
    private DataHandler dataHandler;

    public Play(GameHandler gameHandler) {
        this.name = "play";
        this.aliases = new String[]{"p"};
        this.category = "hidden";
        this.arguments = "<color><value>";
        this.gameHandler = gameHandler;
        this.description = "Play a card from your hand, pick the color for a wildcard immediately";
        dataHandler = new DataHandler();
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        UnoGame unoGame = gameHandler.getUnoGame();
        if (unoGame != null && unoGame.getHands().stream().map(UnoHand::getChannelId).collect(Collectors.toList()).contains(e.getChannel().getIdLong())) {
            int turn = unoGame.getTurn();
            ArrayList<UnoHand> hands = unoGame.getHands();
            if (unoGame.isFinished()) {
                e.getChannel().sendMessage("The game has already ended").queue();
                return;
            }
            if (turn != -1 && hands.get(turn).getPlayerId() == e.getMember().getIdLong()) {
                UnoCard card = UnoCard.fromString(args[0]);
                Guild guild = e.getGuild();
                if (card != null && unoGame.canPlay(card)) {
                    unoGame.playCard(card);
                    Color color = guild.getSelfMember().getColor();
                    int newturn = unoGame.getTurn();
                    for (int i = 0; i < hands.size(); i++) {
                        UnoHand hand = hands.get(i);
                        long player = hand.getPlayerId();
                        TextChannel channel = guild.getTextChannelById(hand.getChannelId());
                        if (player != e.getMember().getIdLong()) {
                            int finalI = i;
                            channel.retrieveMessageById(hand.getMessageId()).queue(message -> {
                                EmbedBuilder eb = unoGame.createEmbed(player);
                                eb.setColor(color);
                                if (unoGame.isFinished()) {
                                    message.editMessage(eb.build()).queue();
                                    EmbedBuilder eb2 = new EmbedBuilder();
                                    int size = hands.size() - 1;
                                    int bet = unoGame.getBet();
                                    int credits = bet == 0 ? 200 * size : bet * size;
                                    eb2.setTitle(String.format("%s played a **%s** and won, he/she won **%d** credits", e.getMember().getEffectiveName(), card.toString(), credits));
                                    if (bet != 0) {
                                        eb2.setDescription(String.format("You lost **%d** credits", bet));
                                    }

                                    if (bet != 0) dataHandler.addCredits(player + "", -1 * credits);
                                    eb2.setColor(color);
                                    channel.sendMessage(eb2.build()).queue();
                                    channel.delete().queueAfter(1, TimeUnit.MINUTES);
                                } else if (newturn == finalI) {
                                    message.editMessage(eb.build()).queue();
                                    EmbedBuilder eb2 = new EmbedBuilder();
                                    eb2.setTitle("It's your turn!");
                                    eb2.setColor(color);
                                    channel.sendMessage(eb2.build()).queue();
                                } else if (Utils.isBetween(unoGame, turn, finalI) && (card.getValue() == UnoCard.Value.PLUSFOUR || card.getValue() == UnoCard.Value.PLUSTWO)) {
                                    EmbedBuilder eb2 = new EmbedBuilder();
                                    eb2.setColor(color);
                                    eb2.setTitle(String.format("You had to draw %d cards because %s played a %s", card.getValue() == UnoCard.Value.PLUSTWO ? 2 : 4, hands.get(turn).getPlayerName(), card.toString()));
                                    channel.sendMessage(eb2.build()).queue();
                                    channel.sendFile(ImageHandler.getCardsImage(hand.getCards()), "hand.png").embed(eb.build()).queueAfter(1, TimeUnit.SECONDS, newmessage -> hand.setMessageId(newmessage.getIdLong()));
                                }
                            });
                        } else {
                            if (!unoGame.isFinished()) {
                                EmbedBuilder eb = unoGame.createEmbed(player);
                                eb.setColor(guild.getSelfMember().getColor());
                                channel.sendFile(ImageHandler.getCardsImage(hand.getCards()), "hand.png").embed(eb.build()).queue(newmessage -> hand.setMessageId(newmessage.getIdLong()));
                            } else {
                                EmbedBuilder eb2 = new EmbedBuilder();
                                int size = hands.size() - 1;
                                int credits = unoGame.getBet() == 0 ? 200 * size : unoGame.getBet() * size;
                                eb2.setTitle(String.format("You played a **%s** and won, you won **%d** credits", card.toString(), credits));
                                dataHandler.addCredits(player + "", credits);
                                channel.sendMessage(eb2.build()).queue();
                                guild.getTextChannelById(unoGame.getChannelID()).retrieveMessageById(unoGame.getMessageID()).queue(m -> {
                                    EmbedBuilder eb = new EmbedBuilder(m.getEmbeds().get(0));
                                    eb.setTitle("The game of uno has concluded");
                                    eb.setDescription(String.format("%s won the game and won **%d** credits", hand.getPlayerName(), credits));
                                    m.editMessage(eb.build()).queue();
                                });
                                channel.delete().queueAfter(1, TimeUnit.MINUTES);

                            }
                        }
                    }
                    if (unoGame.isFinished()) {
                        guild.getCategoryById(unoGame.getCategory()).delete().queueAfter(65, TimeUnit.SECONDS);
                    }
                } else {
                    e.getChannel().sendMessage("You need to play a valid card that's in your hand").queue();
                }
            } else {
                e.getChannel().sendMessage("It's not your turn yet").queue();
            }

        }
    }
}
