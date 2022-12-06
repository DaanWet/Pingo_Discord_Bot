package companions;

import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.HashMap;

public class CustomBet {

    private final int ID;
    private final HashMap<Long, Pair<Integer, String>> bets;
    private final long userId;
    private Long messageId;
    private Long channelId;
    private boolean ended;

    public CustomBet(int ID, long userId){
        this.ID = ID;
        this.userId = userId;
        bets = new HashMap<>();
        ended = false;
    }


    public HashMap<Long, Pair<Integer, String>> getBets(){
        return bets;
    }

    public Integer getBet(long userId){
        return bets.get(userId).getLeft();
    }

    public void addBet(long userId, int bet, String answer){
        bets.put(userId, Pair.of(bet, answer));
    }

    public String getAnswer(long userId){
        return bets.get(userId).getRight();
    }

    public int getID(){
        return ID;
    }

    public Long getChannelId(){
        return channelId;
    }

    public void setIds(long channelId, long messageId){
        this.channelId = channelId;
        this.messageId = messageId;
    }

    public Long getMessageId(){
        return messageId;
    }

    public boolean didBet(long userId){
        return bets.containsKey(userId);
    }

    public long getUserId(){
        return userId;
    }


    public boolean isEnded(){
        return ended;
    }

    public void end(){
        this.ended = true;
    }
}
