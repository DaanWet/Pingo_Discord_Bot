package companions;

import companions.paginators.EmbedPaginator;
import utils.QueueMap;

import java.util.ArrayList;
import java.util.HashMap;

public class DataCompanion {

    private final HashMap<Long, QueueMap<Long, EmbedPaginator>> embedPaginatorMap;


    public DataCompanion() {
        embedPaginatorMap = new HashMap<>();
    }

    public QueueMap<Long, EmbedPaginator> getEmbedPaginatorMap(long guildId){
        return embedPaginatorMap.getOrDefault(guildId, new QueueMap<>());
    }

    public void addEmbedPaginator(long guildId, long messageId, EmbedPaginator embedPaginator){
        QueueMap<Long, EmbedPaginator> l = getEmbedPaginatorMap(guildId);
        l.put(messageId, embedPaginator);
        embedPaginatorMap.put(guildId, l);
    }





}
