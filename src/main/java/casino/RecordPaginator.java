package casino;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import utils.DataHandler;
import utils.EmbedPaginator;
import utils.dbdata.RecordData;

import java.util.*;

public class RecordPaginator extends EmbedPaginator {

    private final String record;
    private final Long guildId;
    private final Properties properties;

    public RecordPaginator(String record, Long guildId, Properties properties) {
        this.record = record;
        this.guildId = guildId;
        this.properties = properties;
    }


    @Override
    public MessageEmbed createEmbed() {
        DataHandler dataHandler = new DataHandler();
        EmbedBuilder eb = new EmbedBuilder();
        boolean isInt = dataHandler.isInt(record);
        ArrayList<RecordData> records = guildId == null ? dataHandler.getRecords(record) : dataHandler.getRecords(guildId, record);
        eb.setTitle(String.format("%s%s leaderboard", guildId == null ? "Global " : "", properties.getProperty(record.toLowerCase())));
        StringBuilder sb = new StringBuilder();
        int size = records.size();
        int maxpage = ((size - 1) / 10) + 1;
        if (page == -1)
            page = maxpage;
        else
            page = Math.min(maxpage, page);
        for (int i = (page - 1) * 10; i < Math.min(size, page * 10); i++) {
            RecordData v = records.get(i);
            sb.append("`").append(i + 1).append(i >= 9 ? ".`  " : ". `  ")
                    .append("<@!")
                    .append(records.get(i).getUserId())
                    .append(">  **: ")
                    .append(isInt ? (int) v.getValue() : String.format("%.2f%s", v.getValue() * 100, "%"))
                    .append("** ");
            if (v.getLink() != null) {
                sb.append(" [jump](").append(v.getLink()).append(")");
            }
            sb.append("\n");
        }
        eb.setDescription(sb.toString());
        eb.setFooter(String.format("Page %d/%d", page, maxpage));
        return eb.build();
    }

}
