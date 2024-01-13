package me.damascus2000.pingo.commands.casino.blackjack;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.Record;
import me.damascus2000.pingo.companions.cardgames.BlackJackGame;
import me.damascus2000.pingo.data.handlers.SettingsDataHandler;
import me.damascus2000.pingo.models.UserRecord;
import me.damascus2000.pingo.services.MemberService;
import me.damascus2000.pingo.services.RecordService;
import me.damascus2000.pingo.utils.MyResourceBundle;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public abstract class BCommand extends Command {

    protected final GameCompanion gameCompanion;
    protected final MemberService memberService;
    protected final RecordService recordService;

    public BCommand(GameCompanion gameCompanion, MemberService memberService, RecordService recordService){
        this.gameCompanion = gameCompanion;
        this.memberService = memberService;
        this.recordService = recordService;
        this.category = Category.CASINO;
        this.hidden = true;
    }

    protected void updateMessage(GuildMessageReceivedEvent e, BlackJackGame bjg, MyResourceBundle language) throws Exception{
        long guildId = e.getGuild().getIdLong();
        long id = e.getAuthor().getIdLong();
        e.getChannel().retrieveMessageById(bjg.getMessageId()).queue(m -> {
            String prefix = new SettingsDataHandler().getStringSetting(guildId, Setting.PREFIX).get(0);
            EmbedBuilder eb = bjg.buildEmbed(e.getAuthor().getName(), prefix, language);
            int startXP = memberService.getXP(guildId, id);
            int endXP = memberService.getXP(guildId, id);
            int xp = 0;
            if (bjg.hasEnded()){
                int won_lose = bjg.getWonCreds();
                xp = bjg.getWonXP();
                int credits = memberService.addCredits(guildId, id, won_lose);
                if (xp > 0 && canBeta(guildId)){
                    startXP = memberService.getXP(guildId, id);
                    endXP = memberService.addXP(guildId, id, xp);
                }
                String desc = language.getString("credit.new", credits) + "\n";
                if (betaGuilds.contains(guildId))
                    desc += language.getString("xp.new", xp);
                eb.addField(language.getString("credit.name"), desc, false);
                updateRecords(guildId, id, won_lose, m.getJumpUrl());
            }

            m.editMessageEmbeds(eb.build()).queue();
            if (bjg.hasEnded()){
                checkLevel(m.getTextChannel(), e.getMember(), startXP, endXP);
                checkAchievements(e.getChannel(), id, gameCompanion);
                gameCompanion.removeBlackJackGame(guildId, id);
                checkLevel(m.getTextChannel(), e.getMember(), endXP, memberService.getXP(guildId, id)); //TODO change this ??
            }
        });
    }

    protected void updateRecords(long guildId, long playerId, int won_lose, String jumpurl){
        recordService.setRecord(guildId, playerId, won_lose > 0 ? Record.WIN : Record.LOSS, won_lose > 0 ? won_lose : won_lose * -1, jumpurl, false);
        UserRecord playedGames = recordService.getRecord(guildId, playerId, Record.GAMES);
        UserRecord winRate = recordService.getRecord(guildId, playerId, Record.WIN_RATE);
        int temp = playedGames == null ? 0 : (int) playedGames.getValue();
        double tempw = winRate == null ? 0.0 : winRate.getValue();
        recordService.setRecord(guildId, playerId, Record.GAMES, temp + 1, false);
        recordService.setRecord(guildId, playerId, Record.WIN_RATE, tempw + (((won_lose > 0 ? 1.0 : won_lose == 0 ? 0.5 : 0.0) - tempw) / (temp + 1.0)), true);
        int streak = memberService.getStreak(guildId, playerId);
        int newstreak = 0;
        if (won_lose > 0){
            newstreak = streak < 0 ? 1 : streak + 1;
        } else if (won_lose < 0){
            newstreak = streak > 0 ? -1 : streak - 1;
        }
        memberService.setStreak(guildId, playerId, newstreak, jumpurl);
    }


}
