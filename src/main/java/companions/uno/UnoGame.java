package companions.uno;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static java.lang.Math.min;

public class UnoGame {

    private final String PATH = "https://pingo.wettinck.be/uno/";

    private final ArrayList<UnoHand> hands;
    private final ArrayList<UnoCard> trekstapel;
    private final ArrayList<UnoCard> aflegstapel;
    private final Random random = new Random();
    private final int bet;
    private final long channelID;
    private final long starter;
    private int turn;
    private boolean clockwise;
    private boolean finished;
    private long messageID;
    private long category;

    public UnoGame(int bet, long starter, long channelID){
        trekstapel = new ArrayList<>();
        for (UnoCard.Value value : UnoCard.Value.values()){
            for (UnoCard.Color color : UnoCard.Color.values()){
                trekstapel.add(new UnoCard(color, value));
                if (value != UnoCard.Value.ZERO && value != UnoCard.Value.PLUSFOUR && value != UnoCard.Value.WILD){
                    trekstapel.add(new UnoCard(color, value));
                }
            }
        }
        Collections.shuffle(trekstapel);
        aflegstapel = new ArrayList<>();
        turn = -1;
        this.bet = bet;
        clockwise = true;
        finished = false;
        this.starter = starter;
        this.channelID = channelID;
        hands = new ArrayList<>();
    }

    public void addPlayer(long id, String name){
        UnoHand hand = new UnoHand(id, name);
        for (int i = 0; i < 7; i++){
            hand.addCard(trekstapel.remove(0), false);
        }
        hands.add(hand);
    }

    public int start(){
        if (hands.size() >= 2){
            UnoCard beginCard = trekstapel.remove(0);
            if (beginCard.getValue() == UnoCard.Value.PLUSFOUR || beginCard.getValue() == UnoCard.Value.WILD){
                trekstapel.add(beginCard);
                Collections.shuffle(trekstapel);
                return start();
            }
            aflegstapel.add(beginCard);
            turn = random.nextInt(hands.size());
        }
        return turn;
    }

    public boolean canPlay(UnoCard card){
        return hands.get(turn).canPlay(card) && getTopCard().canBePlayed(card);
    }

    public boolean canPlayDrawFour(int turn){
        UnoCard topcard = aflegstapel.get(aflegstapel.size() - 2);
        boolean canPlay = true;
        int i = 0;
        ArrayList<UnoCard> cards = hands.get(turn).getCards();
        while (canPlay && i < cards.size()){
            UnoCard card = cards.get(i);
            if (card.getColor() == topcard.getColor() && card.getValue() != UnoCard.Value.PLUSFOUR && card.getValue() != UnoCard.Value.WILD){
                canPlay = false;
            }
            i++;
        }
        return canPlay;
    }

    public boolean playCard(UnoCard card){
        if (finished || !canPlay(card)){
            return false;
        }
        UnoHand hand = hands.get(turn);
        hand.endTurn(card);
        aflegstapel.add(card);
        if (hand.getCards().size() == 0){
            finished = true;
            return true;
        }
        switch (card.getValue()){
            case REVERSE -> {
                clockwise = !clockwise;
                nextTurn(hands.size() == 2);
            }
            case SKIP -> nextTurn(true);
            case PLUSTWO -> {
                nextTurn(false);
                hand = hands.get(turn);
                if (trekstapel.size() == 0) reshuffle();
                hand.addCard(trekstapel.remove(0), false);
                if (trekstapel.size() == 0) reshuffle();
                hand.addCard(trekstapel.remove(0), false);
                nextTurn(false);
            }
            case PLUSFOUR -> {
                nextTurn(false);
                hand = hands.get(turn);
                for (int i = 0; i < 4; i++){
                    if (trekstapel.size() == 0) reshuffle();
                    hand.addCard(trekstapel.remove(0), false);
                }
                nextTurn(false);
            }
            default -> nextTurn(false);
        }
        return true;

    }

