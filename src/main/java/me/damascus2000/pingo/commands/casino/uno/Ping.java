package me.damascus2000.pingo.commands.casino.uno;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.uno.UnoGame;
import me.damascus2000.pingo.companions.uno.UnoHand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

public class Ping extends Command {

    private final GameCompanion gameCompanion;

    public Ping(GameCompanion gameCompanion){
        this.name = "ping";
        this.aliases = new String[]{"pi", "remind"};
        this.category = Category.UNO;
        this.arguments = new String[]{};
        this.gameCompanion = gameCompanion;
        this.description = "uno.ping.description";
        this.hidden = true;
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        UnoGame unoGame = gameCompanion.getUnoGame(e.getGuild().getIdLong());
        if (unoGame != null && unoGame.getHands().stream().map(UnoHand::getChannelId).collect(Collectors.toList()).contains(e.getChannel().getIdLong())){
            int turn = unoGame.getTurn();
            Guild guild = e.getGuild();
            MyResourceBundle language = Utils.getLanguage(guild.getIdLong());
            if (unoGame.isFinished())
                throw new MessageException(language.getString("uno.ended"));
            UnoHand hand = unoGame.getHands().get(turn);
            if (hand.getLastPingOrPlay().isAfter(LocalDateTime.now().plusHours(12))){
                LocalDateTime till = hand.getLastPingOrPlay().plusHours(12);
                LocalDateTime temp = LocalDateTime.now();
                long hours = temp.until(till, ChronoUnit.HOURS) + 1;
                throw new MessageException(language.getString("uno.ping.error", hours));
            }
            TextChannel channel = guild.getTextChannelById(hand.getChannelId());
            Color color = guild.getSelfMember().getColor();
            EmbedBuilder eb2 = new EmbedBuilder();
            eb2.setTitle( language.getString("uno.turn"));
            eb2.setColor(color);
            channel.sendMessageEmbeds(eb2.build()).append(e.getGuild().getMemberById(hand.getPlayerId()).getAsMention()).queue();
        }
    }
}
