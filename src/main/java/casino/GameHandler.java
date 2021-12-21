package casino;


import casino.uno.UnoGame;
import casino.uno.UnoHand;
import utils.EmbedPaginator;
import utils.QueueMap;

import java.util.ArrayList;
import java.util.HashMap;

public class GameHandler {



    private final HashMap<Long, HashMap<Long, BlackJackGame>> blackJackGames;
    private final HashMap<Long, UnoGame> unoGames;
    private final HashMap<Long, QueueMap<Long, EmbedPaginator>> embedPaginatorMap;
    private final HashMap<Long, ArrayList<CustomBet>> customBetMap;

    public GameHandler(){
        blackJackGames = new HashMap<>();
        unoGames = new HashMap<>();
        embedPaginatorMap = new HashMap<>();
        customBetMap = new HashMap<>();
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

    public QueueMap<Long, EmbedPaginator> getEmbedPaginatorMap(long guildId) {
        return embedPaginatorMap.getOrDefault(guildId, new QueueMap<>());
    }

    public void addEmbedPaginator(long guildId, long messageId, EmbedPaginator embedPaginator){
        QueueMap<Long, EmbedPaginator> l = getEmbedPaginatorMap(guildId);
        l.put(messageId, embedPaginator);
        embedPaginatorMap.put(guildId, l);
    }

    public ArrayList<CustomBet> getCustomBet(long guildId){
        return customBetMap.getOrDefault(guildId, new ArrayList<>());
    }

    public CustomBet addCustomBet(long guildId){
        ArrayList<CustomBet> b = getCustomBet(guildId);
        CustomBet c = new CustomBet(b.size() + 1);
        b.add(c);
        customBetMap.put(guildId, b);
        return c;
    }

}
