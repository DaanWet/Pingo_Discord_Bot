package companions.paginators;

import companions.Record;
import data.handlers.RecordDataHandler;
import data.models.RecordData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.ArrayList;

public class RecordPaginator extends EmbedPaginator {

    private final Record record;
    private final Long guildId;

    public RecordPaginator(Record record, Long guildId){
        this.record = record;
        this.guildId = guildId;
    }


    @Override
    public MessageEmbed createEmbed(long guild){
        RecordDataHandler dataHandler = new RecordDataHandler();
        EmbedBuilder eb = new EmbedBuilder();
        ArrayList<RecordData> records = guildId == null ? dataHandler.getRecords(record) : dataHandler.getRecords(guildId, record);
        MyResourceBundle language = Utils.getLanguage(guild);
        eb.setTitle(language.getString(guildId == null ? "record.leaderboard.global" : "record.leaderboard.title", record.getDisplay(language)));
        StringBuilder sb = new StringBuilder();
        int size = records.size();
        int maxpage = ((size - 1) / 10) + 1;
        if (page == -1)
            page = maxpage;
        else
            page = Math.min(maxpage, page);
        for (int i = (page - 1) * 10; i < Math.min(size, page * 10); i++){
            RecordData v = records.get(i);
            sb.append("`").append(i + 1).append(i >= 9 ? ".`  " : ". `  ")
                    .append("<@!")
                    .append(records.get(i).getUserId())
                    .append(">  **: ")
                    .append(record.isInt() ? (int) v.getValue() : String.format("%.2f%s", v.getValue() * 100, "%"))
                    .append("** ");
            if (v.getLink() != null){
                sb.append(" [jump](").append(v.getLink()).append(")");
            }
            sb.append("\n");
        }
        eb.setDescription(sb.toString());
        eb.setFooter(language.getString("paginator.footer", page, maxpage));
        return eb.build();
    }

}
