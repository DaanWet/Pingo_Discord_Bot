package companions.cardgames;

import net.dv8tion.jda.api.EmbedBuilder;
import utils.MyProperties;
import utils.MyResourceBundle;
import utils.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class BlackJackGame {
    public enum EndState {
        WON("blackjack.won", 1),
        LOST("blackjack.lost", -1),
        BUST("blackjack.bust", -1),
        DEALER_BUST("blackjack.dealer", 1),
        PUSH("blackjack.push", 0),
        BLACKJACK("blackjack.blackjack", 1.5);

        private final String display;
        private final double reward;

        EndState(String display, double reward){
            this.display = display;
            this.reward = reward;
        }

        public String getDisplay(MyResourceBundle language){
            return language.getString(display);
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
    public int getWonXP(){
        int xp = Utils.getGameXP(this.bet);
        int xp2 = Utils.getGameXP(this.secondbet);
        xp = endstate.reward > 0 ? xp : Math.min(xp, 1);
        if (secondEndstate != null)
            xp2 = secondEndstate.reward > 0 ? xp2 : Math.min(xp2, 1);
        return xp + xp2;
    }

    public EmbedBuilder buildEmbed(String user, String prefix, MyResourceBundle language){
        MyProperties config = Utils.config;
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(String.format("\u2063%s         \u2063", language.getString("blackjack.title", user, bet + secondbet)));
        eb.addField((hasSplit && firsthand ? config.getProperty("emoji.arrow")/*extra space here*/ : "") + language.getString("blackjack.player.title"), String.format("%s\n%s", playerHand.toString(), language.getString("blackjack.value", playerHand.getValue())), true);
        eb.addField(language.getString("blackjack.dealer.title"), String.format("%s\n%s", hasEnded ? dealerHand.toString() : dealerHand.toString().split(" ")[0] + /*extra space here*/config.getProperty("emoji.question"), language.getString("blackjack.value", hasEnded ? dealerHand.getValue() : config.getProperty("emoji.question"))), true);

        if (hasSplit){
            eb.addField((!firsthand ? config.getProperty("emoji.arrow") /*extra space here*/ : "") + language.getString("blackjack.player.second"), String.format("%s\n%s", secondPlayerHand.toString(), language.getString("blackjack.value", secondPlayerHand.getValue())), false);
        }
        eb.setColor(Color.BLUE);
        if (hasEnded){
            int credits = getWonCreds();
            eb.addField(hasSplit ? language.getString("blackjack.end.split", endstate.getDisplay(language), secondEndstate.getDisplay(language)) : language.getString("blackjack.end", endstate.getDisplay(language)), language.getString("blackjack.end.desc", credits), hasSplit);
            eb.setColor(credits > 0 ? Color.GREEN : credits == 0 ? Color.BLUE : Color.RED);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(language.getString("blackjack.stand", prefix + "stand")).append("\n");
            sb.append(language.getString("blackjack.hit", prefix + "hit"));
            if (canDouble())
                sb.append("\n").append(language.getString("blackjack.double", prefix + "double"));
            if (canSplit() && !hasSplit)
                sb.append("\n").append(language.getString("blackjack.split", prefix + "split"));
            eb.addField(language.getString("commands.title"), sb.toString(), false);
        }
        return eb;
    }
}
