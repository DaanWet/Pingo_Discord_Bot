package commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;

import java.util.concurrent.TimeUnit;

public class AmongUs extends Command {

    public AmongUs(){
        this.name = "amongus";
        this.aliases = new String[]{"am", "among"};
        this.description = "amongus.description";

    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        GuildVoiceState vs = e.getMember().getVoiceState();
        MyResourceBundle language = getLanguage(e);
        if (vs == null || !vs.inVoiceChannel())
            throw new MessageException(language.getString("amongus.error"), 10);

        VoiceChannel vc = vs.getChannel();
        boolean mute = !vs.isGuildMuted();
        for (Member m : vc.getMembers()){
            m.mute(mute).queue();
        }
        e.getChannel().sendMessage(language.getString(mute ? "amongus.mute" : "amongus.unmute")).queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));


        e.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
    }
}
