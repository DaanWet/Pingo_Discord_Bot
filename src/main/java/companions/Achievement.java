package companions;

import data.handlers.AchievementHandler;
import data.handlers.CreditDataHandler;

public enum Achievement {
    BALANCE_10K(new AchievementChecker.Balance(10000), 5, "achievement.balance.10K", "achievement.balance.10K.desc", false),
    BALANCE_100K(new AchievementChecker.Balance(100000), 1, "achievement.balance.100K", "achievement.balance.100K.desc", false),
    BALANCE_1M(new AchievementChecker.Balance(1000000), 1, "achievement.balance.1M", "achievement.balance.1M.desc", false),
    BALANCE_10M(new AchievementChecker.Balance(10000000), 1, "achievement.balance.10M", "achievement.balance.10M.desc", false),

    BALANCE_NEG((guildId, userId, gameCompanion) -> new CreditDataHandler().getCredits(guildId, userId) < 0, 1, "achievement.balance.neg", "achievement.balance.neg.desc", true),

    GAMES_10(new AchievementChecker.PlayedGames(10), 1, "achievement.games.10", "achievement.games.10.desc", false),
    GAMES_100(new AchievementChecker.PlayedGames(100), 1, "achievement.games.100", "achievement.games.100.desc", false),
    GAMES_1000(new AchievementChecker.PlayedGames(1000), 1, "achievement.games.1K", "achievement.games.1K.desc", false),
    GAMES_5000(new AchievementChecker.PlayedGames(5000), 1, "achievement.games.5K", "achievement.games.5K.desc", false),

    STREAK_2(new AchievementChecker.WinStreak(2), 1, "achievement.streak.2", "achievement.streak.2.desc", false),
    STREAK_5(new AchievementChecker.WinStreak(5), 1, "achievement.streak.5", "achievement.streak.5.desc", false),
    STREAK_7(new AchievementChecker.WinStreak(7), 1, "achievement.streak.7", "achievement.streak.7.desc", false),
    STREAK_10(new AchievementChecker.WinStreak(10), 1, "achievement.streak.10", "achievement.streak.10.desc", false),
    STREAK_15(new AchievementChecker.WinStreak(15), 1, "achievement.streak.15", "achievement.streak.15.desc", false),

    BET_10K(new AchievementChecker.BlackjackBet(10000), 1, "achievement.bet.10K", "achievement.bet.10K.desc", false),
    BET_100K(new AchievementChecker.BlackjackBet(100000), 1, "achievement.bet.100K", "achievement.bet.100K.desc", false),
    BET_1M(new AchievementChecker.BlackjackBet(1000000), 1, "achievement.bet.1M", "achievement.bet.1M.desc", false),
    BET_10M(new AchievementChecker.BlackjackBet(10000000), 1, "achievement.bet.10M", "achievement.bet.10M.desc", false);

    private final AchievementChecker checker;
    private final String title;
    private final String description;
    private final boolean hidden;
    private final int reward;

    Achievement(AchievementChecker checker, int reward, String title, String description, boolean hidden){
        this.checker = checker;
        this.title = title;
        this.description = description;
        this.hidden = hidden;
        this.reward = reward;
    }
    public boolean isAchieved(long guildId, long userId, GameCompanion gameCompanion){
        AchievementHandler handler = new AchievementHandler();
        if (handler.hasAchieved(guildId, userId, this))
            return false;

        return checker.isAchieved(guildId, userId, gameCompanion);

    }


    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }

    public boolean isHidden(){
        return hidden;
    }

    public int getReward(){
        return reward;
    }
}
