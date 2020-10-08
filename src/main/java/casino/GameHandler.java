package casino;


import casino.uno.UnoGame;

import java.util.HashMap;

public class GameHandler {



    private HashMap<Long, BlackJackGame> blackJackGames;
    private UnoGame unoGame;


    public GameHandler(){
        blackJackGames = new HashMap<>();
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

    public UnoGame getUnoGame(){
        return unoGame;
    }

    public void removeUnoGame(){
        unoGame = null;
    }

    public void setUnoGame(UnoGame unoGame){
        this.unoGame = unoGame;
    }
}
