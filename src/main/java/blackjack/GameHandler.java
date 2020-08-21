package blackjack;


import java.util.HashMap;

public class GameHandler {



    private HashMap<Long, BlackJackGame> blackJackGames;


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
}
