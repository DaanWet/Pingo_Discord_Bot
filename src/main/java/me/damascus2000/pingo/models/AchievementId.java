package me.damascus2000.pingo.models;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.damascus2000.pingo.companions.Achievement;
import me.damascus2000.pingo.repositories.AchievementConverter;

import java.io.Serializable;

@Getter
@Setter
@Data
@Access(AccessType.FIELD)
public class AchievementId implements Serializable {

    @Column(name = "userid", nullable = false)
    private long userId;
    @Column(name = "guildid", nullable = false)
    private long guildId;

    @Column(name = "achievement", nullable = false)
    @Convert(converter = AchievementConverter.class)
    private Achievement achievement;

    public AchievementId(){}


    public AchievementId(long guildId, long userId, Achievement achievement){
        this.guildId = guildId;
        this.userId = userId;
        this.achievement = achievement;
    }
}