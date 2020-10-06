package commands.casino.uno;

import blackjack.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import uno.UnoCard;
import uno.UnoGame;
import uno.UnoHand;
import utils.DataHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Play extends Command {

    private GameHandler gameHandler;
    private DataHandler dataHandler;

    public Play(GameHandler gameHandler) {
        this.name = "play";
        this.aliases = new String[]{"p"};
        this.category = "hidden";
        this.gameHandler = gameHandler;
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
                    for (UnoHand hand : hands) {
                        long player = hand.getPlayerId();
                        TextChannel channel = guild.getTextChannelById(hand.getChannelId());
                        if (player != e.getMember().getIdLong()) {
                            channel.retrieveMessageById(hand.getMessageId()).queue(message -> {
                                EmbedBuilder eb = unoGame.createEmbed(player);
                                eb.setColor(color);
                                message.editMessage(eb.build()).queue();
                                if (unoGame.isFinished()) {
                                    EmbedBuilder eb2 = new EmbedBuilder();
                                    int credits = unoGame.getBet() == 0 ? 100 * hands.size() : unoGame.getBet() * hands.size();
                                    eb2.setTitle(String.format("%s played a **%s** and won, he/she won **%d** credits", e.getMember().getEffectiveName(), card.toString(), credits));
                                    eb2.setColor(color);
                                    channel.sendMessage(eb2.build()).queue();
                                    channel.delete().queueAfter(1, TimeUnit.MINUTES);
                                } else if (hands.get(newturn).getPlayerId() == player) {
                                    EmbedBuilder eb2 = new EmbedBuilder();
                                    eb2.setTitle("It's your turn!");
                                    eb2.setColor(color);
                                    channel.sendMessage(eb2.build()).queue();
                                }
                            });
                        } else {
                            if (!unoGame.isFinished()) {
                                EmbedBuilder eb = unoGame.createEmbed(player);
                                eb.setColor(guild.getSelfMember().getColor());
                                channel.sendMessage(eb.build()).queue(newmessage -> hand.setMessageId(newmessage.getIdLong()));
                            } else {
                                EmbedBuilder eb2 = new EmbedBuilder();
                                int credits = unoGame.getBet() == 0 ? 100 * hands.size() : unoGame.getBet() * hands.size();
                                eb2.setTitle(String.format("You played a **%s** and won, you won **%d** credits", card.toString(), credits));
                                dataHandler.addCredits(player + "", credits);
                                channel.sendMessage(eb2.build()).queue();
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


    @Override
    public String getDescription() {
        return "Play a card in a unogame";
    }
}
