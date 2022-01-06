package commands.casino.uno;

import commands.Command;
import commands.settings.Setting;
import companions.GameHandler;
import companions.uno.UnoGame;
import data.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.Utils;

import java.time.LocalDateTime;

public class Uno extends Command {

    private final GameHandler gameHandler;

    public Uno(GameHandler gameHandler){
        this.name = "uno";
        this.aliases = new String[]{"playuno"};
        this.category = "Casino";
        this.arguments = "[<bet>]";
        this.description = "Start a game of Uno";
        this.gameHandler = gameHandler;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (gameHandler.getUnoGame(e.getGuild().getIdLong()) != null){
            throw new MessageException("A game has already started");
        }
        int bet = 0;
        if (args.length > 1)
            throw new MessageException("You need to place a valid bet");
        DataHandler dataHandler = new DataHandler();
        if (args.length == 1){
            if (!dataHandler.getBoolSetting(e.getGuild().getIdLong(), Setting.BETTING)){
                e.getChannel().sendMessage("Betting is currently disabled in this server, starting a game without credits").queue();
            } else {
                bet = Utils.getInt(args[0]);
                if (bet < 10)
                    throw new MessageException("You need to place a bet for at least 10 credits");
                if (dataHandler.getCredits(e.getGuild().getIdLong(), e.getAuthor().getIdLong()) < bet)
                    throw new MessageException(String.format("You don't have enough credits to make a %d credits bet", bet));
            }
        }
        dataHandler.setCooldown(e.getGuild().getIdLong(), e.getAuthor().getIdLong(), Setting.UNO, LocalDateTime.now());
        UnoGame unogame = new UnoGame(bet, e.getAuthor().getIdLong(), e.getChannel().getIdLong());
        gameHandler.setUnoGame(e.getGuild().getIdLong(), unogame);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("A game of uno is going to start!");
        if (bet != 0)
            eb.setDescription(String.format("This game requires a %d credits bet.\nThe winner receives the sum of all bets", bet));
        eb.addField("Players", "No Players yet", false);
        eb.setFooter("React with \uD83D\uDD90️ to join, ▶️ to start and ❌ to cancel the game");
        e.getChannel().sendMessage(eb.build()).queue(m -> {
            unogame.setMessageID(m.getIdLong());
            m.addReaction("\uD83D\uDD90️").queue();
            m.addReaction("▶️").queue();
            m.addReaction("❌").queue();
        });
    }
}
