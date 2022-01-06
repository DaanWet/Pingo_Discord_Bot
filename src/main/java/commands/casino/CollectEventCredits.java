package commands.casino;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import data.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CollectEventCredits extends Command {

    private final String ROLL = "<a:rolling_number_75:922895069827194880>";
    private final String[] emoji = new String[]{"0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣"};
    private final String eDescription = "Your gift is falling down the chimney, it contains:\n%s%s%s%s%s%s%s **credits**";


    public CollectEventCredits(){
        this.name = "daily";
        this.aliases = new String[]{"collect", "dailycredits"};
        this.category = "Casino";
        this.description = "Collect your daily credits";
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
        if (latestcollect != null && !LocalDateTime.now().minusDays(1).isAfter(latestcollect)){
            LocalDateTime till = latestcollect.plusDays(1);
            LocalDateTime temp = LocalDateTime.now();
            long hours = temp.until(till, ChronoUnit.HOURS);
            long minutes = temp.plusHours(hours).until(till, ChronoUnit.MINUTES);
            throw new MessageException(
                    String.format(
                            "You need to wait %d hour%s and %d minute%s before you can collect your next credits",
                            hours, hours == 1 ? "" : "s", minutes, minutes == 1 ? "" : "s"));
        }
        Random random = new Random();
        double c = random.nextDouble();
        /*
         * 1m => 0.01
         * 100k => 0.01 - 0.06
         * 50k => 0.06 - 0.165
         * 10k => 0.165 - 0.32
         * 7500 => 0.32 - 0.49
         * 5000 =>0.49 - 0.69
         * 3000 => 0.69 - 1.0
         * */
        final int value;
        if (c < 0.01)
            value = 1000000;
        else if (c < 0.06)
            value = 100000;
        else if (c < 0.165)
            value = 50000;
        else if (c < 0.32)
            value = 10000;
        else if (c < 0.49)
            value = 7500;
        else if (c < 0.69)
            value = 5000;
        else
            value = 3000;

        int first = value / 1000000;
        int second = (value / 100000) - first * 10;
        int third = (value / 10000) - (second * 10 + first * 100);
        int fourth = (value / 1000) - (third * 10 + second * 100 + first * 1000);
        int fifth = value / 100 - (fourth * 10 + third * 100 + second * 1000 + first * 10000);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.GREEN);
        eb.setTitle("You're collecting your daily christmas gift");
        eb.setDescription(String.format(eDescription, ROLL, ROLL, ROLL, ROLL, ROLL, ROLL, ROLL));


        e.getChannel().sendMessage(eb.build()).queue(m -> {
            m.editMessage(eb.setDescription(String.format(eDescription, ROLL, ROLL, ROLL, ROLL, ROLL, ROLL, emoji[0])).build()).queueAfter(3, TimeUnit.SECONDS);
            m.editMessage(eb.setDescription(String.format(eDescription, ROLL, ROLL, ROLL, ROLL, ROLL, emoji[0], emoji[0])).build()).queueAfter(4, TimeUnit.SECONDS);
            m.editMessage(eb.setDescription(String.format(eDescription, ROLL, ROLL, ROLL, ROLL, emoji[fifth], emoji[0], emoji[0])).build()).queueAfter(5, TimeUnit.SECONDS);
            m.editMessage(eb.setDescription(String.format(eDescription, ROLL, ROLL, ROLL, emoji[fourth], emoji[fifth], emoji[0], emoji[0])).build()).queueAfter(6, TimeUnit.SECONDS);
            m.editMessage(eb.setDescription(String.format(eDescription, ROLL, ROLL, emoji[third], emoji[fourth], emoji[fifth], emoji[0], emoji[0])).build()).queueAfter(7, TimeUnit.SECONDS);
            m.editMessage(eb.setDescription(String.format(eDescription, ROLL, emoji[second], emoji[third], emoji[fourth], emoji[fifth], emoji[0], emoji[0])).build()).queueAfter(8, TimeUnit.SECONDS);
            m.editMessage(eb.setTitle("You collected your daily christmas gift")
                                  .setDescription(String.format("Your gift fell down the chimney, it contained:\n%s%s%s%s%s%s%s **credits**", emoji[first], emoji[second], emoji[third], emoji[fourth], emoji[fifth], emoji[0], emoji[0])).build()).queueAfter(9, TimeUnit.SECONDS, me -> {
                int creds = dataHandler.addCredits(e.getGuild().getIdLong(), id, value);
                dataHandler.setLatestCollect(e.getGuild().getIdLong(), id, LocalDateTime.now());
                me.editMessage(new EmbedBuilder(me.getEmbeds().get(0)).appendDescription(String.format("\n\nYour new balance is now **%d credits **", creds)).build()).queue();
            });

        });
    }
}
