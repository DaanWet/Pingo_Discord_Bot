package commands.roles;


import commands.settings.CommandState;
import commands.settings.Setting;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AddRoleAssign extends RoleCommand {

    public AddRoleAssign() {
        name = "addRoleAssign";
        aliases = new String[]{"addRole", "addRoleA", "addRA"};
        category = "Moderation";
        this.arguments = "<category> <emoji> <role> <name>";
        this.description = "Add a role to the role assigner";
    }

    public CommandState canBeExecuted(long guildId, long channelId, Member member) {
        return canBeExecuted(guildId, channelId, member, Setting.ROLEASSIGN);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        DataHandler dataHandler = new DataHandler();
        long guildId = e.getGuild().getIdLong();
        if (args.length == 1) {
            e.getChannel().sendMessage("No emoji, role and name given\n" + getUsage()).queue();
        } else if (args.length == 2) {
            e.getChannel().sendMessage("No role and name given\n" + getUsage()).queue();
        } else if (args.length == 3) {
            e.getChannel().sendMessage("No name given\n" + getUsage()).queue();
        } else if (args.length >= 4 && dataHandler.getRoleCategories(e.getGuild().getIdLong()).contains(args[0])) {
            if (!hasEmoji(e.getMessage(), args[1])) {
                e.getChannel().sendMessage(String.format("%s is not a valid emoji\n%s", args[1], getUsage())).queue();
                return;
            }
            Role role = null;
            try {
                role = e.getMessage().getMentionedRoles().size() == 0 ? e.getGuild().getRoleById(args[2]) : e.getMessage().getMentionedRoles().get(0);
            } catch (Exception exc) {
                e.getChannel().sendMessage("Could not get the role \n" + getUsage()).queue();
                return;
            }
            int pos = role.getPosition();
            if (e.getGuild().getSelfMember().getRoles().stream().noneMatch(r -> r.getPosition() > pos) || !e.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                e.getChannel().sendMessage("I have insufficient permissions to assign that role to somebody. Make sure that I have a role that's higher than the role you're trying to assign and that I have the _manage roles_ permission").queue();
                e.getMessage().addReaction("❌").queue();
                return;
            }
            StringBuilder name = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                name.append(args[i]).append(" ");
            }

            boolean succeeded = dataHandler.addRoleAssign(e.getGuild().getIdLong(), args[0], args[1], name.toString().trim(), role.getIdLong());
            if (succeeded) {
                e.getMessage().addReaction("✅").queue();
                e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
                RoleAssignData data = dataHandler.getRoleAssignData(e.getGuild().getIdLong(), args[0]);
                String emote = args[1].replaceFirst("<", "").replaceFirst(">$", "");
                if (data.getMessageId() != null) {
                    e.getGuild().getTextChannelById(data.getChannelId()).retrieveMessageById(data.getMessageId()).queue(m -> {
                        MessageEmbed me = m.getEmbeds().get(0);
                        ArrayList<RoleAssignRole> roles = dataHandler.getRoles(e.getGuild().getIdLong(), args[0]);
                        m.editMessage(getRoleEmbed(roles, args[0], data).build()).queue();
                        m.addReaction(emote).queue();
                    });
                }
            } else {
                e.getMessage().addReaction("❌").queue();
                e.getChannel().sendMessage("Unable to add to database, that emoji is already used in the role picker").queue();
            }

        } else {
            e.getChannel().sendMessage("No valid category given\n" + getUsage()).queue();
        }
    }
}
