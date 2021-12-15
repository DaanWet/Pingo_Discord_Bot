package commands.roles;

import commands.settings.CommandState;
import commands.settings.Setting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;
import utils.MessageException;
import utils.dbdata.RoleAssignData;
import utils.dbdata.RoleAssignRole;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RoleAssign extends RoleCommand {

    public RoleAssign() {
        name = "roleassign";
        this.arguments = "<category> [<compact>]";
        this.description = "Display Role picker";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member) {
        return canBeExecuted(guildId, channelId, member, Setting.ROLEASSIGN);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        if (args.length == 1 || args.length == 2) {
            DataHandler dataHandler = new DataHandler();
            ArrayList<RoleAssignRole> roles = dataHandler.getRoles(e.getGuild().getIdLong(), args[0]);
            if (roles == null)
                throw new MessageException(String.format("%s is not an existing category", args[0]));
            RoleAssignData data = dataHandler.getRoleAssignData(e.getGuild().getIdLong(), args[0]);
            Compacting compact = Objects.requireNonNullElse(data.getCompacting(), Compacting.NORMAL);
            if (args.length == 2) {
                if (args[1].equalsIgnoreCase("compact")) {
                    compact = Compacting.COMPACT;
                } else if (args[1].equalsIgnoreCase("supercompact")) {
                    compact = Compacting.SUPER_COMPACT;
                } else if (!args[1].equalsIgnoreCase("normal")) {
                    throw new MessageException(String.format("%s is not an valid compacting method", args[1]));
                }
            }
            data.setCompacting(compact);
            dataHandler.setCompacting(e.getGuild().getIdLong(), args[0], compact, data.getSorting() == Sorting.CUSTOM ? data.getCustomS() : data.getSorting().toString());
            if (data.getMessageId() != null) {
                e.getGuild().getTextChannelById(data.getChannelId()).retrieveMessageById(data.getMessageId()).queue(m -> {
                    if (m != null) {m.delete().queue();}
                });
            }
            EmbedBuilder eb = getRoleEmbed(roles, args[0], data);
            e.getChannel().sendMessage(eb.build()).queue(m -> {
                dataHandler.setMessage(e.getGuild().getIdLong(), args[0], m.getTextChannel().getIdLong(), m.getIdLong());
                for (RoleAssignRole obj : roles) {
                    m.addReaction(obj.getEmoji().replaceFirst("<", "").replaceFirst(">$", "")).queue();
                }
            });
        } else {
            throw new MessageException(getUsage(), 10);
        }
        e.getMessage().delete().queue();
    }
}
