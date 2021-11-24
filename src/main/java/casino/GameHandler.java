package casino;


import casino.uno.UnoGame;
import casino.uno.UnoHand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameHandler {



    private final HashMap<Long, HashMap<Long, BlackJackGame>> blackJackGames;
    private final HashMap<Long, UnoGame> unoGames;
    private final HashMap<Long, ArrayList<Long>> recordBrowser;

    public GameHandler(){
        blackJackGames = new HashMap<>();
        unoGames = new HashMap<>();
        recordBrowser = new HashMap<>();
    }

    public BlackJackGame getBlackJackGame(long guildId, long user){
        return blackJackGames.getOrDefault(guildId, new HashMap<Long, BlackJackGame>()).getOrDefault(user, null);
    }

    public void removeBlackJackGame(long guildId, long user){
        if (blackJackGames.containsKey(guildId)){
            blackJackGames.get(guildId).remove(user);
        }
    }

    public void putBlackJackGame(long guildId, long user, BlackJackGame game){
        blackJackGames.putIfAbsent(guildId, new HashMap<>());
        blackJackGames.get(guildId).put(user, game);
    }

    public UnoGame getUnoGame(long guildId){
       return unoGames.getOrDefault(guildId, null);
    }

    public void removeUnoGame(long guildId){
        unoGames.remove(guildId);
    }

    public void setUnoGame(long guildId, UnoGame unoGame){
        unoGames.put(guildId, unoGame);
    }

    public boolean isUnoChannel(long guildId, long channelId){
        if (unoGames.containsKey(guildId)) {
            return unoGames.get(guildId).getHands().stream().map(UnoHand::getChannelId).anyMatch(id -> id == channelId);
        }
        return false;
    }

    public ArrayList<Long> getRecordBrowser(long guildId) {
        return recordBrowser.getOrDefault(guildId, new ArrayList<>());
    }

    public void addRecordBrowser(long guildId, long messageId){
        ArrayList<Long> l = getRecordBrowser(guildId);
        if (l.size() == 4){
            l.remove(0);
        }
        l.add(messageId);
        recordBrowser.put(guildId, l);
    }
}
