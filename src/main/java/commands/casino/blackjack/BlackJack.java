package commands.casino.blackjack;

import commands.settings.CommandState;
import commands.settings.Setting;
import companions.GameCompanion;
import companions.cardgames.BlackJackGame;
import data.handlers.CreditDataHandler;
import data.handlers.GeneralDataHandler;
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


    public BlackJack(GameCompanion gameCompanion){
        super(gameCompanion);
        this.name = "blackjack";
        this.aliases = new String[]{"bj", "21"};
        this.arguments = new String[]{"<bet>"};
        this.description = "bj.description";
        this.example = "10k";
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
        if (gameCompanion.isUnoChannel(guildId, e.getChannel().getIdLong()))
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
            throw new MessageException(language.getString("credit.error.not_enough", bet));
        BlackJackGame objg = gameCompanion.getBlackJackGame(guildId, playerId);
        if (objg != null)
            throw new MessageException(language.getString("bj.error.playing"));

        BlackJackGame bjg = new BlackJackGame(bet);
        SettingsDataHandler settingDH = new SettingsDataHandler();
        settingDH.setCooldown(guildId, playerId, Setting.BLACKJACK, LocalDateTime.now());
        String prefix = settingDH.getStringSetting(guildId, Setting.PREFIX).get(0);
        EmbedBuilder eb = bjg.buildEmbed(author.getName(), prefix, language);
        GeneralDataHandler handler = new GeneralDataHandler();
        int xp = 0;
        int startXP = handler.getXP(guildId, playerId);;
        int endXP = handler.getXP(guildId, playerId);;
        gameCompanion.putBlackJackGame(guildId, playerId, bjg);
        if (bjg.hasEnded()){
            int credits = dataHandler.addCredits(guildId, playerId, bjg.getWonCreds());
            xp = bjg.getWonXP();
            if (xp > 0){
                endXP = handler.addXP(guildId, playerId, xp);
            }
            eb.addField(language.getString("credit.name"), language.getString("credit.new", credits) + "\n" + language.getString("xp.new", xp), false);
        }
        e.getChannel().sendMessageEmbeds(eb.build()).queue(m -> {
            bjg.setMessageId(m.getIdLong());
            if (bjg.hasEnded())
                updateRecords(guildId, playerId, bjg.getWonCreds(), m.getJumpUrl());
        });
        if (bjg.hasEnded()){
            checkLevel(e.getChannel(), e.getMember(), startXP, endXP);
            //checkAchievements(e.getChannel(), id, gameCompanion);
            gameCompanion.removeBlackJackGame(guildId, playerId);
            //checkLevel(m.getTextChannel(), e.getMember(), endXP, handler.getXP(guildId, id)); //TODO change this ??
        }

    }
}
