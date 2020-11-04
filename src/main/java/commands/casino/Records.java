package commands.casino;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import utils.DataHandler;
import utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class Records extends Command {

    private Properties properties;


    public Records() {
        this.name = "records";
        this.category = "Casino";
        this.description = "Show records";
        this.arguments = "[<member>]";
        properties = new Properties();
        try {
            properties.load(Records.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        DataHandler dataHandler = new DataHandler();
        if (args.length == 0) {
            HashMap<String, Triple<Long, Double, String>> records = dataHandler.getRecords(e.getGuild().getIdLong());
            if (records.size() == 0) return;
            e.getGuild().retrieveMembersByIds(records.values().stream().map(Triple::getLeft).collect(Collectors.toList())).onSuccess(list -> {
                Map<Long, Member> m = list.stream().collect(Collectors.toMap(Member::getIdLong, member -> member));
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Casino Records");
                StringBuilder sb = new StringBuilder();
                for (String record : records.keySet()) {
                    Triple<Long, Double, String> v = records.get(record);
                    sb.append(":small_blue_diamond: ").append(properties.getProperty(record))
                            .append(": **");
                    boolean isInt = dataHandler.isInt(record);
                    // Formats the blackjack winrate into something more human readable
                    sb.append( isInt ? v.getMiddle().intValue() : String.format("%.2f%s", v.getMiddle() * 100, "%"));

                    sb.append("** by ")
                            .append(m.get(v.getLeft()).getAsMention());
                    if (v.getRight() != null) {
                        sb.append(" [jump](").append(v.getRight()).append(")");
                    }
                    sb.append("\n");
                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessage(eb.build()).queue();
            });
        } else if (e.getMessage().getMentionedMembers().size() == 1) {
            Member target = e.getMessage().getMentionedMembers().get(0);
            HashMap<String, Pair<Double, String>> records = dataHandler.getRecords(e.getGuild().getIdLong(), target.getIdLong());
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(String.format("%s's Records", target.getUser().getName()));
            StringBuilder sb = new StringBuilder();
            for (String record : records.keySet()) {
                Pair<Double, String> v = records.get(record);

                sb.append(":small_blue_diamond: ").append(properties.getProperty(record))
                        .append(": **");

                boolean isInt = dataHandler.isInt(record);
                // Formats the blackjack winrate into something more human readable
                sb.append( isInt ? v.getLeft().intValue() : String.format("%.2f%s", v.getLeft() * 100, "%"));


                sb.append("** ");
                if (v.getRight() != null) {
                    sb.append(" [jump](").append(v.getRight()).append(")");
                }
                sb.append("\n");
            }
            eb.setDescription(sb.toString());
            e.getChannel().sendMessage(eb.build()).queue();
        }
    }
}
