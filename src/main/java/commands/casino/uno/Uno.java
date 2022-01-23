package commands.casino.uno;

import commands.Command;
import commands.settings.Setting;
import companions.GameHandler;
import companions.uno.UnoGame;
import data.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.time.LocalDateTime;
import java.util.Properties;

public class Uno extends Command {

    private final GameHandler gameHandler;

    public Uno(GameHandler gameHandler){
        this.name = "uno";
        this.aliases = new String[]{"playuno"};
        this.category = Category.CASINO;
        this.arguments = "[<bet>]";
        this.description = "uno.description";
        this.gameHandler = gameHandler;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        if (gameHandler.getUnoGame(e.getGuild().getIdLong()) != null){
            throw new MessageException(language.getString("uno.error.started"));
        }
        int bet = 0;
        if (args.length > 1)
            throw new MessageException(language.getString("uno.error.valid_bet"));
        DataHandler dataHandler = new DataHandler();
        if (args.length == 1){
            if (!dataHandler.getBoolSetting(guildId, Setting.BETTING)){
                e.getChannel().sendMessage(language.getString("uno.error.betting")).queue();
            } else {
                bet = Utils.getInt(args[0]);
                if (bet < 10)
                    throw new MessageException(language.getString("credit.error.least"));
                if (dataHandler.getCredits(guildId, e.getAuthor().getIdLong()) < bet)
                    throw new MessageException(language.getString("credit.error.not_enough", bet));
            }
        }
        dataHandler.setCooldown(guildId, e.getAuthor().getIdLong(), Setting.UNO, LocalDateTime.now());
        UnoGame unogame = new UnoGame(bet, e.getAuthor().getIdLong(), e.getChannel().getIdLong());
        gameHandler.setUnoGame(guildId, unogame);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(language.getString("uno.embed.title"));
        if (bet != 0)
            eb.setDescription(language.getString("uno.embed.description", bet));
        eb.addField(language.getString("uno.embed.players.title"), language.getString("uno.embed.players.no_players"), false);
        Properties config = Utils.config;
        eb.setFooter(language.getString("uno.embed.footer", config.getProperty("emoji.uno.join"), config.getProperty("emoji.uno.start"), config.getProperty("emoji.cancel")));
        e.getChannel().sendMessage(eb.build()).queue(m -> {
            unogame.setMessageID(m.getIdLong());
            m.addReaction(config.getProperty("emoji.uno.join")).queue();
            m.addReaction(config.getProperty("emoji.uno.start")).queue();
            m.addReaction(config.getProperty("emoji.cancel")).queue();
        });
    }
}
