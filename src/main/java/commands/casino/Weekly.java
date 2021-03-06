package commands.casino;

import commands.Command;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Weekly extends Command {

    public Weekly() {
        this.name = "weekly";
        this.aliases = new String[]{"weeklycredits"};
        this.category = "Casino";
        this.description = "Collect your weekly credits";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (args.length == 0) {
            DataHandler dataHandler = new DataHandler();
            Long id = e.getAuthor().getIdLong();
            LocalDateTime latestcollect = dataHandler.getLatestWeekCollect(e.getGuild().getIdLong(), id);
            if (latestcollect == null || LocalDateTime.now().minusDays(7).isAfter(latestcollect)) {
                int creds = dataHandler.addCredits(e.getGuild().getIdLong(), id, 15000);
                dataHandler.setLatestWeekCollect(e.getGuild().getIdLong(), id, LocalDateTime.now());
                e.getChannel().sendMessage(String.format("You collected your weekly **15000 credits** \nYour new balance is now **%d credits**", creds)).queue();
            } else {
                LocalDateTime till = latestcollect.plusDays(7);
                LocalDateTime temp = LocalDateTime.now();
                long days = temp.until(till, ChronoUnit.DAYS);
                long hours = temp.plusDays(days).until(till, ChronoUnit.HOURS);
                long minutes = temp.plusDays(days).plusHours(hours).until(till, ChronoUnit.MINUTES);
                e.getChannel().sendMessage(String.format("You need to wait %d day%s, %d hour%s and %d minute%s before you can collect your next credits", days, days == 1 ? "" : "s", hours, hours == 1 ? "" : "s", minutes, minutes == 1 ? "" : "s")).queue();
            }

        } else {
            e.getChannel().sendMessage(this.getUsage()).queue();
        }
    }
}
