package commands.casino;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import data.DataHandler;
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
        this.category = "Casino";
        this.description = "weekly.description";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (args.length != 0)
            throw new MessageException(this.getUsage());

        DataHandler dataHandler = new DataHandler();
        Long id = e.getAuthor().getIdLong();
        LocalDateTime latestcollect = dataHandler.getLatestWeekCollect(e.getGuild().getIdLong(), id);
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (latestcollect != null && !LocalDateTime.now().minusDays(7).isAfter(latestcollect)){
            LocalDateTime till = latestcollect.plusDays(7);
            LocalDateTime temp = LocalDateTime.now();
            long days = temp.until(till, ChronoUnit.DAYS);
            long hours = temp.plusDays(days).until(till, ChronoUnit.HOURS);
            long minutes = temp.plusDays(days).plusHours(hours).until(till, ChronoUnit.MINUTES);
            throw new MessageException(language.getString("weekly.wait", hours, minutes));
        }
        int creds = dataHandler.addCredits(e.getGuild().getIdLong(), id, 15000);
        dataHandler.setLatestWeekCollect(e.getGuild().getIdLong(), id, LocalDateTime.now());
        e.getChannel().sendMessage(language.getString("weekly.success", 15000, creds)).queue();

    }
}
