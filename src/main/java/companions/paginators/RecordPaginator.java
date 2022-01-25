package companions.paginators;

import data.DataHandler;
import data.models.RecordData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.ArrayList;
import java.util.Properties;

public class RecordPaginator extends EmbedPaginator {

    private final String record;
    private final Long guildId;

    public RecordPaginator(String record, Long guildId){
        this.record = record;
        this.guildId = guildId;
    }


    @Override
    public MessageEmbed createEmbed(){
        DataHandler dataHandler = new DataHandler();
        EmbedBuilder eb = new EmbedBuilder();
        boolean isInt = dataHandler.isInt(record);
        ArrayList<RecordData> records = guildId == null ? dataHandler.getRecords(record) : dataHandler.getRecords(guildId, record);
        MyResourceBundle language = Utils.getLanguage(guildId);
        eb.setTitle(language.getString(guildId == null ? "record.leaderboard.global" : "record.leaderboard.title", language.getString(record.toLowerCase())));
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
                    .append(isInt ? (int) v.getValue() : String.format("%.2f%s", v.getValue() * 100, "%"))
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
