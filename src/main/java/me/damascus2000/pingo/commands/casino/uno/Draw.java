package me.damascus2000.pingo.commands.casino.uno;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.uno.UnoCard;
import me.damascus2000.pingo.companions.uno.UnoGame;
import me.damascus2000.pingo.companions.uno.UnoHand;
import me.damascus2000.pingo.data.ImageHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Draw extends Command {

    private final GameCompanion gameCompanion;

    public Draw(GameCompanion gameCompanion){
        this.name = "draw";
        this.aliases = new String[]{"d"};
        this.category = Category.UNO;
        this.gameCompanion = gameCompanion;
        this.description = "uno.draw.description";
        this.hidden = true;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        Guild guild = e.getGuild();
        UnoGame unoGame = gameCompanion.getUnoGame(guild.getIdLong());
        if (unoGame != null && unoGame.getHands().stream().map(UnoHand::getChannelId).collect(Collectors.toList()).contains(e.getChannel().getIdLong())){
            int turn = unoGame.getTurn();
            ArrayList<UnoHand> hands = unoGame.getHands();
            MyResourceBundle language = Utils.getLanguage(guild.getIdLong());
            if (unoGame.isFinished()){
                throw new MessageException(language.getString("uno.error.ended"));
            }
            if (turn == -1 || hands.get(turn).getPlayerId() != e.getAuthor().getIdLong()){
                throw new MessageException(language.getString("uno.error.turn"));
            }


            UnoCard newCard = unoGame.drawCard();
            EmbedBuilder deb = new EmbedBuilder();
            Color color = guild.getSelfMember().getColor();
            deb.setColor(color);
            boolean played = false;
            if (unoGame.canPlay(newCard)){
                if (newCard.getValue() == UnoCard.Value.PLUSFOUR || newCard.getValue() == UnoCard.Value.WILD){
                    UnoHand hand = hands.get(turn);
                    TextChannel channel = guild.getTextChannelById(hand.getChannelId());
                    deb.setTitle(language.getString("uno.draw.wild", newCard));
                    channel.sendMessageEmbeds(deb.build()).queue();
                    return;
                }
                unoGame.playCard(newCard);
                played = true;
                deb.setTitle(language.getString("uno.draw.played", newCard));
            } else {
                hands.get(turn).endTurn(null);
                unoGame.nextTurn(false);
                deb.setTitle(language.getString("uno.draw.one", newCard.toString()));
            }
            int newturn = unoGame.getTurn();
            for (int i = 0; i < hands.size(); i++){
                UnoHand hand = hands.get(i);
                TextChannel channel = guild.getTextChannelById(hand.getChannelId());
                long player = hand.getPlayerId();
                if (player != e.getMember().getIdLong()){
                    if (played && Utils.isBetween(unoGame, turn, i) && (newCard.getValue() == UnoCard.Value.PLUSTWO)){
                        EmbedBuilder eb = unoGame.createEmbed(player, language);
                        eb.setColor(guild.getSelfMember().getColor());
                        EmbedBuilder eb2 = new EmbedBuilder();
                        eb2.setColor(color);
                        eb2.setTitle(language.getString("uno.draw", hands.get(turn).getPlayerName(), newCard));
                        channel.sendMessageEmbeds(eb2.build()).queue();
                        channel.sendFile(ImageHandler.getCardsImage(hand.getCards()), "hand.png").setEmbeds(eb.build()).queueAfter(1, TimeUnit.SECONDS, newmessage -> hand.setMessageId(newmessage.getIdLong()));
                    } else {
                        channel.retrieveMessageById(hand.getMessageId()).queue(message -> {
                            EmbedBuilder eb = unoGame.createEmbed(player, language);
                            eb.setColor(color);
                            message.editMessageEmbeds(eb.build()).queue();
                            if (hands.get(newturn).getPlayerId() == player){
                                EmbedBuilder eb2 = new EmbedBuilder();
                                eb2.setTitle(language.getString("uno.turn"));
                                eb2.setColor(color);
                                channel.sendMessageEmbeds(eb2.build()).queue();
                            }
                        });
                    }
                } else {
                    channel.sendMessageEmbeds(deb.build()).queue();
                    EmbedBuilder eb = unoGame.createEmbed(player, language);
                    eb.setColor(guild.getSelfMember().getColor());
                    channel.sendFile(ImageHandler.getCardsImage(hand.getCards()), "hand.png").setEmbeds(eb.build()).queue(newmessage -> hand.setMessageId(newmessage.getIdLong()));
                }
            }
        }
    }
}
