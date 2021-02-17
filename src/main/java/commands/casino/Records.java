package commands.casino;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import utils.DataHandler;
import utils.Utils;

import java.io.IOException;
import java.util.*;
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
        Guild guild = e.getGuild();
        if (args.length == 0) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Casino Records");
            HashMap<String, Triple<Long, Double, String>> records = dataHandler.getRecords(e.getGuild().getIdLong());
            e.getGuild().retrieveMembersByIds(records.values().stream().map(Triple::getLeft).collect(Collectors.toSet())).onSuccess(list -> {
                Map<Long, Member> m = list.stream().collect(Collectors.toMap(Member::getIdLong, member -> member));
                StringBuilder sb = new StringBuilder();
                for (String record : records.keySet()) {
                    Triple<Long, Double, String> v = records.get(record);
                    sb.append(":small_blue_diamond: ").append(properties.getProperty(record))
                            .append(": **");
                    boolean isInt = dataHandler.isInt(record);
                    // Formats the blackjack winrate into something more human readable
                    sb.append(isInt ? v.getMiddle().intValue() : String.format("%.2f%s", v.getMiddle() * 100, "%"));

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
            if (records.size() == 0) {
                eb.setDescription("There are no records yet, claim credits and play a game to start the records");
                e.getChannel().sendMessage(eb.build()).queue();
            }
        } else if (args.length == 1) {
            Long l = Utils.isLong(args[0]);
            Member target = null;
            EmbedBuilder eb = new EmbedBuilder();
            if (e.getMessage().getMentionedMembers().size() == 1) {
                target = e.getMessage().getMentionedMembers().get(0);
            } else if (args[0].equalsIgnoreCase("me")) {
                target = e.getMember();
            } else if (args[0].equalsIgnoreCase("list")){

            } else if (l != null) {
                target = e.getGuild().getMemberById(l);
            } else if (e.getGuild().getMembersByNickname(args[0], true).size() > 0) {
                target = e.getGuild().getMembersByNickname(args[0], true).get(0);
            } else if(guild.getMembersByName(args[0], true).size() > 0){
                target = guild.getMembersByName(args[0], true).get(0);
            }
            ArrayList<String> recordTypes = dataHandler.getRecordTypes();
            if (recordTypes.contains(args[0].toLowerCase())){
                boolean isInt = dataHandler.isInt(args[0]);
                HashMap<Long, Pair<Double, String>> records = dataHandler.getRecords(e.getGuild().getIdLong(), args[0]);
                List<Map.Entry<Long, Pair<Double, String>>> sorted = records.entrySet().stream().sorted(Comparator.comparingDouble(x -> -x.getValue().getLeft())).limit(10).collect(Collectors.toList());
                eb.setTitle(String.format("%s leaderboard", properties.getProperty(args[0].toLowerCase())));
                e.getGuild().retrieveMembersByIds(sorted.stream().map(Map.Entry::getKey).collect(Collectors.toList())).onSuccess(list -> {
                    Map<Long, Member> m = list.stream().collect(Collectors.toMap(Member::getIdLong, member -> member));
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < sorted.size() && i < 10; i++) {
                        Pair<Double, String> v = sorted.get(i).getValue();
                        sb.append("`").append(i+1).append(i == 9 ? ".`" : ". `  ")
                                .append(m.get(sorted.get(i).getKey()).getAsMention())
                                .append("  **: ")
                                .append(isInt ? v.getLeft().intValue() : String.format("%.2f%s", v.getLeft() * 100, "%"))
                                .append("** ");
                        if (v.getRight() != null) {
                            sb.append(" [jump](").append(v.getRight()).append(")");
                        }
                        sb.append("\n");
                    }
                    eb.setDescription(sb.toString());
                    e.getChannel().sendMessage(eb.build()).queue();
                });
            } else if (args[0].equalsIgnoreCase("list")){
                eb.setTitle("Records list");
                StringBuilder sb = new StringBuilder();
                for (String record : recordTypes){
                    sb.append(":small_blue_diamond: ").append(record)/*.append(": ").append(properties.getProperty(record))*/.append("\n");
                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessage(eb.build()).queue();
            } else if (target != null) {
                HashMap<String, Pair<Double, String>> records = dataHandler.getRecords(e.getGuild().getIdLong(), target.getIdLong());
                eb.setTitle(String.format("%s's Records", target.getUser().getName()));
                StringBuilder sb = new StringBuilder();
                for (String record : records.keySet()) {
                    Pair<Double, String> v = records.get(record);

                    sb.append(":small_blue_diamond: ").append(properties.getProperty(record))
                            .append(": **");

                    boolean isInt = dataHandler.isInt(record);
                    // Formats the blackjack winrate into something more human readable
                    sb.append(isInt ? v.getLeft().intValue() : String.format("%.2f%s", v.getLeft() * 100, "%"));


                    sb.append("** ");
                    if (v.getRight() != null) {
                        sb.append(" [jump](").append(v.getRight()).append(")");
                    }
                    sb.append("\n");
                }
                if (records.size() == 0){
                    sb.append("No records yet for ").append(target.getUser().getName());
                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessage(eb.build()).queue();
            } else {
                //TODO: show help message
            }
        }
    }

}
