package me.damascus2000.pingo.models;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Data
@Access(AccessType.FIELD)
public class AchievementId implements Serializable {

    private long userId;
    private long guildId;

    private String achievement;

    public AchievementId(){}


    public AchievementId(long guildId, long userId, String achievement){
        this.guildId = guildId;
        this.userId = userId;
        this.achievement = achievement;
    }
}