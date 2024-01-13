package me.damascus2000.pingo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.damascus2000.pingo.companions.Record;
import me.damascus2000.pingo.repositories.RecordConverter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class RecordId implements Serializable {

    @Column(name = "userid", nullable = false)
    private long userId;
    @Column(name = "guildid", nullable = false)
    private long guildId;

    @Convert(converter = RecordConverter.class)
    @Column(name = "name", nullable = false)
    private Record record;


    public RecordId(long guildId, long userId, Record record){
        this.guildId = guildId;
        this.userId = userId;
        this.record = record;
    }
}