package listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class JoinListener extends ListenerAdapter {


    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e){
        if (e.getGuild().getIdLong() == 804675215728312361L){
            e.getGuild().addRoleToMember(e.getMember(), e.getGuild().getRoleById(804744530976440410L)).queue();
        }
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e){
        if (e.getGuild().getIdLong() == 804675215728312361L && e.getRoles().contains(e.getGuild().getRoleById(805555290393149470L))){
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(e.getGuild().getSelfMember().getColor());
            eb.setTitle(String.format("Gegroet %s!", e.getMember().getEffectiveName()));
            eb.setDescription("Persoonlijke groet hier");
            eb.setImage("https://i.ytimg.com/vi/4W2JKk2czxE/hqdefault.jpg?sqp=-oaymwEXCNACELwBSFryq4qpAwkIARUAAIhCGAE=&rs=AOn4CLA2x0ei5aHAPLflKjlBsYsaLlcJRQ");
            e.getGuild().getTextChannelById(805176551392935967L).sendMessage(eb.build()).queue();
        }
    }
}
