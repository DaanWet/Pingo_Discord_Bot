package me.damascus2000.pingo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.damascus2000.pingo.companions.Record;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "userrecord")
@IdClass(RecordId.class)
public class UserRecord {

    @Id
    @Column(name = "userid", nullable = false)
    private long userId;
    @Id
    @Column(name = "guildid", nullable = false)
    private long guildId;
    @Id
    @Column(name = "name", nullable = false)
    private Record record;

    private String link;
    private double value;

    public UserRecord(long guildId, long userId, Record record){
        this.guildId = guildId;
        this.userId = userId;
        this.record = record;
    }

}
