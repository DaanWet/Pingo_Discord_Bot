package companions.uno;



import java.time.LocalDateTime;
import java.util.ArrayList;

public class UnoHand {

    private final ArrayList<UnoCard> cards;
    private final long playerId;
    private final String playerName;
    private long channelId;
    private long messageId;
    private boolean drawn;

    private LocalDateTime lastPingOrPlay;


    public UnoHand(long playerId, String playerName){
        this.playerId = playerId;
        this.playerName = playerName;
        cards = new ArrayList<>();
        drawn = false;
        channelId = -1;
        messageId = -1;
        this.lastPingOrPlay = LocalDateTime.now();
    }


    public boolean canPlay(UnoCard unoCard){
        return ((!drawn && cards.contains(unoCard)) || (drawn && cards.get(cards.size() - 1).equals(unoCard)));
    }

    public void addCard(UnoCard card, boolean drawn){
        cards.add(card);
        this.drawn = drawn;
    }

    public void endTurn(UnoCard card){
        if (card != null){
            cards.remove(card);
        }
        drawn = false;
    }

    public void setPingOrPlay(){
        this.lastPingOrPlay = LocalDateTime.now();
    }

    public LocalDateTime getLastPingOrPlay(){
        return this.lastPingOrPlay;
    }

    public ArrayList<UnoCard> getCards(){
        return cards;
    }

    public long getChannelId(){
        return channelId;
    }

    public void setChannelId(long channelId){
        this.channelId = channelId;
    }

    public long getMessageId(){
        return messageId;
    }

    public void setMessageId(long messageId){
        this.messageId = messageId;
    }

    public long getPlayerId(){
        return playerId;
    }

    public String getPlayerName(){
        return playerName;
    }
}
