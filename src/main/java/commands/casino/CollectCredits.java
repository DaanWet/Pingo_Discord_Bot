package commands.casino;

import commands.Command;
import utils.DataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CollectCredits extends Command {


    public CollectCredits() {
        this.name = "daily";
        this.aliases = new String[]{"collect", "dailycredits"};
        this.category = "Casino";
        this.description = "Collect your daily credits";
    }

    @Override
    public boolean canBeExecuted(long guildId, long channelId, long userId){
        DataHandler dataHandler = new DataHandler();
        Boolean betting = dataHandler.getBoolSetting(guildId, "betting", "commands");
        return betting == null || betting;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 0) {
            long id = e.getAuthor().getIdLong();
            DataHandler dataHandler = new DataHandler();
            LocalDateTime latestcollect = dataHandler.getLatestCollect(e.getGuild().getIdLong(), id);
            if (latestcollect == null || LocalDateTime.now().minusDays(1).isAfter(latestcollect)){
                int creds = dataHandler.addCredits(e.getGuild().getIdLong(), id, 2500);
                dataHandler.setLatestCollect(e.getGuild().getIdLong(), id, LocalDateTime.now());
                e.getChannel().sendMessage(String.format("You collected your daily **2,500 credits** \nYour new balance is now **%d credits**", creds)).queue();
            } else {
                LocalDateTime till = latestcollect.plusDays(1);
                LocalDateTime temp = LocalDateTime.now();
                long hours = temp.until(till, ChronoUnit.HOURS);
                long minutes = temp.plusHours(hours).until(till, ChronoUnit.MINUTES);
                e.getChannel().sendMessage(String.format("You need to wait %d hour%s and %d minute%s before you can collect your next credits", hours, hours == 1 ? "" : "s", minutes, minutes == 1 ? "" : "s")).queue();
            }

        } else {
            e.getChannel().sendMessage(this.getUsage()).queue();
        }
    }
}
