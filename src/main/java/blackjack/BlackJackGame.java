package blackjack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class BlackJackGame {
    public enum EndState{
        WON("You Won", Color.GREEN, 1),
        LOST("You Lost", Color.RED, -1),
        BUST("You Bust", Color.RED, -1),
        DEALER_BUST("The Dealer Bust",Color.GREEN, 1),
        PUSH("It's a push", Color.BLUE, 0),
        BLACKJACK("You have blackjack", Color.GREEN, 1.5);

        private String display;
        private  Color color;
        private double reward;

        EndState(String display, Color color, double reward){
            this.display = display;
            this.color = color;
            this.reward = reward;
        }

        public String getDisplay() {
            return display;
        }

        public Color getColor() {
            return color;
        }

        public double getReward() {
            return reward;
        }
    }

    private Long messageId;
    private ArrayList<Card> deck;
    private BlackJackHand playerHand;
    private BlackJackHand dealerHand;
    private boolean hasEnded;
    private EndState endstate;
    private int bet;


    public BlackJackGame(int bet){
        hasEnded = false;
        deck = new ArrayList<>();
        this.bet = bet;
        playerHand = new BlackJackHand();
        dealerHand = new BlackJackHand();
        for (Card.Face f : Card.Face.values()){
            for (Card.Value v : Card.Value.values()){
                deck.add(new Card(f, v));
            }
        }
        Collections.shuffle(deck);
        dealerHand.addCard(deck.remove(0));
        playerHand.addCard(deck.remove(0));
        dealerHand.addCard(deck.remove(0));
        playerHand.addCard(deck.remove(0));
        if (dealerHand.getValue() == 21){
            hasEnded = true;
            endstate = EndState.LOST;
        } else if (playerHand.getValue() == 21){
            hasEnded = true;
            endstate = EndState.BLACKJACK;
        }

    }


    public void doDealerMoves(){
        while (dealerHand.getValue() < 17){
            dealerHand.addCard(deck.remove(0));
        }
        int dealv = dealerHand.getValue();
        int playerv = playerHand.getValue();
        if (dealv > 21){
            endstate = EndState.DEALER_BUST;
        } else if (dealv > playerv){
            endstate = EndState.LOST;
        } else if (dealv < playerv ){
            endstate = EndState.WON;
        } else {
            endstate = EndState.PUSH;
        }
    }

    public void hit(){
        playerHand.addCard(deck.remove(0));
        if (playerHand.getValue() > 21){
            hasEnded = true;
            endstate = EndState.BUST;
        }
    }

    public void stand(){
        hasEnded = true;
        doDealerMoves();
    }

    public void doubleDown(){
        playerHand.addCard(deck.remove(0));
        hasEnded = true;
        doDealerMoves();
    }


    public BlackJackHand getDealerHand() {
        return dealerHand;
    }

    public BlackJackHand getPlayerHand() {
        return playerHand;
    }
    public int getBet() {
        return bet;
    }

    public EndState getEndstate(){
        if (!hasEnded) return null;
        return endstate;
    }

    public boolean hasEnded(){
        return hasEnded;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public MessageEmbed buildEmbed(String user){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(String.format("\u2063Blackjack | %s           \u2063", user));
        eb.addField("Player Cards", String.format("%s\nValue: **%s**", playerHand.toString(), playerHand.getValue()), true);
        eb.addField("Dealer Cards", String.format("%s\nValue: **%s**", hasEnded ? dealerHand.toString() : dealerHand.toString().split(" ")[0] + " :question:", hasEnded ? dealerHand.getValue() : ":question:"), true);
        eb.setColor(Color.BLUE);
        if (hasEnded){
            int credits = ((Double) (bet * endstate.reward)).intValue();
            eb.addField(endstate.getDisplay(), String.format("You %s %d credits", endstate.getReward() > 0 ? "won" : endstate.getReward() == 0 ? "won/lost" : "lost", credits), false);
            eb.setColor(endstate.getColor());
        }

        return eb.build();
    };
}
