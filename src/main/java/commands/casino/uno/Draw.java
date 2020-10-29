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
import utils.ImageHandler;
import utils.Utils;


import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Draw extends Command {

    private GameHandler gameHandler;

    public Draw(GameHandler gameHandler) {
        this.name = "Draw";
        this.aliases = new String[]{"d"};
        this.category = "Uno";
        this.gameHandler = gameHandler;
        this.description = "Draw a card from the deck";
        this.hidden = true;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        UnoGame unoGame = gameHandler.getUnoGame();
        if (unoGame != null && unoGame.getHands().stream().map(UnoHand::getChannelId).collect(Collectors.toList()).contains(e.getChannel().getIdLong())) {
            Guild guild = e.getGuild();
            int turn = unoGame.getTurn();
            ArrayList<UnoHand> hands = unoGame.getHands();
            if (unoGame.isFinished()) {
                e.getChannel().sendMessage("The game has already ended").queue();
                return;
            }
            if (turn != -1 && hands.get(turn).getPlayerId() == e.getMember().getIdLong()) {
                UnoCard newCard = unoGame.drawCard();
                EmbedBuilder deb = new EmbedBuilder();
                Color color = guild.getSelfMember().getColor();
                deb.setColor(color);
                boolean played = false;
                if (unoGame.canPlay(newCard)) {
                    if (newCard.getValue() != UnoCard.Value.PLUSFOUR && newCard.getValue() != UnoCard.Value.WILD) {
                        unoGame.playCard(newCard);
                        played = true;
                        deb.setTitle(String.format("You drew and played a %s", newCard.toString()));
                    } else {
                        UnoHand hand = unoGame.getPlayerHand(e.getMember().getIdLong());
                        TextChannel channel = guild.getTextChannelById(hand.getChannelId());
                        deb.setTitle(String.format("You drew a %s", newCard.toString()));
                        channel.sendMessage(deb.build()).queue();
                        return;
                    }
                } else {
                    hands.get(turn).endTurn(null);
                    unoGame.nextTurn(false);
                    deb.setTitle(String.format("You drew a %s", newCard.toString()));
                }
                int newturn = unoGame.getTurn();
                for (int i = 0; i < hands.size(); i++) {
                    UnoHand hand = hands.get(i);
                    TextChannel channel = guild.getTextChannelById(hand.getChannelId());
                    long player = hand.getPlayerId();
                    if (player != e.getMember().getIdLong()) {
                        if (played && Utils.isBetween(unoGame, turn, i) && (newCard.getValue() == UnoCard.Value.PLUSTWO)) {
                            EmbedBuilder eb = unoGame.createEmbed(player);
                            eb.setColor(guild.getSelfMember().getColor());
                            EmbedBuilder eb2 = new EmbedBuilder();
                            eb2.setColor(color);
                            eb2.setTitle(String.format("You had to draw 2 cards because %s played a %s", hands.get(turn).getPlayerName(), newCard.toString()));
                            channel.sendMessage(eb2.build()).queue();
                            channel.sendFile(ImageHandler.getCardsImage(hand.getCards()), "hand.png").embed(eb.build()).queueAfter(1, TimeUnit.SECONDS, newmessage -> hand.setMessageId(newmessage.getIdLong()));
                        } else {
                            channel.retrieveMessageById(hand.getMessageId()).queue(message -> {
                                EmbedBuilder eb = unoGame.createEmbed(player);
                                eb.setColor(color);
                                message.editMessage(eb.build()).queue();
                                if (hands.get(newturn).getPlayerId() == player) {
                                    EmbedBuilder eb2 = new EmbedBuilder();
                                    eb2.setTitle("It's your turn!");
                                    eb2.setColor(color);
                                    channel.sendMessage(eb2.build()).queue();
                                }
                            });
                        }

                    } else {
                        channel.sendMessage(deb.build()).queue();
                        EmbedBuilder eb = unoGame.createEmbed(player);
                        eb.setColor(guild.getSelfMember().getColor());
                        channel.sendFile(ImageHandler.getCardsImage(hand.getCards()), "hand.png").embed(eb.build()).queue(newmessage -> hand.setMessageId(newmessage.getIdLong()));

                    }
                }


            } else {
                e.getChannel().sendMessage("It's not your turn yet").queue();
            }
        }
    }
}
