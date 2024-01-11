package me.damascus2000.pingo.commands.casino;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.commands.settings.CommandState;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.companions.DataCompanion;
import me.damascus2000.pingo.companions.Record;
import me.damascus2000.pingo.companions.paginators.RecordPaginator;
import me.damascus2000.pingo.data.handlers.RecordDataHandler;
import me.damascus2000.pingo.data.models.RecordData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyProperties;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;

import java.util.ArrayList;

public class Records extends Command {

    private final MyProperties properties;
    private final DataCompanion handler;

    public Records(DataCompanion handler){
        this.name = "records";
        this.category = Category.CASINO;
        this.description = "records.description";
        this.arguments = new String[]{"[member|record|**me**|**list**|**global**]", "<record> [**global**]"};
        properties = Utils.config;
        this.handler = handler;
        this.example = "highest_credits global";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        RecordDataHandler dataHandler = new RecordDataHandler();
        Guild guild = e.getGuild();
        MyResourceBundle language = Utils.getLanguage(guild.getIdLong());
        if (args.length == 0){
            e.getChannel().sendMessageEmbeds(getRecords(dataHandler, language, guild.getIdLong()).build()).queue();
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
                for (Record record : Record.values()){
                    sb.append(dot).append(" ").append(record.getName()).append(": ").append(record.getDisplay(language)).append("\n");
                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessageEmbeds(eb.build()).queue();
                return;
            } else if (args[0].equalsIgnoreCase("global")){
                e.getChannel().sendMessageEmbeds(getRecords(dataHandler, language, null).build()).queue();
                return;
            } else if (l != null){
                target = e.getGuild().getMemberById(l);
            } else if (e.getGuild().getMembersByNickname(args[0], true).size() > 0){
                target = e.getGuild().getMembersByNickname(args[0], true).get(0);
            } else if (guild.getMembersByName(args[0], true).size() > 0){
                target = guild.getMembersByName(args[0], true).get(0);
            }

            if (Record.getRecord(args[0].toLowerCase()).isPresent()){
                RecordPaginator recordPaginator = new RecordPaginator(Record.getRecord(args[0].toLowerCase()).get(), e.getGuild().getIdLong());
                recordPaginator.sendMessage(e.getChannel(), m -> handler.addEmbedPaginator(e.getGuild().getIdLong(), m.getIdLong(), recordPaginator));
            } else if (target != null){
                ArrayList<RecordData> records = dataHandler.getRecords(e.getGuild().getIdLong(), target.getIdLong());
                eb.setTitle(language.getString("records.person", target.getUser().getName()));
                StringBuilder sb = new StringBuilder();
                for (RecordData record : records){
                    sb.append(dot).append(" ").append(record.getRecord().getDisplay(language)).append(": **");

                    // Formats the blackjack winrate into something more human readable
                    sb.append(record.getRecord().isInt() ? (int) record.getValue() : String.format("%.2f%s", record.getValue() * 100, "%"));


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
                e.getChannel().sendMessageEmbeds(eb.build()).queue();
            } else {
                throw new MessageException(language.getString("records.error.valid", args[0]));
            }
        } else if (args.length == 2 && Record.getRecord(args[0].toLowerCase()).isPresent() && args[1].equalsIgnoreCase("global")){
            RecordPaginator recordPaginator = new RecordPaginator(Record.getRecord(args[0].toLowerCase()).get(), null);
            recordPaginator.sendMessage(e.getChannel(), m -> handler.addEmbedPaginator(e.getGuild().getIdLong(), m.getIdLong(), recordPaginator));
        } else {
            throw new MessageException(language.getString("records.error.args", getUsage(guild.getIdLong())));
        }
    }

    private EmbedBuilder getRecords(RecordDataHandler dataHandler, MyResourceBundle language, Long guildId){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(language.getString(guildId == null ? "records.global" : "records.local"));
        ArrayList<RecordData> records = guildId == null ? dataHandler.getRecords() : dataHandler.getRecords(guildId);
        StringBuilder sb = new StringBuilder();
        String dot = Utils.config.getProperty("emoji.list.dot");
        for (RecordData record : records){
            sb.append(dot).append(" ").append(record.getRecord().getDisplay(language))
                    .append(": **");

            // Formats the blackjack winrate into something more human readable
            sb.append(record.getRecord().isInt() ? (int) record.getValue() : String.format("%.2f%s", record.getValue() * 100, "%"));

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
