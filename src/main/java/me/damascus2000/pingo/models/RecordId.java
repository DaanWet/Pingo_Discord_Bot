package me.damascus2000.pingo.models;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.damascus2000.pingo.companions.Record;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Data
@Access(AccessType.FIELD)
public class RecordId implements Serializable {

    private long userId;
    private long guildId;

    private Record record;


    public RecordId(long guildId, long userId, Record record){
        this.guildId = guildId;
        this.userId = userId;
        this.record = record;
    }
}