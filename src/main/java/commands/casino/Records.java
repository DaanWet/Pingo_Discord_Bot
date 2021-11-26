package commands.casino;

import casino.GameHandler;
import casino.RecordPaginator;
import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
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

public class Records extends Command {

    private Properties properties;
    private GameHandler handler;

    public Records(GameHandler handler) {
        this.name = "records";
        this.category = "Casino";
        this.description = "Show all records, records for one member or one record. List all possible records using the `list` argument";
        this.arguments = "[<member>|<record>|list]";
        properties = new Properties();
        try {
            properties.load(Records.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        this.handler = handler;
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        DataHandler dataHandler = new DataHandler();
        Guild guild = e.getGuild();
        if (args.length == 0) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Casino Records");
            HashMap<String, Triple<Long, Double, String>> records = dataHandler.getRecords(e.getGuild().getIdLong());
            StringBuilder sb = new StringBuilder();
            for (String record : records.keySet()) {
                Triple<Long, Double, String> v = records.get(record);
                sb.append(":small_blue_diamond: ").append(properties.getProperty(record))
                        .append(": **");
                boolean isInt = dataHandler.isInt(record);
                // Formats the blackjack winrate into something more human readable
                sb.append(isInt ? v.getMiddle().intValue() : String.format("%.2f%s", v.getMiddle() * 100, "%"));

                sb.append("** by <@!")
                        .append(v.getLeft()).append(">");
                if (v.getRight() != null) {
                    sb.append(" [jump](").append(v.getRight()).append(")");
                }
                sb.append("\n");
            }
            eb.setDescription(sb.toString());
            e.getChannel().sendMessage(eb.build()).queue();
            if (records.size() == 0) {
                eb.setDescription("There are no records yet, claim credits and play a game to start the records");
                e.getChannel().sendMessage(eb.build()).queue();
            }
        } else if (args.length == 1) {
            Long l = Utils.isLong(args[0]);
            Member target = null;
            EmbedBuilder eb = new EmbedBuilder();
            ArrayList<String> recordTypes = dataHandler.getRecordTypes();
            if (e.getMessage().getMentionedMembers().size() == 1) {
                target = e.getMessage().getMentionedMembers().get(0);
            } else if (args[0].equalsIgnoreCase("me")) {
                target = e.getMember();
            } else if (args[0].equalsIgnoreCase("list")) {
                eb.setTitle("Records list");
                StringBuilder sb = new StringBuilder();
                for (String record : recordTypes) {
                    sb.append(":small_blue_diamond: ").append(record)/*.append(": ").append(properties.getProperty(record))*/.append("\n");
                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessage(eb.build()).queue();
            } else if (l != null) {
                target = e.getGuild().getMemberById(l);
            } else if (e.getGuild().getMembersByNickname(args[0], true).size() > 0) {
                target = e.getGuild().getMembersByNickname(args[0], true).get(0);
            } else if (guild.getMembersByName(args[0], true).size() > 0) {
                target = guild.getMembersByName(args[0], true).get(0);
            }

            if (recordTypes.contains(args[0].toLowerCase())) {
                RecordPaginator recordPaginator = new RecordPaginator(args[0], e.getGuild().getIdLong());
                recordPaginator.sendMessage(e.getChannel(), m -> handler.addEmbedPaginator(e.getGuild().getIdLong(), m.getIdLong(), recordPaginator));
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
                if (records.size() == 0) {
                    sb.append("No records yet for ").append(target.getUser().getName());
                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessage(eb.build()).queue();
            } else {
                e.getChannel().sendMessage(String.format("%s is not a valid member name or record name", args[0])).queue();
            }
        } else {
            e.getChannel().sendMessage(String.format("This commands takes only 1 optional argument. \n%s\n If the name of the member consists of multiple words, put it between quotes for it to be recognised as a name.", getUsage())).queue();
        }
    }
}
