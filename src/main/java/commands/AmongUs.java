package commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;

import java.util.concurrent.TimeUnit;

public class AmongUs extends Command {

    public AmongUs(){
        this.name = "amongus";
        this.aliases = new String[]{"am", "among"};
        this.description = "Mutes or unmutes among us players";

    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        GuildVoiceState vs = e.getMember().getVoiceState();
        if (vs == null || !vs.inVoiceChannel())
            throw new MessageException("You're not connected to a voice channel", 10);

        VoiceChannel vc = vs.getChannel();
        boolean mute = !vs.isGuildMuted();
        for (Member m : vc.getMembers()){
            m.mute(mute).queue();
        }
        e.getChannel().sendMessage(String.format("Succesfully %s all playing members", mute ? "muted" : "unmuted")).queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));


        e.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
    }
}
