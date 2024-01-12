package me.damascus2000.pingo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="userachievement")
@IdClass(AchievementId.class)
public class UserAchievement {

    @Id
    @Column(name="userid", nullable = false)
    private long userId;

    @Id
    @Column(name="guildid", nullable = false)
    private long guildId;

    @Id
    private String achievement;

    private boolean achieved;

    private LocalDateTime time;

    public UserAchievement(long guildId, long userId, String achievement){
        this.userId = userId;
        this.guildId = guildId;
        this.achievement = achievement;
    }

}
