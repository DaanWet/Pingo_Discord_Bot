package me.damascus2000.pingo.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.damascus2000.pingo.companions.Achievement;


@Getter
@Setter
@NoArgsConstructor
public class AchievementCountDTO {

    private long count;
    private Achievement achievement;

    public AchievementCountDTO(long count, Achievement achievement){
        this.count = count;
        this.achievement = achievement;
    }

}
