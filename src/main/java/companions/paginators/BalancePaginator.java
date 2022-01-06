package companions.paginators;

import data.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BalancePaginator extends EmbedPaginator {

    private final boolean global;
    private final long guildId;

    public BalancePaginator(boolean global, long guildId){
        this.global = global;
        this.guildId = guildId;
    }


    @Override
    public MessageEmbed createEmbed(){
        DataHandler dataHandler = new DataHandler();
        HashMap<Long, Integer> map = global ? dataHandler.getAllCredits() : dataHandler.getAllCredits(guildId);
        Stream<Map.Entry<Long, Integer>> stream = map.entrySet().stream().sorted((entry1, entry2) -> entry2.getValue() - entry1.getValue());
        List<Map.Entry<Long, Integer>> sorted = stream.collect(Collectors.toList());
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder sb = new StringBuilder();
        int size = sorted.size();
        int maxpage = ((size - 1) / 10) + 1;
        if (page == -1)
            page = maxpage;
        else
            page = Math.min(maxpage, page);

        eb.setTitle(global ? "Global leaderboard" : "Leaderboard");
        for (int i = (page - 1) * 10; i < Math.min(size, page * 10); i++){
            sb.append("`").append(i + 1).append(i >= 9 ? ".`  " : ". `  ")
                    .append("<@!")
                    .append(sorted.get(i).getKey())
                    .append(">  **: ").append(sorted.get(i).getValue()).append(" **\n");

        }
        eb.setDescription(sb.toString());
        if (sorted.size() == 0)
            eb.setDescription("No leaderboard yet, nobody has claimed credits yet.");
        else
            eb.setFooter(String.format("Page %d/%d", page, maxpage));
        return eb.build();
    }
}
