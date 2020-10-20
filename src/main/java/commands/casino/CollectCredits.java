package commands.casino;

import commands.Command;
import net.dv8tion.jda.api.entities.User;
import utils.DataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CollectCredits extends Command {

    private DataHandler dataHandler;

    public CollectCredits() {
        this.name = "daily";
        this.aliases = new String[]{"collect", "dailycredits"};
        this.dataHandler = new DataHandler();
        this.category = "Casino";
        this.description = "Collect your daily credits";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 0) {
            String id = e.getAuthor().getId();
            LocalDateTime latestcollect = dataHandler.getLatestCollect(id);
            if (LocalDateTime.now().minusDays(1).isAfter(latestcollect)){
                int creds = dataHandler.addCredits(id, 2500);
                dataHandler.setLatestCollect(id, LocalDateTime.now());
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
