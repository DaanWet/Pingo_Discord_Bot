package companions.cardgames;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class BlackJackGame {
    public enum EndState {
        WON("You Won", 1),
        LOST("You Lost", -1),
        BUST("You Bust", -1),
        DEALER_BUST("The Dealer Bust", 1),
        PUSH("It's a push", 0),
        BLACKJACK("You have blackjack", 1.5);

        private final String display;
        private final double reward;

        EndState(String display, double reward){
            this.display = display;
            this.reward = reward;
        }

        public String getDisplay(){
            return display;
        }

        public double getReward(){
            return reward;
        }
    }

    private final ArrayList<Card> deck;
    private final BlackJackHand playerHand;
    private final BlackJackHand secondPlayerHand;
    private final BlackJackHand dealerHand;
    private Long messageId;
    private boolean hasEnded;
    private EndState endstate;
    private EndState secondEndstate;
    private int bet;
    private int secondbet;
    private boolean firsthand;
    private boolean hasSplit;

    public BlackJackGame(int bet){
        hasEnded = false;
        firsthand = true;
        hasSplit = false;
        deck = new ArrayList<>();
        this.bet = bet;
        secondbet = 0;
        playerHand = new BlackJackHand();
        dealerHand = new BlackJackHand();
        secondPlayerHand = new BlackJackHand();
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
        if (playerHand.getValue() == 21 && dealerHand.getValue() == 21){
            hasEnded = true;
            endstate = EndState.PUSH;
        } else if (dealerHand.getValue() == 21){
            hasEnded = true;
            endstate = EndState.LOST;
        } else if (playerHand.getValue() == 21){
            hasEnded = true;
            endstate = EndState.BLACKJACK;
        }
    }

    public EndState getEndState(int dealerv, int playerv){
        if (playerv > 21)
            return EndState.BUST;
        if (dealerv > 21)
            return EndState.DEALER_BUST;
        if (dealerv > playerv)
            return EndState.LOST;
        if (dealerv < playerv)
            return EndState.WON;

        return EndState.PUSH;
    }

    public void doDealerMoves(){
        while (dealerHand.getValue() < 17){
            dealerHand.addCard(deck.remove(0));
        }
        int dealv = dealerHand.getValue();
        int playerv = playerHand.getValue();
        endstate = getEndState(dealv, playerv);
        if (hasSplit){
            playerv = secondPlayerHand.getValue();
            secondEndstate = getEndState(dealv, playerv);
        }
    }

    public void hit(){
        BlackJackHand hand = firsthand ? playerHand : secondPlayerHand;
        hand.addCard(deck.remove(0));
        int value = hand.getValue();
        if (value >= 21){
            if (!firsthand || !hasSplit){
                hasEnded = true;
                doDealerMoves();
            }
            if (hasSplit && firsthand){
                firsthand = false;
            }
        }
    }

    public void stand(){
        if (!firsthand || !hasSplit){
            hasEnded = true;
            doDealerMoves();
        }
        if (hasSplit && firsthand){
            firsthand = false;
        }
    }

    public void split(){
        hasSplit = true;
        secondbet = bet;
        secondPlayerHand.addCard(playerHand.removeCard(1));
        playerHand.addCard(deck.remove(0));
        secondPlayerHand.addCard(deck.remove(0));
    }

    public boolean canDouble(){
        return playerHand.getCards().size() == 2;
    }

    public boolean canSplit(){
        return canDouble() && playerHand.getCards().get(0).getValue().getValue() == playerHand.getCards().get(1).getValue().getValue();
    }

    public void doubleDown(){
        if (firsthand){
            bet *= 2;
            playerHand.addCard(deck.remove(0));
        } else {
            secondbet *= 2;
            secondPlayerHand.addCard(deck.remove(0));
        }
        if (!hasSplit || !firsthand){
            hasEnded = true;
            doDealerMoves();
        } else {
            firsthand = false;
        }


    }

    public BlackJackHand getDealerHand(){
        return dealerHand;
    }

    public BlackJackHand getPlayerHand(){
        return playerHand;
    }

    public int getBet(){
        return bet;
    }

    public EndState getEndstate(){
        if (!hasEnded) return null;
        return endstate;
    }

    public boolean hasEnded(){
        return hasEnded;
    }

    public Long getMessageId(){
        return messageId;
    }

    public void setMessageId(Long messageId){
        this.messageId = messageId;
    }

    public int getWonCreds(){
        return ((Double) (bet * endstate.reward + (hasSplit ? secondbet * secondEndstate.reward : 0))).intValue();
    }

    public EmbedBuilder buildEmbed(String user, String prefix){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(String.format("\u2063Blackjack | %s | Bet : %d         \u2063", user, bet + secondbet));
        eb.addField(String.format("%sPlayer Cards", hasSplit && firsthand ? ":arrow_right: " : ""), String.format("%s\nValue: **%s**", playerHand.toString(), playerHand.getValue()), true);
        eb.addField("Dealer Cards", String.format("%s\nValue: **%s**", hasEnded ? dealerHand.toString() : dealerHand.toString().split(" ")[0] + " :question:", hasEnded ? dealerHand.getValue() : ":question:"), true);

        if (hasSplit){
            eb.addField(String.format("%sSecond Hand Cards", !firsthand ? ":arrow_right: " : ""), String.format("%s\nValue: **%s**", secondPlayerHand.toString(), secondPlayerHand.getValue()), false);
        }
        eb.setColor(Color.BLUE);
        if (hasEnded){
            int credits = getWonCreds();
            eb.addField(String.format("%s%s", endstate.display, hasSplit ? " and " + secondEndstate.display : ""), String.format("You %s %d credits", credits > 0 ? "won" : credits == 0 ? "won/lost" : "lost", credits), hasSplit);
            eb.setColor(credits > 0 ? Color.GREEN : credits == 0 ? Color.BLUE : Color.RED);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%sstand : see dealer cards\n%shit : take another card", prefix, prefix));
            if (canDouble())
                sb.append(String.format("\n%sdouble : double bet and take last card", prefix));
            if (canSplit() && !hasSplit)
                sb.append(String.format("\n%ssplit : split your cards", prefix));
            eb.addField("Commands", sb.toString(), false);
        }
        return eb;
    }
}
