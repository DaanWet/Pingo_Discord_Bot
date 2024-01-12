package me.damascus2000.pingo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "member")
@IdClass(GuildUserId.class)
public class Member {

    @Id
    @Column(name = "userid", nullable = false)
    private long userId;
    @Id
    @Column(name = "guildid", nullable = false)
    private long guildId;

    @Column(name = "credits", nullable = false)
    private int credits;

    @Column(name = "lastdaily")
    private LocalDateTime lastDaily;
    @Column(name = "lastweekly")
    private LocalDateTime lastWeekly;
    @Column(name = "experience")
    private int experience;
    @Column(name = "currentstreak")
    private int currentStreak;

    public Member(long guildId, long userId){
        this.userId = userId;
        this.guildId = guildId;
        this.credits = 0;
        this.lastDaily = LocalDateTime.now().minusDays(1);
        this.lastWeekly = LocalDateTime.now().minusDays(7);
        this.experience = 0;
        this.currentStreak = 0;
    }


}
