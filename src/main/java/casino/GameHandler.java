package casino;


import casino.uno.UnoGame;

import java.util.HashMap;

public class GameHandler {



    private HashMap<Long, BlackJackGame> blackJackGames;
    private HashMap<Long, UnoGame> unoGames;


    public GameHandler(){
        blackJackGames = new HashMap<>();
        unoGames = new HashMap<>();
    }

    public BlackJackGame getBlackJackGame(long user){
        if (blackJackGames.containsKey(user)) {
            return blackJackGames.get(user);
        }
        return null;
    }

    public void removeBlackJackGame(long user){
        blackJackGames.remove(user);
    }

    public void putBlackJackGame(long user, BlackJackGame game){
        blackJackGames.put(user, game);
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
}
