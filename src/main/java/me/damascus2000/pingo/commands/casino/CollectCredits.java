package me.damascus2000.pingo.commands.casino;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.commands.settings.CommandState;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.data.handlers.CreditDataHandler;
import me.damascus2000.pingo.data.handlers.GeneralDataHandler;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class CollectCredits extends Command {


    public CollectCredits(){
        this.name = "daily";
        this.aliases = new String[]{"collect", "dailycredits"};
        this.category = Category.CASINO;
        this.description = "daily.description";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long guildId = e.getGuild().getIdLong();
        if (args.length != 0)
            throw new MessageException(this.getUsage(guildId));
        long id = e.getAuthor().getIdLong();
        CreditDataHandler dataHandler = new CreditDataHandler();
        LocalDateTime latestcollect = dataHandler.getLatestCollect(guildId, id);
        MyResourceBundle language = Utils.getLanguage(guildId);
        if (latestcollect != null && !LocalDateTime.now().minusDays(1).isAfter(latestcollect)){
            LocalDateTime till = latestcollect.plusDays(1);
            LocalDateTime temp = LocalDateTime.now();
            long hours = temp.until(till, ChronoUnit.HOURS);
            long minutes = temp.plusHours(hours).until(till, ChronoUnit.MINUTES);
            throw new MessageException(language.getString("daily.wait", hours, minutes));
        }
        int daily = (int) Utils.config.get("daily");
        int creds = dataHandler.addCredits(guildId, id, daily);
        dataHandler.setLatestCollect(guildId, id, LocalDateTime.now());
        e.getChannel().sendMessage(language.getString("daily.success", daily, creds)).queue();
        GeneralDataHandler handler = new GeneralDataHandler();
        int startXP = handler.getXP(guildId, id);
        checkAchievements(e.getChannel(), id);
        int endXp = handler.getXP(guildId, id);
        checkLevel(e.getChannel(), e.getMember(), startXP, endXp);
    }
}
