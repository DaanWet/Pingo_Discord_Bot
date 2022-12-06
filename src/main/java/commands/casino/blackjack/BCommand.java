package commands.casino.blackjack;

import commands.Command;
import commands.settings.Setting;
import companions.GameCompanion;
import companions.Record;
import companions.cardgames.BlackJackGame;
import data.handlers.CreditDataHandler;
import data.handlers.RecordDataHandler;
import data.handlers.SettingsDataHandler;
import data.models.RecordData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MyResourceBundle;

public abstract class BCommand extends Command {

    protected final GameCompanion gameCompanion;

    public BCommand(GameCompanion gameCompanion){
        this.gameCompanion = gameCompanion;
        this.category = Category.CASINO;
        this.hidden = true;
    }

    protected void updateMessage(GuildMessageReceivedEvent e, BlackJackGame bjg, CreditDataHandler dataHandler, MyResourceBundle language){
        long guildId = e.getGuild().getIdLong();
        long id = e.getAuthor().getIdLong();
        if (bjg.hasEnded())
            gameCompanion.removeBlackJackGame(guildId, id);
        e.getChannel().retrieveMessageById(bjg.getMessageId()).queue(m -> {
            String prefix = new SettingsDataHandler().getStringSetting(guildId, Setting.PREFIX).get(0);
            EmbedBuilder eb = bjg.buildEmbed(e.getAuthor().getName(), prefix, language);
            if (bjg.hasEnded()){
                int won_lose = bjg.getWonCreds();
                int credits = dataHandler.addCredits(guildId, id, won_lose);
                eb.addField(language.getString("credit.name"), language.getString("credit.new", credits), false);
                updateRecords(guildId, id, won_lose, m.getJumpUrl());
            }
            m.editMessageEmbeds(eb.build()).queue();
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
