package commands.roles;


import commands.settings.CommandState;
import commands.settings.Setting;
import data.DataHandler;
import data.models.RoleAssignData;
import data.models.RoleAssignRole;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class AddRoleAssign extends RoleCommand {

    public AddRoleAssign(){
        name = "addRoleAssign";
        aliases = new String[]{"addRole", "addRoleA", "addRA"};
        this.arguments = "<category> <emoji> <role> <name>";
        this.description = "roleassign.add.description";
    }

    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.ROLEASSIGN);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        DataHandler dataHandler = new DataHandler();
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        if (args.length == 0)
            throw new MessageException(language.getString("roleassign.error.no_category") + "\n" + getUsage());
        if (args.length == 1)
            throw new MessageException(language.getString("roleassign.error.no_emoji") + "\n" + getUsage());
        if (args.length == 2)
            throw new MessageException(language.getString("roleassign.error.no_role") + "\n" + getUsage());
        if (args.length == 3)
            throw new MessageException(language.getString("roleassign.error.no_name") + "\n" + getUsage());

        if (!dataHandler.getRoleCategories(e.getGuild().getIdLong()).contains(args[0]))
            throw new MessageException(language.getString("roleassign.error.category") + "\n" + getUsage());

        if (!hasEmoji(e.getMessage(), args[1]))
            throw new MessageException(language.getString("roleassign.error.emoji", args[1]) + "\n" + getUsage());
        Role role;
        try {
            role = e.getMessage().getMentionedRoles().size() == 0 ? e.getGuild().getRoleById(args[2]) : e.getMessage().getMentionedRoles().get(0);
        } catch (Exception exc){
            throw new MessageException(language.getString("roleassign.error.role") + "\n" + getUsage());
        }
        int pos = role.getPosition();
        Properties config = Utils.config;
        if (e.getGuild().getSelfMember().getRoles().stream().noneMatch(r -> r.getPosition() > pos) || !e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)){
            e.getMessage().addReaction(config.getProperty("emoji.cancel")).queue();
            throw new MessageException(language.getString("roleassign.error.perms"));
        }
        String name = Utils.concat(args, 3);

        boolean succeeded = dataHandler.addRoleAssign(e.getGuild().getIdLong(), args[0], args[1], name.trim(), role.getIdLong());
        if (!succeeded){
            e.getMessage().addReaction(config.getProperty("emoji.cancel")).queue();
            throw new MessageException(language.getString("roleassign.error.emoji.used"));
        }

        e.getMessage().addReaction(config.getProperty("emoji.checkmark")).queue();
        e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS); // Should this value be added to properties?
        RoleAssignData data = dataHandler.getRoleAssignData(e.getGuild().getIdLong(), args[0]);
        String emote = args[1].replaceFirst("<", "").replaceFirst(">$", "");
        if (data.getMessageId() != null){
            e.getGuild().getTextChannelById(data.getChannelId()).retrieveMessageById(data.getMessageId()).queue(m -> {
                ArrayList<RoleAssignRole> roles = dataHandler.getRoles(e.getGuild().getIdLong(), args[0]);
                m.editMessage(getRoleEmbed(roles, args[0], data, language).build()).queue();
                m.addReaction(emote).queue();
            });
        }


    }
}
