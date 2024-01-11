package me.damascus2000.pingo.commands.casino.uno;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.commands.Help;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.uno.UnoGame;
import me.damascus2000.pingo.data.handlers.CreditDataHandler;
import me.damascus2000.pingo.data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyProperties;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;

import java.time.LocalDateTime;

public class Uno extends Command {

    private final GameCompanion gameCompanion;
    private final Help help;

    public Uno(GameCompanion gameCompanion, Help help){
        this.name = "uno";
        this.aliases = new String[]{"playuno"};
        this.category = Category.CASINO;
        this.arguments = new String[]{"[bet]"};
        this.description = "uno.description";
        this.gameCompanion = gameCompanion;
        this.help = help;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        if (gameCompanion.getUnoGame(e.getGuild().getIdLong()) != null){
            throw new MessageException(language.getString("uno.error.started"));
        }
        int bet = 0;
        if (args.length > 1)
            throw new MessageException(language.getString("uno.error.valid_bet"));
        SettingsDataHandler settingDH = new SettingsDataHandler();
        if (args.length == 1){
            if (!settingDH.getBoolSetting(guildId, Setting.BETTING)){
                e.getChannel().sendMessage(language.getString("uno.error.betting")).queue();
            } else {
                bet = Utils.getInt(args[0]);
                if (bet < 10)
                    throw new MessageException(language.getString("credit.error.least"));
                if (new CreditDataHandler().getCredits(guildId, e.getAuthor().getIdLong()) < bet)
                    throw new MessageException(language.getString("credit.error.not_enough", bet));
            }
        }
        settingDH.setCooldown(guildId, e.getAuthor().getIdLong(), Setting.UNO, LocalDateTime.now());
        UnoGame unogame = new UnoGame(bet, e.getAuthor().getIdLong(), guildId, e.getChannel().getIdLong());
        gameCompanion.setUnoGame(guildId, unogame);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(language.getString("uno.embed.title"));
        if (bet != 0)
            eb.setDescription(language.getString("uno.embed.description", bet));
        eb.addField(language.getString("uno.embed.players.title"), language.getString("uno.embed.players.no_players"), false);
        MyProperties config = Utils.config;
        eb.setFooter(language.getString("uno.embed.footer", config.getProperty("emoji.uno.join"), config.getProperty("emoji.uno.start"), config.getProperty("emoji.cancel")));

        EmbedBuilder eb2 = help.getUnoHelp(new EmbedBuilder(), language, settingDH.getStringSetting(guildId, Setting.PREFIX).get(0));
        e.getChannel().sendMessageEmbeds(eb.build()).queue(m -> {
            unogame.setMessageID(m.getIdLong());
            m.addReaction(config.getProperty("emoji.uno.join")).queue();
            m.addReaction(config.getProperty("emoji.uno.start")).queue();
            m.addReaction(config.getProperty("emoji.cancel")).queue();
        });
    }

}
