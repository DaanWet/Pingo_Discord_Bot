package companions;

import com.iwebpp.crypto.TweetNaclFast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;

public class VoiceCompanion {


    private final HashMap<Long, ArrayList<Long>> customChannels;
    private final HashMap<Long, HashMap<Long, ScheduledFuture<?>>> scheduled;


    public VoiceCompanion(){
        customChannels = new HashMap<>();
        scheduled = new HashMap<>();
    }


    public boolean isChannel(long guildId, long channelId){
        return getChannels(guildId).contains(channelId);
    }

    public ArrayList<Long> getChannels(long guildId){
        return customChannels.getOrDefault(guildId, new ArrayList<>());
    }

    public void addChannel(long guildId, long channelId){
        customChannels.putIfAbsent(guildId, new ArrayList<>());
        customChannels.get(guildId).add(channelId);
    }

    public boolean removeChannel(long guildId, long channelId){
        if (customChannels.containsKey(guildId)){
            return customChannels.get(guildId).remove(channelId);
        }
        return false;
    }

    public void addSchedule(long guildId, long channelId, ScheduledFuture<?> schedule){
        scheduled.putIfAbsent(guildId, new HashMap<>());
        scheduled.get(guildId).put(channelId, schedule);
    }

    public boolean removeSchedule(long guildId, long channelId){
        if (!scheduled.containsKey(guildId))
            return false;
        ScheduledFuture<?> remove = scheduled.get(guildId).remove(channelId);
        if (remove == null)
            return false;
        remove.cancel(true);
        return true;
    }
}
