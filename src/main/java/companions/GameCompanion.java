package companions;


import companions.cardgames.BlackJackGame;
import companions.uno.UnoGame;
import companions.uno.UnoHand;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class GameCompanion {


    private final HashMap<Long, HashMap<Long, BlackJackGame>> blackJackGames;
    private final HashMap<Long, UnoGame> unoGames;
    private final HashMap<Long, HashMap<Integer, Question<Pair<Integer, String>>>> customBetMap;
    private final HashMap<Integer, Question<String>> blackboxes;

    public GameCompanion(){
        blackJackGames = new HashMap<>();
        unoGames = new HashMap<>();
        customBetMap = new HashMap<>();
        blackboxes = new HashMap<>();
    }

    public BlackJackGame getBlackJackGame(long guildId, long user){
        return blackJackGames.getOrDefault(guildId, new HashMap<>()).getOrDefault(user, null);
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
        if (unoGames.containsKey(guildId)){
            return unoGames.get(guildId).getHands().stream().map(UnoHand::getChannelId).anyMatch(id -> id == channelId);
        }
        return false;
    }

    public HashMap<Integer, Question<Pair<Integer, String>>> getCustomBets(long guildId){
        return customBetMap.getOrDefault(guildId, new HashMap<>());
    }

    public Question<Pair<Integer, String>> getCustomBet(long guildId, int id){
        return getCustomBets(guildId).get(id);
    }

    public Question<Pair<Integer, String>> addCustomBet(long guildId, long userId, String question){
        HashMap<Integer, Question<Pair<Integer, String>>> bets = getCustomBets(guildId);
        Question<Pair<Integer, String>> q = new Question<>(userId, question);
        while (bets.containsKey(q.getID())){
            q = new Question<>(userId, question); // ensure unique key
        }
        bets.put(q.getID(), q);
        customBetMap.put(guildId, bets);
        return q;
    }


    public Question<String> getBlackBox(int id){
        return blackboxes.get(id);
    }
    public Question<String> addBlackBox(long guildId, long userId, String question){
        Question<String> q = new Question<>(userId, question);
        while (blackboxes.containsKey(q.getID())){
            q = new Question<>(userId, question); // ensure unique key
        }
        blackboxes.put(q.getID(), q);
        return q;
    }



}
