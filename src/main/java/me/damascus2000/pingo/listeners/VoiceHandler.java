package me.damascus2000.pingo.listeners;

import me.damascus2000.pingo.companions.VoiceCompanion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class VoiceHandler extends ListenerAdapter {

    private final VoiceCompanion voiceCompanion;

    public VoiceHandler(VoiceCompanion companion){
        this.voiceCompanion = companion;
    }


    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event){
        if (voiceCompanion.isChannel(event.getGuild().getIdLong(), event.getChannelJoined().getIdLong())){
            voiceCompanion.removeSchedule(event.getGuild().getIdLong(), event.getChannelJoined().getIdLong());
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event){
        long guildId = event.getGuild().getIdLong();
        long channel = event.getChannelLeft().getIdLong();
        if (voiceCompanion.isChannel(guildId, channel)){
            if (event.getChannelLeft().getMembers().isEmpty()){
                voiceCompanion.addSchedule(guildId, channel, event.getChannelLeft().delete().queueAfter(10, TimeUnit.MINUTES, m -> voiceCompanion.removeChannel(guildId, channel)));
            }
        }
    }


}
