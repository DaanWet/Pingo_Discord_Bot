package companions;

import data.handlers.AchievementHandler;
import data.handlers.CreditDataHandler;

public enum Achievement {
    BALANCE_10K(new AchievementChecker.Balance(10000)),
    BALANCE_100K(new AchievementChecker.Balance(100000)),
    BALANCE_1M(new AchievementChecker.Balance(1000000)),
    BALANCE_10M(new AchievementChecker.Balance(10000000)),

    BALANCE_NEG((guildId, userId, gameCompanion) -> new CreditDataHandler().getCredits(guildId, userId) < 0),

    GAMES_10(new AchievementChecker.PlayedGames(10)),
    GAMES_100(new AchievementChecker.PlayedGames(100)),
    GAMES_1000(new AchievementChecker.PlayedGames(1000)),
    GAMES_5000(new AchievementChecker.PlayedGames(5000)),

    STREAK_2(new AchievementChecker.WinStreak(2)),
    STREAK_5(new AchievementChecker.WinStreak(5)),
    STREAK_7(new AchievementChecker.WinStreak(7)),
    STREAK_10(new AchievementChecker.WinStreak(10)),
    STREAK_15(new AchievementChecker.WinStreak(15)),

    BET_10K(new AchievementChecker.BlackjackBet(10000)),
    BET_100K(new AchievementChecker.BlackjackBet(100000)),
    BET_1M(new AchievementChecker.BlackjackBet(1000000)),
    BET_10M(new AchievementChecker.BlackjackBet(10000000));

    final AchievementChecker checker;
    Achievement(AchievementChecker checker){
        this.checker = checker;
    }
    public boolean isAchieved(long guildId, long userId, GameCompanion gameCompanion){
        AchievementHandler handler = new AchievementHandler();
        if (handler.hasAchieved(guildId, userId, this))
            return false;

        return checker.isAchieved(guildId, userId, gameCompanion);

    }

}
