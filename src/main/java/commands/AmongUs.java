package commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
                e.getChannel().sendMessage(String.format("Succesfully %s all playing members", mute ? "muted" : "unmuted")).queue();
            }


        }
    }

    @Override
    public String getDescription() {
        return "Mutes or unmutes among us players";
    }
}