    public UnoCard getNextCard(){
        if (trekstapel.size() == 0) reshuffle();
        return trekstapel.remove(0);
    }

    public UnoCard drawCard(){
        UnoCard card = getNextCard();
        hands.get(turn).addCard(card, true);
        return card;
    }

    public void nextTurn(boolean extra){
        int amount = extra ? 2 : 1;
        turn = calculateNextTurn(amount);
    }

    public int calculateNextTurn(int step){
        int temp = (turn + (clockwise ? step : -step)) % hands.size();
        if (temp < 0) temp += hands.size();
        return temp;
    }

    public void reshuffle(){
        UnoCard topcard = aflegstapel.remove(aflegstapel.size() - 1);
        trekstapel.addAll(aflegstapel);
        aflegstapel.clear();
        aflegstapel.add(topcard);
        Collections.shuffle(trekstapel);
    }

    public long getStarter(){
        return starter;
    }

    public long getCategory(){
        return category;
    }

    public void setCategory(long category){
        this.category = category;
    }

    public long getMessageID(){
        return messageID;
    }

    public void setMessageID(long messageID){
        this.messageID = messageID;
    }

    public boolean isFinished(){
        return finished;
    }

    public UnoCard getTopCard(){
        return aflegstapel.get(aflegstapel.size() - 1);
    }

    public int getTurn(){
        return turn;
    }

    public int getBet(){
        return bet;
    }

    public boolean isClockwise(){
        return clockwise;
    }

    public ArrayList<UnoHand> getHands(){
        return hands;
    }

    public ArrayList<UnoCard> getTrekstapel(){
        return trekstapel;
    }

    public UnoHand getPlayerHand(long id){
        UnoHand hand = null;
        int i = 0;
        while (hand == null && i < hands.size()){
            if (hands.get(i).getPlayerId() == id)
                hand = hands.get(i);
            i++;
        }
        return hand;
    }


    public EmbedBuilder createEmbed(long player){
        EmbedBuilder eb = new EmbedBuilder();
        UnoHand hand = hands.get(turn);
        if (player == hand.getPlayerId()){
            eb.setTitle("It's your turn");
        } else {
            eb.setTitle(String.format("It's %s's turn", hand.getPlayerName()));
        }
        StringBuilder sb = new StringBuilder();
        for (UnoCard c : getPlayerHand(player).getCards()){
            if (c.getValue() == UnoCard.Value.WILD || c.getValue() == UnoCard.Value.PLUSFOUR){
                sb.append(c.getValue().getName()).append(", ");
            } else {
                sb.append(c).append(", ");
            }

        }
        sb.delete(sb.length() - 2, sb.length());
        eb.addField("Current Card", getTopCard().toString(), false);
        eb.addField("Your cards", sb.toString(), false);

        StringBuilder names = new StringBuilder();
        StringBuilder cards = new StringBuilder();
        names.append("Order: ").append(clockwise ? ":arrow_forward:\n" : ":arrow_backward:\n");
        for (int i = 0; i < hands.size(); i++){
            hand = hands.get(i);
            String name = hand.getPlayerName();
            names.append(name, 0, min(name.length(), 5));
            names.append(name.length() > 5 ? "." : "");
            cards.append("   ").append(hand.getCards().size()).append("   ");
            if (hand.getPlayerId() == player){
                names.append("(You)");
                cards.append("    ");
            } else if (i == turn){
                names.append("(Now)");
                cards.append("    ");
            }
            names.append(" ");
        }
        names.append("\n").append(cards);
        eb.addField("Other players' cards", names.toString(), false);
        eb.setImage("attachment://hand.png");
        eb.setThumbnail(String.format("%s%s%s.png", PATH, getTopCard().getColor().getToken(), getTopCard().getValue().getToken()));
        return eb;
    }

    public long getChannelID(){
        return channelID;
    }
}
