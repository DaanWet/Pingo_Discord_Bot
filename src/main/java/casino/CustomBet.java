package casino;

import java.util.HashMap;

public class CustomBet {

    private Long messageId;
    private Long channelId;
    private int ID;
    private HashMap<Long, Integer> bets;

    public CustomBet(int ID){
        this.ID = ID;
    }



    public HashMap<Long, Integer> getBets(){
        return bets;
    }

    public Integer getBet(long userId){
        return bets.get(userId);
    }

    public void addBet(long userId, int bet){
        bets.put(userId, bet);
    }


    public int getID() {
        return ID;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setIds(long channelId, long messageId) {
        this.channelId = channelId;
        this.messageId = messageId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public boolean didBet(long userId){
        return bets.containsKey(userId);
    }

}
