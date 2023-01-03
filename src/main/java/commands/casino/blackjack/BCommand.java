package commands.casino.blackjack;

import commands.Command;
import commands.settings.Setting;
import companions.GameCompanion;
import companions.Record;
import companions.cardgames.BlackJackGame;
import data.handlers.CreditDataHandler;
import data.handlers.GeneralDataHandler;
import data.handlers.RecordDataHandler;
import data.handlers.SettingsDataHandler;
import data.models.RecordData;
import listeners.MessageListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import utils.MessageException;
import utils.MyResourceBundle;

public abstract class BCommand extends Command {

    protected final GameCompanion gameCompanion;

    public BCommand(GameCompanion gameCompanion){
        this.gameCompanion = gameCompanion;
        this.category = Category.CASINO;
        this.hidden = true;
    }

    protected void updateMessage(GuildMessageReceivedEvent e, BlackJackGame bjg, CreditDataHandler dataHandler, MyResourceBundle language) throws Exception{
        long guildId = e.getGuild().getIdLong();
        long id = e.getAuthor().getIdLong();
        e.getChannel().retrieveMessageById(bjg.getMessageId()).queue(m -> {
            String prefix = new SettingsDataHandler().getStringSetting(guildId, Setting.PREFIX).get(0);
            EmbedBuilder eb = bjg.buildEmbed(e.getAuthor().getName(), prefix, language);
            GeneralDataHandler handler = new GeneralDataHandler();
            int startXP = handler.getXP(guildId, id);
            int endXP = handler.getXP(guildId, id);
            int xp = 0;
            if (bjg.hasEnded()){
                int won_lose = bjg.getWonCreds();
                xp = bjg.getWonXP();
                int credits = dataHandler.addCredits(guildId, id, won_lose);
                if (xp > 0 && canBeta(guildId)){
                    startXP = handler.getXP(guildId, id);
                    endXP = handler.addXP(guildId, id, xp);
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
                //checkAchievements(e.getChannel(), id, gameCompanion);
                gameCompanion.removeBlackJackGame(guildId, id);
                //checkLevel(m.getTextChannel(), e.getMember(), endXP, handler.getXP(guildId, id)); //TODO change this ??
            }
        });
    }

    protected void updateRecords(long guildId, long playerId, int won_lose, String jumpurl){
        RecordDataHandler dataHandler = new RecordDataHandler();
        dataHandler.setRecord(guildId, playerId, won_lose > 0 ? Record.WIN : Record.LOSS, won_lose > 0 ? won_lose : won_lose * -1, jumpurl, false);
        RecordData played_games = dataHandler.getRecord(guildId, playerId, Record.GAMES);
        RecordData winrate = dataHandler.getRecord(guildId, playerId, Record.WIN_RATE);
        int temp = played_games == null ? 0 : (int) played_games.getValue();
        double tempw = winrate == null ? 0.0 : winrate.getValue();
        dataHandler.setRecord(guildId, playerId, Record.GAMES, temp + 1, false);
        dataHandler.setRecord(guildId, playerId, Record.WIN_RATE, tempw + (((won_lose > 0 ? 1.0 : won_lose == 0 ? 0.5 : 0.0) - tempw) / (temp + 1.0)), true);
        int streak = dataHandler.getStreak(guildId, playerId);
        int newstreak = 0;
        if (won_lose > 0){
            newstreak = streak < 0 ? 1 : streak + 1;
        } else if (won_lose < 0){
            newstreak = streak > 0 ? -1 : streak - 1;
        }
        dataHandler.setStreak(guildId, playerId, newstreak, jumpurl);
    }


}
