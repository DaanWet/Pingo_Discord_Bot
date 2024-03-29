package commands.casino;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import data.handlers.CreditDataHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Weekly extends Command {

    public Weekly(){
        this.name = "weekly";
        this.aliases = new String[]{"weeklycredits"};
        this.category = Category.CASINO;
        this.description = "weekly.description";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (args.length != 0)
            throw new MessageException(this.getUsage(e.getGuild().getIdLong()));

        CreditDataHandler dataHandler = new CreditDataHandler();
        long id = e.getAuthor().getIdLong();
        LocalDateTime latestcollect = dataHandler.getLatestWeekCollect(e.getGuild().getIdLong(), id);
        if (latestcollect != null && !LocalDateTime.now().minusDays(7).isAfter(latestcollect)){
            LocalDateTime till = latestcollect.plusDays(7);
            LocalDateTime now = LocalDateTime.now();
            long days = now.until(till, ChronoUnit.DAYS);
            long hours = now.plusDays(days).until(till, ChronoUnit.HOURS);
            long minutes = now.plusDays(days).plusHours(hours).until(till, ChronoUnit.MINUTES);
            throw new MessageException(language.getString("weekly.wait", days, hours, minutes));
        }
        int weekly = (int) Utils.config.get("weekly");
        int creds = dataHandler.addCredits(e.getGuild().getIdLong(), id, weekly);
        dataHandler.setLatestWeekCollect(e.getGuild().getIdLong(), id, LocalDateTime.now());
        e.getChannel().sendMessage(language.getString("weekly.success", weekly, creds)).queue();

    }
}
