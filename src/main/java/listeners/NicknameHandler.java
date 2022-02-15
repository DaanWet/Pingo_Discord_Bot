package listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.Utils;

public class NicknameHandler extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event){
        String name = event.getOldNickname();
        Guild guild = event.getGuild();
        guild.retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).queue(logs -> {
            if (!logs.isEmpty()){
                AuditLogEntry log = logs.get(0);
                Member person = guild.getMember(log.getUser());
                Member target = guild.getMemberById(log.getTargetId());
                if (!person.getUser().isBot() && !target.getRoles().contains(guild.getRoleById((long) Utils.config.get("special.role"))) && !person.equals(target) && !person.hasPermission(Permission.ADMINISTRATOR)){
                    target.modifyNickname(name).queue();
                }
            }
        });
    }
}