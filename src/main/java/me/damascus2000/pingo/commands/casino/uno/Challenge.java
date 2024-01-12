package me.damascus2000.pingo.commands.casino.uno;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.uno.UnoCard;
import me.damascus2000.pingo.companions.uno.UnoGame;
import me.damascus2000.pingo.companions.uno.UnoHand;
import me.damascus2000.pingo.data.ImageHandler;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class Challenge extends Command {

    private final GameCompanion gameCompanion;

    public Challenge(GameCompanion gameCompanion){
        this.name = "challenge";
        this.category = Category.UNO;
        this.gameCompanion = gameCompanion;
        this.hidden = true;
        this.description = "uno.challenge.description";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        Guild guild = e.getGuild();
        UnoGame unoGame = gameCompanion.getUnoGame(guild.getIdLong());
        if (unoGame != null && unoGame.getHands().stream().map(UnoHand::getChannelId).collect(Collectors.toList()).contains(e.getChannel().getIdLong())){
            ArrayList<UnoHand> hands = unoGame.getHands();
            MyResourceBundle language = Utils.getLanguage(guild.getIdLong());
            if (unoGame.isFinished()){
                throw new MessageException(language.getString("uno.error.ended"));
            }
            UnoHand skippedHand = hands.get(unoGame.calculateNextTurn(-1));
            if (unoGame.getTopCard().getValue() != UnoCard.Value.PLUSFOUR || skippedHand.getPlayerId() != e.getAuthor().getIdLong())
                throw new MessageException(language.getString("uno.challenge.error"));

            int playedturn = unoGame.calculateNextTurn(-2);
            UnoHand playedHand = hands.get(playedturn);
            EmbedBuilder eb1 = new EmbedBuilder();
            EmbedBuilder eb2 = new EmbedBuilder();
            Color color = guild.getSelfMember().getColor();
            eb1.setColor(color);
            eb2.setColor(color);
            if (unoGame.canPlayDrawFour(playedturn)){
                UnoCard card1 = unoGame.getNextCard();
                UnoCard card2 = unoGame.getNextCard();
                skippedHand.addCard(card1, false);
                skippedHand.addCard(card2, false);
                eb1.setTitle(language.getString("uno.challenge.wrong.sender", playedHand.getPlayerName(), card1.toString(), card2.toString()));
                eb2.setTitle(language.getString("uno.challenge.wrong.receiver", skippedHand.getPlayerName()));
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 4; i++){
                    unoGame.getDrawPile().add(skippedHand.getCards().remove(skippedHand.getCards().size() - 1));
                    UnoCard card = unoGame.getDrawPile().remove(0);
                    playedHand.addCard(card, false);
                    sb.append(card.toString()).append(", ");
                }
                eb1.setTitle(language.getString("uno.challenge.correct.sender", playedHand.getPlayerName()));
                eb2.setTitle(language.getString("uno.challenge.correct.receiver", skippedHand.getPlayerName(), sb.substring(0, sb.length() - 3)));
            }
            TextChannel skippedchannel = guild.getTextChannelById(skippedHand.getChannelId());
            TextChannel playedChannel = guild.getTextChannelById(playedHand.getChannelId());
            skippedchannel.sendMessageEmbeds(eb1.build()).queue();
            playedChannel.sendMessageEmbeds(eb2.build()).queue();
            EmbedBuilder eb = unoGame.createEmbed(skippedHand.getPlayerId(), language);
            eb.setColor(color);
            skippedchannel.sendFile(ImageHandler.getCardsImage(skippedHand.getCards()), "hand.png").setEmbeds(eb.build()).queue(newmessage -> skippedHand.setMessageId(newmessage.getIdLong()));
            eb = unoGame.createEmbed(playedHand.getPlayerId(), language);
            playedChannel.sendFile(ImageHandler.getCardsImage(playedHand.getCards()), "hand.png").setEmbeds(eb.build()).queue(newmessage -> playedHand.setMessageId(newmessage.getIdLong()));

        }
    }
}