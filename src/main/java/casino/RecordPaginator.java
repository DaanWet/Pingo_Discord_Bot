package casino;

import commands.casino.Records;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.DataHandler;
import utils.EmbedPaginator;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RecordPaginator extends EmbedPaginator {

    private final String record;
    private final long guildId;
    private final Properties properties;

    public RecordPaginator(String record, long guildId, Properties properties) {
        this.record = record;
        this.guildId = guildId;
        this.properties = properties;
    }


    @Override
    public MessageEmbed createEmbed() {
        DataHandler dataHandler = new DataHandler();
        EmbedBuilder eb = new EmbedBuilder();
        boolean isInt = dataHandler.isInt(record);
        HashMap<Long, Pair<Double, String>> records = dataHandler.getRecords(guildId, record);
        List<Map.Entry<Long, Pair<Double, String>>> sorted = records.entrySet().stream().sorted(Comparator.comparingDouble(x -> -x.getValue().getLeft())).collect(Collectors.toList());
        eb.setTitle(String.format("%s leaderboard", properties.getProperty(record.toLowerCase())));
        StringBuilder sb = new StringBuilder();
        int size = sorted.size();
        int maxpage = ((size - 1) / 10) + 1;
        if (page == -1)
            page = maxpage;
        else
            page = Math.min(maxpage, page);


        for (int i = (page - 1) * 10; i < Math.min(size, page * 10); i++) {
            Pair<Double, String> v = sorted.get(i).getValue();
            sb.append("`").append(i + 1).append(i == 9 ? ".`" : ". `  ")
                    .append("<@!")
                    .append(sorted.get(i).getKey())
                    .append(">  **: ")
                    .append(isInt ? v.getLeft().intValue() : String.format("%.2f%s", v.getLeft() * 100, "%"))
                    .append("** ");
            if (v.getRight() != null) {
                sb.append(" [jump](").append(v.getRight()).append(")");
            }
            sb.append("\n");
        }
        eb.setDescription(sb.toString());
        eb.setFooter(String.format("Page %d/%d", page, maxpage));
        return eb.build();
    }

}
