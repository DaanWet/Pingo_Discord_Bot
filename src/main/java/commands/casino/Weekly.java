package commands.casino;

import commands.Command;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Weekly extends Command {

    private DataHandler dataHandler;

    public Weekly() {
        this.name = "weekly";
        this.aliases = new String[]{"weeklycredits"};
        this.dataHandler = new DataHandler();
        this.category = "Casino";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 0) {
            String id = e.getAuthor().getId();
            LocalDateTime latestcollect = dataHandler.getLatestWeekCollect(id);
            if (LocalDateTime.now().minusDays(7).isAfter(latestcollect)){
                int creds = dataHandler.addCredits(id, 15000);
                dataHandler.setLatestWeekCollect(id, LocalDateTime.now());
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

    @Override
    public String getDescription() {
        return "Collect your weekly credits";
    }
}
