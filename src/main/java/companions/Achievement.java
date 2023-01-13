package companions;

import data.handlers.AchievementHandler;
import data.handlers.CreditDataHandler;

public enum Achievement {
    BALANCE_10K(new AchievementChecker.Balance(10000), Reward.BEGINNER, "achievement.balance.10K", "achievement.balance.10K.desc", false, Type.BALANCE),
    BALANCE_100K(new AchievementChecker.Balance(100000), Reward.EASY, "achievement.balance.100K", "achievement.balance.100K.desc", false, Type.BALANCE),
    BALANCE_1M(new AchievementChecker.Balance(1000000), Reward.HARD, "achievement.balance.1M", "achievement.balance.1M.desc", false, Type.BALANCE),
    BALANCE_10M(new AchievementChecker.Balance(10000000), Reward.VERY_HARD, "achievement.balance.10M", "achievement.balance.10M.desc", false, Type.BALANCE),

    BALANCE_NEG((guildId, userId, gameCompanion) -> new CreditDataHandler().getCredits(guildId, userId) < 0, Reward.NORMAL, "achievement.balance.neg", "achievement.balance.neg.desc", true, Type.BALANCE),

    GAMES_10(new AchievementChecker.PlayedGames(10), Reward.BEGINNER, "achievement.games.10", "achievement.games.10.desc", false, Type.GAMES),
    GAMES_100(new AchievementChecker.PlayedGames(100), Reward.NORMAL, "achievement.games.100", "achievement.games.100.desc", false, Type.GAMES),
    GAMES_1000(new AchievementChecker.PlayedGames(1000), Reward.HARD, "achievement.games.1K", "achievement.games.1K.desc", false, Type.GAMES),
    GAMES_5000(new AchievementChecker.PlayedGames(5000), Reward.VERY_HARD, "achievement.games.5K", "achievement.games.5K.desc", false, Type.GAMES),

    STREAK_2(new AchievementChecker.WinStreak(2), Reward.EASY, "achievement.streak.2", "achievement.streak.2.desc", false, Type.STREAK),
    STREAK_5(new AchievementChecker.WinStreak(5), Reward.NORMAL, "achievement.streak.5", "achievement.streak.5.desc", false, Type.STREAK),
    STREAK_7(new AchievementChecker.WinStreak(7), Reward.HARD, "achievement.streak.7", "achievement.streak.7.desc", false, Type.STREAK),
    STREAK_10(new AchievementChecker.WinStreak(10), Reward.VERY_HARD, "achievement.streak.10", "achievement.streak.10.desc", false, Type.STREAK),
    STREAK_15(new AchievementChecker.WinStreak(15), Reward.EXTREME, "achievement.streak.15", "achievement.streak.15.desc", false, Type.STREAK),

    BET_10K(new AchievementChecker.BlackjackBet(10000), Reward.EASY, "achievement.bet.10K", "achievement.bet.10K.desc", false, Type.BET),
    BET_100K(new AchievementChecker.BlackjackBet(100000), Reward.NORMAL, "achievement.bet.100K", "achievement.bet.100K.desc", false, Type.BET),
    BET_1M(new AchievementChecker.BlackjackBet(1000000), Reward.VERY_HARD, "achievement.bet.1M", "achievement.bet.1M.desc", false, Type.BET),
    BET_10M(new AchievementChecker.BlackjackBet(10000000), Reward.EXTREME, "achievement.bet.10M", "achievement.bet.10M.desc", false, Type.BET);

    public enum Type{
        BALANCE("Balance", "\uD83D\uDFE8"),
        GAMES("Games", "\uD83D\uDFE6"),
        STREAK("Streak", "\uD83D\uDFE9"),
        BET("Placed Bet", "\uD83D\uDFE5");

        public String description;
        public String emoji;
        Type(String description, String emoji){
            this.description = description;
            this.emoji = emoji;
        }
    }

    public enum Reward {
        NO_REWARD("Magic", 0),
        BEGINNER("Beginner", 10),
        EASY("Easy", 15),
        NORMAL("Normal", 20),
        HARD("Hard", 50),
        VERY_HARD("Very Hard", 100),
        EXTREME("Extreme", 250);
        public int reward;
        public String name;
        Reward(String name, int reward){
            this.reward = reward;
            this.name = name;
        }
    }



    private final AchievementChecker checker;
    private final String title;
    private final String description;
    private final boolean hidden;
    private final Reward reward;
    private final Type type;

    Achievement(AchievementChecker checker, Reward reward, String title, String description, boolean hidden, Type type){
        this.checker = checker;
        this.title = title;
        this.description = description;
        this.hidden = hidden;
        this.reward = reward;
        this.type = type;
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

    public Reward getReward(){
        return reward;
    }

    public Type getType(){
        return type;
    }
}
