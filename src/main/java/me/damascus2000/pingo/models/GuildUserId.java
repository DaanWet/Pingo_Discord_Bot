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
public class GuildUserId implements Serializable {

    private long userId;
    private long guildId;

    public GuildUserId(){}


    public GuildUserId(long guildId, long userId){
        this.guildId = guildId;
        this.userId = userId;
    }
}
