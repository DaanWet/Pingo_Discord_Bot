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
import utils.MyResourceBundle;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Records extends Command {

    private final Properties properties;
    private final GameHandler handler;

    public Records(GameHandler handler){
        this.name = "records";
        this.category = Category.CASINO;
        this.description = "records.description";
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
        MyResourceBundle language = Utils.getLanguage(guild.getIdLong());
        if (args.length == 0){
            e.getChannel().sendMessage(getRecords(dataHandler, language, guild.getIdLong()).build()).queue();
        } else if (args.length == 1){
            Long l = Utils.isLong(args[0]);
            Member target = null;
            EmbedBuilder eb = new EmbedBuilder();
            String dot = Utils.config.getProperty("emoji.list.dot");
            if (e.getMessage().getMentionedMembers().size() == 1){
                target = e.getMessage().getMentionedMembers().get(0);
            } else if (args[0].equalsIgnoreCase("me")){
                target = e.getMember();
            } else if (args[0].equalsIgnoreCase("list")){
                eb.setTitle(language.getString("records.list"));
                StringBuilder sb = new StringBuilder();
                for (String record : recordTypes){
                    sb.append(dot).append(" ").append(record)/*.append(": ").append(properties.getProperty(record))*/.append("\n");
                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessage(eb.build()).queue();
                return;
            } else if (args[0].equalsIgnoreCase("global")){
                e.getChannel().sendMessage(getRecords(dataHandler, language, null).build()).queue();
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
                eb.setTitle(language.getString("records.person", target.getUser().getName()));
                StringBuilder sb = new StringBuilder();
                for (RecordData record : records){

                    sb.append(dot).append(" ").append(properties.getProperty(record.getRecord()))
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
                    sb.append(language.getString("records.no_records.person", target.getUser().getName()));
                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessage(eb.build()).queue();
            } else {
                throw new MessageException(language.getString("records.error.valid", args[0]));
            }
        } else if (args.length == 2 && recordTypes.contains(args[0].toLowerCase()) && args[1].equalsIgnoreCase("global")){
            RecordPaginator recordPaginator = new RecordPaginator(args[0], null, properties);
            recordPaginator.sendMessage(e.getChannel(), m -> handler.addEmbedPaginator(e.getGuild().getIdLong(), m.getIdLong(), recordPaginator));
        } else {
            throw new MessageException(language.getString("records.error.args", getUsage()));
        }
    }

    private EmbedBuilder getRecords(DataHandler dataHandler, MyResourceBundle language, Long guildId){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(language.getString(guildId == null ? "records.global" : "records.local"));
        ArrayList<RecordData> records = guildId == null ? dataHandler.getRecords() : dataHandler.getRecords(guildId);
        StringBuilder sb = new StringBuilder();
        String dot = Utils.config.getProperty("emoji.list.dot");
        for (RecordData record : records){
            sb.append(dot).append(" ").append(properties.getProperty(record.getRecord()))
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
            eb.setDescription(language.getString("records.no_records"));
        return eb;
    }
}
