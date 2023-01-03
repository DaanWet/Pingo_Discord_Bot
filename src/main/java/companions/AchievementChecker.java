package companions;

import companions.cardgames.BlackJackGame;
import data.handlers.CreditDataHandler;
import data.handlers.RecordDataHandler;
import data.models.RecordData;

public interface AchievementChecker {


    boolean isAchieved(long guildId, long userId, GameCompanion gameCompanion);


    class Balance implements AchievementChecker{

        private final int amount;

        Balance(int amount){
            this.amount = amount;
        }

        @Override
        public boolean isAchieved(long guildId, long userId, GameCompanion gameCompanion){
            CreditDataHandler handler = new CreditDataHandler();
            int credits = handler.getCredits(guildId, userId);
            return credits >= amount;
        }
    }

    class PlayedGames implements AchievementChecker{

        private final int amount;

        PlayedGames(int amount){
            this.amount = amount;
        }

        @Override
        public boolean isAchieved(long guildId, long userId, GameCompanion gameCompanion){
            RecordDataHandler handler = new RecordDataHandler();
            RecordData games = handler.getRecord(guildId, userId, Record.GAMES);
            return games.getValue() >= amount;
        }
    }

    class WinStreak implements AchievementChecker {
        private final int amount;

        WinStreak(int amount){
            this.amount = amount;
        }

        @Override
        public boolean isAchieved(long guildId, long userId, GameCompanion gameCompanion){
            RecordDataHandler handler = new RecordDataHandler();
            int streak = handler.getStreak(guildId, userId);
            return streak >= amount;
        }
    }

    class BlackjackBet implements AchievementChecker {

        private final int amount;

        BlackjackBet(int amount){
            this.amount = amount;
        }

        @Override
        public boolean isAchieved(long guildId, long userId, GameCompanion gameCompanion){
            BlackJackGame game = gameCompanion.getBlackJackGame(guildId, userId);
            if (game == null)
                return false;
            return game.getBet() >= amount;
        }
    }

}
