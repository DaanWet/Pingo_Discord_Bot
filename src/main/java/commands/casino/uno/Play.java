package commands.casino.uno;

import commands.Command;
import companions.GameCompanion;
import companions.uno.UnoCard;
import companions.uno.UnoGame;
import companions.uno.UnoHand;
import data.ImageHandler;
import data.handlers.CreditDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyProperties;
import utils.MyResourceBundle;
import utils.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Play extends Command {

    private final GameCompanion gameCompanion;

    public Play(GameCompanion gameCompanion){
        this.name = "play";
        this.aliases = new String[]{"p"};
        this.category = Category.UNO;
        this.arguments = new String[]{"<color><value>"};
        this.gameCompanion = gameCompanion;
        this.description = "uno.play.description";
        this.example = "blueskip";
        this.hidden = true;
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        UnoGame unoGame = gameCompanion.getUnoGame(e.getGuild().getIdLong());
        if (unoGame != null && unoGame.getHands().stream().map(UnoHand::getChannelId).collect(Collectors.toList()).contains(e.getChannel().getIdLong())){
            int turn = unoGame.getTurn();
            ArrayList<UnoHand> hands = unoGame.getHands();
            Guild guild = e.getGuild();
            MyResourceBundle language = Utils.getLanguage(guild.getIdLong());
            if (unoGame.isFinished())
                throw new MessageException(language.getString("uno.ended"));

            if (turn == -1 || hands.get(turn).getPlayerId() != e.getAuthor().getIdLong())
                throw new MessageException(language.getString("uno.turn"));

            if (args.length == 0)
                throw new MessageException(getUsage(guild.getIdLong()));

            UnoCard card = UnoCard.fromString(args[0]);
            if (card == null || !unoGame.canPlay(card))
                throw new MessageException(language.getString("uno.play.error"));


            unoGame.playCard(card);
            Color color = guild.getSelfMember().getColor();
            int newturn = unoGame.getTurn();
            MyProperties config = Utils.config;
            for (int i = 0; i < hands.size(); i++){
                UnoHand hand = hands.get(i);
                long player = hand.getPlayerId();
                TextChannel channel = guild.getTextChannelById(hand.getChannelId());
                CreditDataHandler dataHandler = new CreditDataHandler();
                if (player != e.getMember().getIdLong()){
                    final int finalI = i; // values inside message Consumer lambdas need to be final, so we need to construct a final variant of i
                    channel.retrieveMessageById(hand.getMessageId()).queue(message -> {
                        EmbedBuilder eb = unoGame.createEmbed(player, language);
                        eb.setColor(color);
                        if (unoGame.isFinished()){
                            message.editMessageEmbeds(eb.build()).queue();
                            EmbedBuilder eb2 = new EmbedBuilder();
                            int size = hands.size() - 1;
                            int bet = unoGame.getBet();
                            int credits = bet == 0 ? (int) config.get("uno.win") * size : bet * size;
                            eb2.setTitle(language.getString("uno.win", e.getMember().getEffectiveName(), card, credits));
                            if (bet != 0){
                                eb2.setDescription(language.getString("uno.lost", bet));
                            }

                            if (bet != 0) dataHandler.addCredits(e.getGuild().getIdLong(), player, -1 * credits);
                            eb2.setColor(color);
                            channel.sendMessageEmbeds(eb2.build()).queue();
                            channel.delete().queueAfter((int) config.get("uno.timeout"), TimeUnit.MINUTES);
                        } else if (newturn == finalI){
                            message.editMessageEmbeds(eb.build()).queue();
                            EmbedBuilder eb2 = new EmbedBuilder();
                            eb2.setTitle(language.getString("uno.turn"));
                            eb2.setColor(color);
                            channel.sendMessageEmbeds(eb2.build()).queue();
                        } else if (Utils.isBetween(unoGame, turn, finalI) && (card.getValue() == UnoCard.Value.PLUSFOUR || card.getValue() == UnoCard.Value.PLUSTWO)){
                            EmbedBuilder eb2 = new EmbedBuilder();
                            eb2.setColor(color);
                            eb2.setTitle(language.getString("uno.draw", card.getValue() == UnoCard.Value.PLUSTWO ? 2 : 4, hands.get(turn).getPlayerName(), card));
                            channel.sendMessageEmbeds(eb2.build()).queue();
                            channel.sendFile(ImageHandler.getCardsImage(hand.getCards()), "hand.png").setEmbeds(eb.build()).queueAfter(1, TimeUnit.SECONDS, newmessage -> hand.setMessageId(newmessage.getIdLong()));
                        } else {
                            message.editMessageEmbeds(eb.build()).queue();
                        }
                    });
                } else {
                    if (!unoGame.isFinished()){
                        EmbedBuilder eb = unoGame.createEmbed(player, language);
                        eb.setColor(guild.getSelfMember().getColor());
                        channel.sendFile(ImageHandler.getCardsImage(hand.getCards()), "hand.png").setEmbeds(eb.build()).queue(newmessage -> hand.setMessageId(newmessage.getIdLong()));
                    } else {
                        EmbedBuilder eb2 = new EmbedBuilder();
                        int size = hands.size() - 1;
                        int credits = unoGame.getBet() == 0 ? 200 * size : unoGame.getBet() * size;
                        eb2.setTitle(language.getString("uno.win.you", card, credits));
                        dataHandler.addCredits(guild.getIdLong(), player, credits);
                        channel.sendMessageEmbeds(eb2.build()).queue();
                        guild.getTextChannelById(unoGame.getChannelID()).retrieveMessageById(unoGame.getMessageID()).queue(m -> {
                            EmbedBuilder eb = new EmbedBuilder(m.getEmbeds().get(0));
                            eb.setTitle(language.getString("uno.end"));
                            eb.setDescription(language.getString("uno.win.short", hand.getPlayerName(), credits));
                            m.editMessageEmbeds(eb.build()).queue();
                        });
                        channel.delete().queueAfter((int) config.get("uno.timeout"), TimeUnit.MINUTES);

                    }
                }
            }
            if (unoGame.isFinished()){
                guild.getCategoryById(unoGame.getCategory()).delete().queueAfter((int) config.get("uno.timeout") * 60 + 5, TimeUnit.SECONDS);
                gameCompanion.removeUnoGame(guild.getIdLong());
            }
        }
    }
}
