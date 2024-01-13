package me.damascus2000.pingo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.damascus2000.pingo.companions.Achievement;
import me.damascus2000.pingo.repositories.AchievementConverter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "userachievement")
@IdClass(AchievementId.class)
public class UserAchievement {

    @Id
    @Column(name = "userid", nullable = false)
    private long userId;

    @Id
    @Column(name = "guildid", nullable = false)
    private long guildId;

    @Id
    @Convert(converter = AchievementConverter.class)
    private Achievement achievement;

    private boolean achieved;

    private LocalDateTime time;

    public UserAchievement(long guildId, long userId, Achievement achievement){
        this.userId = userId;
        this.guildId = guildId;
        this.achievement = achievement;
    }

}
