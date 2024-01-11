package me.damascus2000.pingo.companions.paginators;

import me.damascus2000.pingo.data.handlers.GeneralDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XPPaginator extends EmbedPaginator{

    private final boolean global;
    private final long guildId;

    public XPPaginator(boolean global, long guildId){
        this.global = global;
        this.guildId = guildId;
    }



    @Override
    public MessageEmbed createEmbed(long guild){
        GeneralDataHandler handler = new GeneralDataHandler();
        HashMap<Long, Integer> map = global ? handler.getAllXp() : handler.getAllXp(guildId);
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
        MyResourceBundle language = Utils.getLanguage(guild);
        eb.setTitle(language.getString(global ? "leaderboard.xp.global" : "leaderboard.xp.title"));
        for (int i = (page - 1) * 10; i < Math.min(size, page * 10); i++){
            sb.append("`").append(i + 1).append(i >= 9 ? ".`  " : ". `  ")
                    .append("<@!")
                    .append(sorted.get(i).getKey())
                    .append(">  **: ").append(sorted.get(i).getValue()).append("xp** (lvl ")
                    .append(Utils.getLevel(sorted.get(i).getValue())).append(")\n");

        }
        eb.setDescription(sb.toString());
        if (sorted.size() == 0)
            eb.setDescription(language.getString("leaderboard.xp.error"));
        else
            eb.setFooter(language.getString("paginator.footer", page, maxpage));
        return eb.build();
    }
}
