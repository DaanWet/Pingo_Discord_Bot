package commands.roles;

import commands.settings.CommandState;
import commands.settings.Setting;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RemoveRoleAssign extends RoleCommand {


    public RemoveRoleAssign() {
        this.name = "removeRoleAssign";
        this.aliases = new String[]{"removeRole", "removeRoleA", "removeRA"};
        this.category = "Moderation";
        this.arguments = "<category> <emoji>";
        this.description = "Removes a role from the board";
    }

    public CommandState canBeExecuted(long guildId, long channelId, Member member) {
        return canBeExecuted(guildId, channelId, member, Setting.ROLEASSIGN);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        DataHandler dataHandler = new DataHandler();
        if (args.length == 1) {
            e.getChannel().sendMessage("No emoji provided to delete\n" + getUsage()).queue();
        } else if (args.length == 2 && dataHandler.getRoleCategories(e.getGuild().getIdLong()).contains(args[0])) {
            if (!hasEmoji(e.getMessage(), args[1])) {
                e.getChannel().sendMessage(String.format("%s is not a valid emoji\n%s", args[1], getUsage())).queue();
                return;
            }
            long guildId = e.getGuild().getIdLong();
            String emote = args[1].replaceFirst("<", "").replaceFirst(">$", "");
            boolean found = dataHandler.removeRoleAssign(guildId, args[0], args[1]);
            if (!found) {
                e.getChannel().sendMessage("No matching role found").queue(mes -> mes.delete().queueAfter(15, TimeUnit.SECONDS));
                return;
            }
            RoleAssignData data = dataHandler.getRoleAssignData(guildId, args[0]);
            if (data.getMessageId() != null) {
                e.getGuild().getTextChannelById(data.getChannelId()).retrieveMessageById(data.getMessageId()).queue(m -> {
                    MessageEmbed me = m.getEmbeds().get(0);
                    ArrayList<RoleAssignRole> roles = dataHandler.getRoles(guildId, args[0]);
                    m.editMessage(getRoleEmbed(roles, args[0], data).build()).queue();
                    e.getMessage().addReaction("✅").queue();
                    for (MessageReaction mr : e.getMessage().getReactions()) {
                        if (mr.getReactionEmote().getAsReactionCode().equals(emote)) {
                            mr.clearReactions().queue();
                        }
                    }
                    e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
                });
            }
        } else {
            e.getChannel().sendMessage("No valid category provided\n" + getUsage()).queue();
        }
    }
}
