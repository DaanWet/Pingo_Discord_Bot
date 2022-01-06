package companions.paginators;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;

public class OpenExplorerData {

    private final String playerId;
    private final String channelId;
    private final String messageId;
    private Guild g;

    public OpenExplorerData(String playerId, String channelId, String messageId, Guild g){
        this.channelId = channelId;
        this.playerId = playerId;
        this.messageId = messageId;
        this.g = g;
    }

    public String getPlayerId(){
        return playerId;
    }

    public String getChannelId(){
        return channelId;
    }

    public String getMessageId(){
        return messageId;
    }

    public RestAction<Message> getMessage(){
        return g.getTextChannelById(channelId).retrieveMessageById(messageId);
    }
}
