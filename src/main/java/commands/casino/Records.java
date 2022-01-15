package commands.casino;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import companions.GameHandler;
import companions.paginators.RecordPaginator;
import data.DataHandler;
import data.models.RecordData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Records extends Command {

    private final Properties properties;
    private final GameHandler handler;

    public Records(GameHandler handler){
        this.name = "records";
        this.category = "Casino";
        this.description = "Show all records, records for one member or one record. List all possible records using the `list` argument";
        this.arguments = "[<member>|<record>|me|list|global]\n<record> global";
        properties = new Properties();
        try {
            properties.load(Records.class.getClassLoader().getResourceAsStream("strings.properties"));
        } catch (IOException ioException){
            ioException.printStackTrace();
        }
        this.handler = handler;
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        DataHandler dataHandler = new DataHandler();
        Guild guild = e.getGuild();
        ArrayList<String> recordTypes = dataHandler.getRecordTypes();
        if (args.length == 0){
            e.getChannel().sendMessage(getRecords(dataHandler, guild.getIdLong()).build()).queue();
        } else if (args.length == 1){
            Long l = Utils.isLong(args[0]);
            Member target = null;
            EmbedBuilder eb = new EmbedBuilder();
            if (e.getMessage().getMentionedMembers().size() == 1){
                target = e.getMessage().getMentionedMembers().get(0);
            } else if (args[0].equalsIgnoreCase("me")){
                target = e.getMember();
            } else if (args[0].equalsIgnoreCase("list")){
                eb.setTitle("Records list");
                StringBuilder sb = new StringBuilder();
                for (String record : recordTypes){
                    sb.append(":small_blue_diamond: ").append(record)/*.append(": ").append(properties.getProperty(record))*/.append("\n");
                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessage(eb.build()).queue();
                return;
            } else if (args[0].equalsIgnoreCase("global")){
                e.getChannel().sendMessage(getRecords(dataHandler, null).build()).queue();
                return;
            } else if (l != null){
                target = e.getGuild().getMemberById(l);
            } else if (e.getGuild().getMembersByNickname(args[0], true).size() > 0){
                target = e.getGuild().getMembersByNickname(args[0], true).get(0);
            } else if (guild.getMembersByName(args[0], true).size() > 0){
                target = guild.getMembersByName(args[0], true).get(0);
            }

            if (recordTypes.contains(args[0].toLowerCase())){
                RecordPaginator recordPaginator = new RecordPaginator(args[0], e.getGuild().getIdLong(), properties);
                recordPaginator.sendMessage(e.getChannel(), m -> handler.addEmbedPaginator(e.getGuild().getIdLong(), m.getIdLong(), recordPaginator));
            } else if (target != null){
                ArrayList<RecordData> records = dataHandler.getRecords(e.getGuild().getIdLong(), target.getIdLong());
                eb.setTitle(String.format("%s's Records", target.getUser().getName()));
                StringBuilder sb = new StringBuilder();
                for (RecordData record : records){

                    sb.append(":small_blue_diamond: ").append(properties.getProperty(record.getRecord()))
                            .append(": **");

                    boolean isInt = dataHandler.isInt(record.getRecord());
                    // Formats the blackjack winrate into something more human readable
                    sb.append(isInt ? (int) record.getValue() : String.format("%.2f%s", record.getValue() * 100, "%"));


                    sb.append("** ");
                    if (record.getLink() != null){
                        sb.append(" [jump](").append(record.getLink()).append(")");
                    }
                    sb.append("\n");
                }
                if (records.size() == 0){
                    sb.append("No records yet for ").append(target.getUser().getName());
                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessage(eb.build()).queue();
            } else {
                throw new MessageException(String.format("%s is not a valid member name or record name", args[0]));
            }
        } else if (args.length == 2 && recordTypes.contains(args[0].toLowerCase()) && args[1].equalsIgnoreCase("global")){
            RecordPaginator recordPaginator = new RecordPaginator(args[0], null, properties);
            recordPaginator.sendMessage(e.getChannel(), m -> handler.addEmbedPaginator(e.getGuild().getIdLong(), m.getIdLong(), recordPaginator));
        } else {
            throw new MessageException(String.format("This commands takes max 2 optional arguments. \n%s\n If the name of the member consists of multiple words, put it between quotes for it to be recognised as a name.", getUsage()));
        }
    }

    private EmbedBuilder getRecords(DataHandler dataHandler, Long guildId){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(guildId == null ? "Global Casino Records" : "Casino Records");
        ArrayList<RecordData> records = guildId == null ? dataHandler.getRecords() : dataHandler.getRecords(guildId);
        StringBuilder sb = new StringBuilder();
        for (RecordData record : records){
            sb.append(":small_blue_diamond: ").append(properties.getProperty(record.getRecord()))
                    .append(": **");
            boolean isInt = dataHandler.isInt(record.getRecord());
            // Formats the blackjack winrate into something more human readable
            sb.append(isInt ? (int) record.getValue() : String.format("%.2f%s", record.getValue() * 100, "%"));

            sb.append("** by <@!")
                    .append(record.getUserId()).append(">");
            if (record.getLink() != null){
                sb.append(" [jump](").append(record.getLink()).append(")");
            }
            sb.append("\n");
        }
        eb.setDescription(sb.toString());
        if (records.size() == 0)
            eb.setDescription("There are no records yet, claim credits and play a game to start the records");
        return eb;
    }
}
