package me.damascus2000.pingo.companions.uno;

import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.data.handlers.SettingsDataHandler;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.sk.PrettyTable;

import java.util.*;

import static java.lang.Math.min;

public class UnoGame {

    private final String PATH;

    private final ArrayList<UnoHand> hands;
    private final ArrayList<UnoCard> drawPile;
    private final ArrayList<UnoCard> discardPile;
    private final Random random = new Random();
    private final int bet;
    private final long guildId;
    private final long channelID;
    private final long starter;
    private final int cards;
    private int turn;
    private boolean clockwise;
    private boolean finished;
    private long messageID;
    private long category;

    public UnoGame(int bet, long starter, long guildId, long channelID){
        PATH = Utils.config.getProperty("uno.url");
        drawPile = new ArrayList<>();
        for (UnoCard.Value value : UnoCard.Value.values()){
            for (UnoCard.Color color : UnoCard.Color.values()){
                drawPile.add(new UnoCard(color, value));
                if (value != UnoCard.Value.ZERO && value != UnoCard.Value.PLUSFOUR && value != UnoCard.Value.WILD){
                    drawPile.add(new UnoCard(color, value));
                }
            }
        }
        Collections.shuffle(drawPile);
        discardPile = new ArrayList<>();
        turn = -1;
        this.bet = bet;
        clockwise = true;
        finished = false;
        this.starter = starter;
        this.channelID = channelID;
        this.guildId = guildId;
        cards = new SettingsDataHandler().getIntSetting(guildId, Setting.START_CARDS).get(0);
        hands = new ArrayList<>();
    }

    public void addPlayer(long id, String name){
        UnoHand hand = new UnoHand(id, name);
        for (int i = 0; i < cards; i++){
            hand.addCard(drawPile.remove(0), false);
        }
        hands.add(hand);
    }

    public int start(){
        if (hands.size() >= 2){
            UnoCard beginCard = drawPile.remove(0);
            if (beginCard.getValue() == UnoCard.Value.PLUSFOUR || beginCard.getValue() == UnoCard.Value.WILD){
                drawPile.add(beginCard);
                Collections.shuffle(drawPile);
                return start();
            }
            discardPile.add(beginCard);
            turn = random.nextInt(hands.size());
        }
        return turn;
    }

    public boolean canPlay(UnoCard card){
        return hands.get(turn).canPlay(card) && getTopCard().canBePlayed(card);
    }

    public boolean canPlayDrawFour(int turn){
        UnoCard topcard = discardPile.get(discardPile.size() - 2);
        boolean canPlay = true;
        Iterator<UnoCard> iterator = hands.get(turn).getCards().iterator();
        while (canPlay && iterator.hasNext()){
            UnoCard card = iterator.next();
            if (card.getColor() == topcard.getColor() && card.getValue() != UnoCard.Value.PLUSFOUR && card.getValue() != UnoCard.Value.WILD){
                canPlay = false;
            }
        }
        return canPlay;
    }

    public boolean playCard(UnoCard card){
        if (finished || !canPlay(card)){
            return false;
        }
        UnoHand hand = hands.get(turn);
        hand.endTurn(card);
        discardPile.add(card);
        hand.setPingOrPlay();
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
                if (drawPile.size() == 0) reshuffle();
                hand.addCard(drawPile.remove(0), false);
                if (drawPile.size() == 0) reshuffle();
                hand.addCard(drawPile.remove(0), false);
                nextTurn(false);
            }
            case PLUSFOUR -> {
                nextTurn(false);
                hand = hands.get(turn);
                for (int i = 0; i < 4; i++){
                    if (drawPile.size() == 0) reshuffle();
                    hand.addCard(drawPile.remove(0), false);
                }
                nextTurn(false);
            }
            default -> nextTurn(false);
        }
        return true;

    }

    public UnoCard getNextCard(){
        if (drawPile.size() == 0) reshuffle();
        return drawPile.remove(0);
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
        UnoCard topcard = discardPile.remove(discardPile.size() - 1);
        drawPile.addAll(discardPile);
        discardPile.clear();
        discardPile.add(topcard);
        Collections.shuffle(drawPile);
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
        return discardPile.get(discardPile.size() - 1);
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

    public ArrayList<UnoCard> getDrawPile(){
        return drawPile;
    }

    public Optional<UnoHand> getPlayerHand(long id){
        return hands.stream().filter(h -> h.getPlayerId() == id).findFirst();

    }


    public EmbedBuilder createEmbed(long player, MyResourceBundle language){
        EmbedBuilder eb = new EmbedBuilder();
        UnoHand hand = hands.get(turn);
        if (player == hand.getPlayerId()){
            eb.setTitle(language.getString("uno.turn"));
        } else {
            eb.setTitle(language.getString("uno.his_turn", hand.getPlayerName()));
        }
        StringBuilder sb = new StringBuilder();
        for (UnoCard c : getPlayerHand(player).get().getCards()){
            if (c.getValue() == UnoCard.Value.WILD || c.getValue() == UnoCard.Value.PLUSFOUR){
                sb.append(c.getValue().getName()).append(", ");
            } else {
                sb.append(c).append(", ");
            }

        }
        sb.delete(sb.length() - 2, sb.length());
        eb.addField(language.getString("uno.current_card"), getTopCard().toString(), false);
        eb.addField(language.getString("uno.your_cards"), sb.toString(), false);


        String[] header = new String[hands.size()];
        String[] values = new String[header.length];

        for (int i = 0; i < hands.size(); i++){
            hand = hands.get(i);
            String name = hand.getPlayerName();
            name = name.substring(0, min(name.length(), 5)) + (name.length() > 5 ? "." : "");
            if (hand.getPlayerId() == player){
                name = name + "(You)";
            } else if (i == turn){
                name = name + "(Now)";
            }
            header[i] = name;
            values[i] = Integer.toString(hand.getCards().size());
        }
        PrettyTable table = new PrettyTable(header);
        table.addRow(values);
        String otherCards = String.format("%s %s\n```%s```", language.getString("uno.order"), Utils.config.getProperty(clockwise ? "emoji.arrow" : "emoji.back_arrow"), table);
        eb.addField(language.getString("uno.other_cards"), otherCards, false);
        eb.setImage("attachment://hand.png");
        eb.setThumbnail(String.format("%s%s%s.png", PATH, getTopCard().getColor().getToken(), getTopCard().getValue().getToken()));
        return eb;
    }

    public long getChannelID(){
        return channelID;
    }
}
