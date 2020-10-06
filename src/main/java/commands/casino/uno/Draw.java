package commands.casino.uno;

import blackjack.GameHandler;
import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import uno.UnoCard;
import uno.UnoGame;
import uno.UnoHand;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Draw extends Command {

    private GameHandler gameHandler;

    public Draw(GameHandler gameHandler) {
        this.name = "Draw";
        this.aliases = new String[]{"d"};
        this.category = "hidden";
        this.gameHandler = gameHandler;
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
                if (unoGame.canPlay(newCard)) {
                    unoGame.playCard(newCard);
                    deb.setTitle(String.format("You drew and played a %s", newCard.toString()));
                } else {
                    unoGame.nextTurn(false);
                    deb.setTitle(String.format("You drew a %s", newCard.toString()));
                }
                deb.setColor(color);
                int newturn = unoGame.getTurn();

                for (UnoHand hand : hands) {
                    TextChannel channel = guild.getTextChannelById(hand.getChannelId());
                    long player = hand.getPlayerId();
                    if (player != e.getMember().getIdLong()) {
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
                    } else {
                        channel.sendMessage(deb.build()).queue();
                        EmbedBuilder eb = unoGame.createEmbed(player);
                        eb.setColor(guild.getSelfMember().getColor());
                        channel.sendMessage(eb.build()).queue(newmessage -> hand.setMessageId(newmessage.getIdLong()));

                    }
                }
            } else {
                e.getChannel().sendMessage("It's not your turn yet").queue();
            }
        }
    }

    @Override
    public String getDescription() {
        return null;
    }
}
