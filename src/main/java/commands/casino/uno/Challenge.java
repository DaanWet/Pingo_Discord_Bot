package commands.casino.uno;

import casino.GameHandler;
import casino.uno.UnoCard;
import casino.uno.UnoGame;
import casino.uno.UnoHand;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.ImageHandler;
import utils.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Challenge extends Command {

    private GameHandler gameHandler;

    public Challenge(GameHandler gameHandler) {
        this.name = "challenge";
        this.category = "Uno";
        this.gameHandler = gameHandler;
        this.hidden = true;
        this.description = "Challenge the person who just played a +4 on you";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        UnoGame unoGame = gameHandler.getUnoGame();
        if (unoGame != null && unoGame.getHands().stream().map(UnoHand::getChannelId).collect(Collectors.toList()).contains(e.getChannel().getIdLong())) {
            Guild guild = e.getGuild();
            ArrayList<UnoHand> hands = unoGame.getHands();
            if (unoGame.isFinished()) {
                e.getChannel().sendMessage("The game has already ended").queue();
                return;
            }
            System.out.printf("Current turn: %d\n", unoGame.getTurn());
            UnoHand skippedHand = hands.get(unoGame.calculateNextTurn(-1));
            System.out.printf("Skipped: %s, turn: %d\n", skippedHand.getPlayerName(), unoGame.calculateNextTurn(-1));
            if (unoGame.getTopCard().getValue() == UnoCard.Value.PLUSFOUR && skippedHand.getPlayerId() == e.getAuthor().getIdLong()) {
                int playedturn = unoGame.calculateNextTurn(-2);
                UnoHand playedHand = hands.get(playedturn);
                System.out.printf("Played: %s, turn: %d\n", playedHand.getPlayerName(), playedturn);
                EmbedBuilder eb1 = new EmbedBuilder();
                EmbedBuilder eb2 = new EmbedBuilder();
                Color color = guild.getSelfMember().getColor();
                eb1.setColor(color);
                eb2.setColor(color);
                if (unoGame.canPlayDrawFour(playedturn)) {
                    UnoCard card1 = unoGame.getNextCard();
                    UnoCard card2 = unoGame.getNextCard();
                    skippedHand.addCard(card1, false);
                    skippedHand.addCard(card2, false);
                    eb1.setTitle(String.format("You challenged %s, but you were wrong, you drew a %s and a %s", playedHand.getPlayerName(), card1.toString(), card2.toString()));
                    eb2.setTitle(String.format("%s challenged you but was wrong, he drew 2 cards", skippedHand.getPlayerName()));
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 4; i++) {
                        unoGame.getTrekstapel().add(skippedHand.getCards().remove(skippedHand.getCards().size() - 1));
                        UnoCard card = unoGame.getTrekstapel().remove(0);
                        playedHand.addCard(card, false);
                        sb.append(card.toString()).append(", ");
                    }
                    eb1.setTitle(String.format("You were right, %s drew 4 cards", playedHand.getPlayerName()));
                    eb2.setTitle(String.format("%s challenged you and was right, you drew 4 cards: %s", skippedHand.getPlayerName(), sb.substring(0, sb.length() - 3)));
                }
                TextChannel skippedchannel = guild.getTextChannelById(skippedHand.getChannelId());
                TextChannel playedChannel = guild.getTextChannelById(playedHand.getChannelId());
                skippedchannel.sendMessage(eb1.build()).queue();
                playedChannel.sendMessage(eb2.build()).queue();
                EmbedBuilder eb = unoGame.createEmbed(skippedHand.getPlayerId());
                eb.setColor(color);
                skippedchannel.sendFile(ImageHandler.getCardsImage(skippedHand.getCards()), "hand.png").embed(eb.build()).queue(newmessage -> skippedHand.setMessageId(newmessage.getIdLong()));
                eb = unoGame.createEmbed(playedHand.getPlayerId());
                playedChannel.sendFile(ImageHandler.getCardsImage(playedHand.getCards()), "hand.png").embed(eb.build()).queue(newmessage -> playedHand.setMessageId(newmessage.getIdLong()));
            } else {
                e.getChannel().sendMessage("You can only challenge draw four cards when you need to draw").queue();
            }
        }


    }
}
