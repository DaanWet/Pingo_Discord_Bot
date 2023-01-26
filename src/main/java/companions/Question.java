package companions;

import java.util.HashMap;
import java.util.Random;

public class Question<T> {



    private final int ID;
    private final HashMap<Long, T> answers;
    private final long userId;
    private String question;
    private Long messageId;
    private Long channelId;
    private Long guildId;
    private boolean ended;

    public Question(long userId, String question){
        this.ID = new Random().nextInt(1000000);
        this.userId = userId;
        this.question = question;
        this.answers = new HashMap<>();
        this.ended = false;
    }

    public void setIds(long channelId, long messageId){
        this.channelId = channelId;
        this.messageId = messageId;
    }

    public int getID(){
        return ID;
    }



    public Long getChannelId(){
        return channelId;
    }

    public Long getMessageId(){
        return messageId;
    }

    public long getUserId(){
        return userId;
    }

    public boolean isEnded(){
        return ended;
    }

    public HashMap<Long, T> getAnswers(){
        return answers;
    }

    public T getAnswer(long userId){
        return answers.get(userId);
    }
    public String getQuestion(){
        return question;
    }
    public void addAnswer(long userId, T answer){
        answers.put(userId, answer);
    }

    public boolean didAnswer(long userId){
        return answers.containsKey(userId);
    }

    public void end(){
        this.ended = true;
    }


}
