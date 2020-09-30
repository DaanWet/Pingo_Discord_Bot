package uno;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class UnoGame {

    private ArrayList<String> players;
    private ArrayList<ArrayList<UnoCard>> cards;
    private ArrayList<UnoCard> trekstapel;
    private ArrayList<UnoCard> aflegstapel;
    private int turn;
    private boolean clockwise;
    private boolean finished;
    private Random random = new Random();

    public UnoGame(){
        trekstapel = new ArrayList<>();
        for (UnoCard.Value value : UnoCard.Value.values()){
            for (UnoCard.Color color : UnoCard.Color.values()){
                trekstapel.add(new UnoCard(color, value));
                if (value != UnoCard.Value.ZERO){
                    trekstapel.add(new UnoCard(color, value));
                }
            }
        }
        Collections.shuffle(trekstapel);
        aflegstapel = new ArrayList<>();
        players = new ArrayList<>();
        turn = -1;
        clockwise = true;
        finished = false;
        cards = new ArrayList<>();                                                                                                                        
    }

    public void addPlayer(String id){
        players.add(id);
        ArrayList<UnoCard> card = new ArrayList<>();
        for (int i = 0; i < 7; i++){
            card.add(trekstapel.remove(0));
        }
        cards.add(card);
    }

    public int getTurn() {
        return turn;
    }

    public int start(){
        if (players.size() >= 2){
            UnoCard beginCard = trekstapel.remove(0);
            if (beginCard.getValue() == UnoCard.Value.PLUSFOUR){
                trekstapel.add(beginCard);
                Collections.shuffle(trekstapel);
                start();
            }
            turn = random.nextInt(players.size());
        }
        return turn;
    }

    public boolean canPlay(UnoCard card){
        ArrayList<UnoCard> cs = cards.get(turn);
        return cs.contains(card) && aflegstapel.get(aflegstapel.size() - 1).canBePlayed(card);
    }

    public boolean playCard(UnoCard card){
        ArrayList<UnoCard> cs = cards.get(turn);

        if (canPlay(card) && !finished){
            cs.remove(card);
            aflegstapel.add(card);
            switch (card.getValue()){
                case REVERSE:
                    clockwise = !clockwise;
                    nextTurn(false);
                    break;
                case SKIP:
                    nextTurn(true);
                case PLUSTWO:
                    nextTurn(false);
                    if (trekstapel.size() == 0) reshuffle();
                    cards.get(turn).add(trekstapel.remove(0));
                    if (trekstapel.size() == 0) reshuffle();
                    cards.get(turn).add(trekstapel.remove(0));
                    nextTurn(false);
                    break;
                case PLUSFOUR:
                    nextTurn(false);
                    for (int i = 0; i < 4; i++){
                        if (trekstapel.size() == 0) reshuffle();
                        cards.get(turn).add(trekstapel.remove(0));
                    }
                    nextTurn(false);
                default:
                    nextTurn(false);
                    
            }
            isFinished();
            return true;
        }
        return false;
    }

    public boolean drawCard(){
        if (trekstapel.size() == 0) reshuffle();
        UnoCard card = trekstapel.remove(0);
        cards.get(turn).add(card);
        return playCard(card);
    }

    public void nextTurn(boolean extra){
        int amount = extra ? 2 : 1;
        turn = (turn + (clockwise ? amount : -amount)) % players.size();
    }
    public void isFinished(){
        for (ArrayList<UnoCard> cs : this.cards){
            if (cs.size() == 0) {
                finished = true;
                return;
            }
        }
    }

    public UnoCard getTopCard(){
        return aflegstapel.get(aflegstapel.size() - 1);
    }

    public void reshuffle(){
        UnoCard topcard = aflegstapel.remove(aflegstapel.size() - 1);
        trekstapel.addAll(aflegstapel);
        aflegstapel.clear();
        aflegstapel.add(topcard);
        Collections.shuffle(trekstapel);
    }
}
