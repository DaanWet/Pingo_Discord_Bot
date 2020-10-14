package commands.casino;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import utils.DataHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class Records extends Command {

    private DataHandler dataHandler;
    private Properties properties;


    public Records(){
        this.name = "records";
        this.category = "Casino";
        this.dataHandler = new DataHandler();
        properties = new Properties();
        try {
            properties.load(Records.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        if (args.length == 0){
            HashMap<String, Triple<String, Comparable, String>> records = dataHandler.getRecords();
            if (records.size() == 0) return;
            e.getGuild().retrieveMembersByIds(records.values().stream().map(tr -> Command.isLong(tr.getLeft())).collect(Collectors.toList())).onSuccess(list -> {
                Map<String, Member> m = list.stream().collect(Collectors.toMap(Member::getId, member -> member));
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Casino Records");
                StringBuilder sb = new StringBuilder();
                for (String record : records.keySet()){
                    Triple<String, Comparable, String> v = records.get(record);
                    sb.append(":small_blue_diamond: ").append(properties.getProperty(record))
                            .append(": **")
                            .append(v.getMiddle()).append("** by ")
                            .append(m.get(v.getLeft()).getAsMention());
                    if (v.getRight() != null){
                        sb.append(" [jump](").append(v.getRight()).append(")");
                    }
                    sb.append("\n");
                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessage(eb.build()).queue();
            });
        } else if (e.getMessage().getMentionedMembers().size() == 1){
            Member target = e.getMessage().getMentionedMembers().get(0);
            HashMap<String, Pair<Comparable, String>> records = dataHandler.getRecords(target.getId());
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(String.format("%s's Records", target.getUser().getName()));
            StringBuilder sb = new StringBuilder();
            for (String record : records.keySet()){
                Pair<Comparable, String> v = records.get(record);

                sb.append(":small_blue_diamond: ").append(properties.getProperty(record))
                        .append(": **");

                // Formats the blackjack winrate into something more human readable
                if(v.getLeft() instanceof Double ||  v.getLeft() instanceof Float)
                        sb.append(String.format("%.2f", v.getLeft() * 100));
                else
                        sb.append(v.getLeft());

                sb.append("** ");
                if (v.getRight() != null){
                    sb.append(" [jump](").append(v.getRight()).append(")");
                }
                sb.append("\n");
            }
            eb.setDescription(sb.toString());
            e.getChannel().sendMessage(eb.build()).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Show all records";
    }
}
