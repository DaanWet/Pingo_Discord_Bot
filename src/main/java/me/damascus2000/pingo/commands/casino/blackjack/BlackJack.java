package me.damascus2000.pingo.commands.casino.blackjack;

import me.damascus2000.pingo.commands.settings.CommandState;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.cardgames.BlackJackGame;
import me.damascus2000.pingo.data.handlers.SettingsDataHandler;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.services.AchievementService;
import me.damascus2000.pingo.services.MemberService;
import me.damascus2000.pingo.services.RecordService;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BlackJack extends BCommand {


    public BlackJack(GameCompanion gameCompanion, MemberService memberService, RecordService recordService, AchievementService achievementService){
        super(gameCompanion, memberService, recordService, achievementService);
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
        if (args.length != 0 && args[0].matches("(?i)all(-?in)?")){
            bet = memberService.getCredits(guildId, playerId);

        }

        if (bet < 10)
            throw new MessageException(language.getString("credit.error.least"));
        if (memberService.getCredits(guildId, playerId) < bet)
            throw new MessageException(language.getString("credit.error.not_enough", bet));
        BlackJackGame objg = gameCompanion.getBlackJackGame(guildId, playerId);
        if (objg != null)
            throw new MessageException(language.getString("bj.error.playing"));
        int startXP = memberService.getXP(guildId, playerId);
        int level = Utils.getLevel(startXP);
        BlackJackGame bjg = new BlackJackGame(bet, level, canBeta(guildId));
        SettingsDataHandler settingDH = new SettingsDataHandler();
        settingDH.setCooldown(guildId, playerId, Setting.BLACKJACK, LocalDateTime.now());
        String prefix = settingDH.getStringSetting(guildId, Setting.PREFIX).get(0);
        EmbedBuilder eb = bjg.buildEmbed(author.getName(), prefix, language);
        int xp = 0;
        int endXP = memberService.getXP(guildId, playerId);
        gameCompanion.putBlackJackGame(guildId, playerId, bjg);
        if (bjg.hasEnded()){
            int credits = memberService.addCredits(guildId, playerId, bjg.getWonCreds());
            xp = bjg.getWonXP();
            if (xp > 0 && canBeta(guildId)){
                endXP = memberService.addXP(guildId, playerId, xp);
            }
            String desc = language.getString("credit.new", credits) + "\n";
            if (canBeta(guildId))
                desc += language.getString("xp.new", xp);
            eb.addField(language.getString("credit.name"), desc, false);
        }
        e.getChannel().sendMessageEmbeds(eb.build()).queue(m -> {
            bjg.setMessageId(m.getIdLong());
            if (bjg.hasEnded())
                updateRecords(guildId, playerId, bjg.getWonCreds(), m.getJumpUrl());
        });
        if (bjg.hasEnded()){
            checkLevel(e.getChannel(), e.getMember(), startXP, endXP);
            checkAchievements(e.getChannel(), playerId, gameCompanion);
            gameCompanion.removeBlackJackGame(guildId, playerId);
            checkLevel(e.getChannel(), e.getMember(), endXP, memberService.getXP(guildId, playerId)); //TODO change this ??
        }

    }
}
