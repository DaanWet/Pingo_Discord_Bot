package commands.casino.blackjack;

import commands.settings.CommandState;
import commands.settings.Setting;
import companions.GameHandler;
import companions.cardgames.BlackJackGame;
import data.handlers.CreditDataHandler;
import data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.time.LocalDateTime;


public class BlackJack extends BCommand {


    public BlackJack(GameHandler gameHandler){
        super(gameHandler);
        this.name = "blackjack";
        this.aliases = new String[]{"bj", "21"};
        this.arguments = "<bet>";
        this.description = "bj.description";
        this.hidden = false;
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        CommandState betting = canBeExecuted(guildId, channelId, member, Setting.BETTING);
        CommandState blackjack = canBeExecuted(guildId, channelId, member, Setting.BLACKJACK);
        return betting.worst(blackjack);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        User author = e.getAuthor();
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        if (gameHandler.isUnoChannel(guildId, e.getChannel().getIdLong()))
            throw new MessageException(language.getString("bj.error.uno"));

        long playerId = author.getIdLong();
        int bet = args.length == 0 ? 0 : Utils.getInt(args[0]);
        CreditDataHandler dataHandler = new CreditDataHandler();
        if (args.length != 0 && args[0].matches("(?i)all(-?in)?")){
            bet = dataHandler.getCredits(guildId, playerId);
        }

        if (bet < 10)
            throw new MessageException(language.getString("credit.error.least"));
        if (dataHandler.getCredits(guildId, playerId) < bet)
            throw new MessageException(language.getString("credit.error.no_enough", bet));
        BlackJackGame objg = gameHandler.getBlackJackGame(guildId, playerId);
        if (objg != null)
            throw new MessageException(language.getString("blackjack.error.playing"));

        BlackJackGame bjg = new BlackJackGame(bet);
        SettingsDataHandler settingDH = new SettingsDataHandler();
        settingDH.setCooldown(guildId, playerId, Setting.BLACKJACK, LocalDateTime.now());
        String prefix = settingDH.getStringSetting(guildId, Setting.PREFIX).get(0);
        EmbedBuilder eb = bjg.buildEmbed(author.getName(), prefix, language);
        if (!bjg.hasEnded()){
            gameHandler.putBlackJackGame(guildId, playerId, bjg);
        } else {
            int credits = dataHandler.addCredits(guildId, playerId, bjg.getWonCreds());
            eb.addField(language.getString("credit.name"), language.getString("credit.new", credits), false);
        }
        e.getChannel().sendMessageEmbeds(eb.build()).queue(m -> {
            if (!bjg.hasEnded()) bjg.setMessageId(m.getIdLong());
            else
                updateRecords(guildId, playerId, bjg.getWonCreds(), m.getJumpUrl());
        });
    }
}
