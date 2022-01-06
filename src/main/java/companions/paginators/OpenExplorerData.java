package companions.paginators;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;

public class OpenExplorerData {

    private String playerId;
    private String channelid;
    private String messageId;
    private Guild g;

    public OpenExplorerData(String playerId, String channelId, String messageId, Guild g){
        this.channelid = channelId;
        this.playerId = playerId;
        this.messageId = messageId;
        this.g = g;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getChannelid() {
        return channelid;
    }

    public String getMessageId() {
        return messageId;
    }

    public RestAction<Message> getMessage(){
        return g.getTextChannelById(channelid).retrieveMessageById(messageId);
    }
}
