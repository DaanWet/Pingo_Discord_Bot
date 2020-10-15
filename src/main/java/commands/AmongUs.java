package commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class AmongUs extends Command{

    public AmongUs(){
        this.name = "amongus";
        this.aliases = new String[]{"am", "among"};

    }
    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        GuildVoiceState vs = e.getMember().getVoiceState();
        if (vs != null && vs.inVoiceChannel()){
            VoiceChannel vc = vs.getChannel();
            if (vc.getIdLong() == 764495094375645205L){
                boolean mute = !vs.isGuildMuted();
                for (Member m : vc.getMembers()){
                    m.mute(mute).queue();
                }
                e.getChannel().sendMessage(String.format("Succesfully %s all playing members", mute ? "muted" : "unmuted")).queueAfter(10, TimeUnit.SECONDS);
            } else {
                e.getChannel().sendMessage("Wrong voicechannel noob").queueAfter(10, TimeUnit.SECONDS);
            }
        } else {
            e.getChannel().sendMessage("You're not connected to a voice channel").queueAfter(10, TimeUnit.SECONDS);
        }
        e.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
    }

    @Override
    public String getDescription() {
        return "Mutes or unmutes among us players";
    }
}
