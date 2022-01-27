package companions.paginators;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;

public class OpenExplorerData {

    private final long playerId;
    private final long channelId;
    private final long messageId;
    private final Guild g;
    private final String command;
    private final int page;

    public OpenExplorerData(long playerId, long channelId, long messageId, Guild g, String command){
        this.channelId = channelId;
        this.playerId = playerId;
        this.messageId = messageId;
        this.g = g;
        this.command = command;
        page = 0;
    }

    public long getPlayerId(){
        return playerId;
    }

    public long getChannelId(){
        return channelId;
    }

    public long getMessageId(){
        return messageId;
    }

    public long getGuildId(){
        return g.getIdLong();
    }

    public String getCommand(){
        return command;
    }

    public int getPage(){
        return page;
    }

    public RestAction<Message> getMessage(){
        return g.getTextChannelById(channelId).retrieveMessageById(messageId);
    }
}
