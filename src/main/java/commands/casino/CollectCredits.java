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

public class CollectCredits extends Command {


    public CollectCredits(){
        this.name = "daily";
        this.aliases = new String[]{"collect", "dailycredits"};
        this.category = "Casino";
        this.description = "daily.description";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (args.length != 0)
            throw new MessageException(this.getUsage());

        long id = e.getAuthor().getIdLong();
        DataHandler dataHandler = new DataHandler();
        LocalDateTime latestcollect = dataHandler.getLatestCollect(e.getGuild().getIdLong(), id);
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (latestcollect != null && !LocalDateTime.now().minusDays(1).isAfter(latestcollect)){
            LocalDateTime till = latestcollect.plusDays(1);
            LocalDateTime temp = LocalDateTime.now();
            long hours = temp.until(till, ChronoUnit.HOURS);
            long minutes = temp.plusHours(hours).until(till, ChronoUnit.MINUTES);
            throw new MessageException(language.getString("daily.wait", hours, minutes));
        }
        int creds = dataHandler.addCredits(e.getGuild().getIdLong(), id, 2500);
        dataHandler.setLatestCollect(e.getGuild().getIdLong(), id, LocalDateTime.now());
        e.getChannel().sendMessage(language.getString("daily.success", 2500, creds)).queue();

    }
}
