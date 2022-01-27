package companions;

import utils.MyResourceBundle;

import java.util.Arrays;
import java.util.Optional;

public enum Record {
    CREDITS("highes_credits", 1, true),
    WIN("biggest_bj_win", 4, true),
    LOSS("biggest_bj_loss", 5, true),
    WIN_RATE("bj_win_rate", 3, false),
    GAMES("bj_games_played", 2, true),
    WIN_STREAK("bj_win_streak", 6, true),
    LOSS_STREAK("bj_loss_streak", 7, true);


    private final int place;
    private final String name;
    private final boolean isInt;

    Record(String name, int place, boolean isInt){
        this.name = name;
        this.place = place;
        this.isInt = isInt;
    }

    public static Optional<Record> getRecord(String name){
        return Arrays.stream(values()).filter(r -> r.name.equalsIgnoreCase(name)).findFirst();
    }

    public int getPlace(){
        return place;
    }

    public String getName(){
        return name;
    }

    public boolean isInt(){
        return isInt;
    }

    public String getDisplay(MyResourceBundle language){
        return language.getString(name);
    }


}
