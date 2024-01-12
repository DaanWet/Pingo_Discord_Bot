package me.damascus2000.pingo.companions;

import me.damascus2000.pingo.companions.paginators.EmbedPaginator;
import me.damascus2000.pingo.companions.paginators.OpenExplorerData;
import me.damascus2000.pingo.utils.QueueMap;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

@Service
public class DataCompanion {

    private final HashMap<Long, QueueMap<Long, EmbedPaginator>> embedPaginatorMap;
    private final HashMap<String, OpenExplorerData> openExplorers;
    private final HashMap<String, ScheduledFuture<?>> autoClosers;


    public DataCompanion(){
        embedPaginatorMap = new HashMap<>();
        openExplorers = new HashMap<>();
        autoClosers = new HashMap<>();
    }

    public QueueMap<Long, EmbedPaginator> getEmbedPaginatorMap(long guildId){
        return embedPaginatorMap.getOrDefault(guildId, new QueueMap<>());
    }

    public void addEmbedPaginator(long guildId, long messageId, EmbedPaginator embedPaginator){
        QueueMap<Long, EmbedPaginator> l = getEmbedPaginatorMap(guildId);
        l.put(messageId, embedPaginator);
        embedPaginatorMap.put(guildId, l);
    }

    public OpenExplorerData getExplorerData(String command){
        return openExplorers.getOrDefault(command, null);
    }

    public Optional<OpenExplorerData> getExplorerData(long guildId, long channelId, long messageId){
        return openExplorers.values().stream().filter(ex -> ex.getMessageId() == messageId && ex.getChannelId() == channelId && ex.getGuildId() == guildId).findFirst();
    }

    public RestAction<?> deleteMessage(String command, Message message){
        RestAction<Message> getM = openExplorers.get(command).getMessage();
        return message.delete().flatMap(s -> getM.flatMap(Message::delete));
    }

    public void closeExplorer(String command, Message message){
        autoClosers.get(command).cancel(false);
        deleteMessage(command, message).queue();
        openExplorers.remove(command);
        autoClosers.remove(command);
    }

    public void putExplorer(String command, OpenExplorerData explorerData){
        openExplorers.put(command, explorerData);
    }

    public void putAutoCloser(String command, ScheduledFuture<?> closer){
        autoClosers.put(command, closer);
    }

}
