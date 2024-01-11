package me.damascus2000.pingo.commands;

import me.damascus2000.pingo.companions.VoiceCompanion;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;

import java.nio.channels.Channel;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Voice extends Command{

    private final VoiceCompanion voiceCompanion;


    public Voice(VoiceCompanion voiceCompanion){
        this.name = "voice";
        this.description = "voice.description";
        this.arguments = new String[]{"<channelName> [people]"};
        this.voiceCompanion = voiceCompanion;
        this.category = Category.OTHER;
        this.example = "temporary @bob";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        Guild guild = e.getGuild();
        MyResourceBundle language = Utils.getLanguage(guild.getIdLong());
         if (args.length == 0)
             throw new MessageException(language.getString("voice.error.name"));
         if (args.length == 1)
             e.getGuild().createVoiceChannel(args[0], e.getChannel().getParent()).queue(voice -> createVoice(voice, e));
         else {
             ChannelAction<VoiceChannel> action = e.getGuild().createVoiceChannel(args[0], e.getChannel().getParent());
             List<IMentionable> mentions = e.getMessage().getMentions();
             if (mentions.size() != args.length - 1)
                 throw new MessageException(language.getString("voice.error.mentions"));
             action = action.addRolePermissionOverride(e.getGuild().getPublicRole().getIdLong(), Collections.emptyList(), EnumSet.of(Permission.VOICE_CONNECT, Permission.VIEW_CHANNEL));
             action = action.addMemberPermissionOverride(e.getGuild().getSelfMember().getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.MANAGE_CHANNEL), Collections.emptyList());
             for (int i = 1; i < args.length; i++){
                 if (args[i].replaceFirst("!", "").equalsIgnoreCase(mentions.get(i-1).getAsMention())){
                     IMentionable mention = mentions.get(i-1);
                     if (mention instanceof Channel)
                         throw new MessageException(language.getString("voice.error.mentions"));
                     if (mention instanceof Role)
                         action = action.addRolePermissionOverride(mention.getIdLong(),  List.of(Permission.VOICE_CONNECT, Permission.VIEW_CHANNEL), Collections.emptyList());
                     if (mention instanceof Member || mention instanceof User)
                         action = action.addMemberPermissionOverride(mention.getIdLong(),  List.of(Permission.VOICE_CONNECT, Permission.VIEW_CHANNEL), Collections.emptyList());
                 }
             }
             action.queue(voice -> createVoice(voice, e));
         }
    }

    public void createVoice(VoiceChannel voice, GuildMessageReceivedEvent e){
        long guildId = voice.getGuild().getIdLong();
        long channel = voice.getIdLong();
        voiceCompanion.addChannel(guildId, channel);
        voiceCompanion.addSchedule(guildId, channel, voice.delete().queueAfter(5, TimeUnit.MINUTES, m -> voiceCompanion.removeChannel(guildId, channel)));
        e.getMessage().addReaction(Utils.config.getProperty("emoji.checkmark")).queue();
    }

}
