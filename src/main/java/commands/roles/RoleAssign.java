package commands.roles;


import org.apache.commons.lang3.tuple.Triple;
import utils.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RoleAssign extends RoleCommand{

    public RoleAssign() {
        name = "roleassign";
        this.arguments = "<category> [<compact>]";
        this.description = "Display Role picker";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (args.length == 1 || args.length == 2) {
            DataHandler dataHandler = new DataHandler();
            ArrayList<Triple<String, String, Long>> roles = dataHandler.getRoles(e.getGuild().getIdLong(), args[0]);
            if (roles == null) {
                e.getChannel().sendMessage(String.format("%s is not an existing category", args[0])).queue();
                return;
            }
            RoleAssignData data = dataHandler.getRoleAssignData(e.getGuild().getIdLong(), args[0]);
            Compacting compact = Objects.requireNonNullElse(data.getCompacting(), Compacting.NORMAL);
            if (args.length == 2) {
                if ( args[1].equalsIgnoreCase("compact")){
                    compact = Compacting.COMPACT;
                } else if (args[1].equalsIgnoreCase("supercompact")){
                    compact = Compacting.SUPER_COMPACT;
                } else if (!args[1].equalsIgnoreCase("normal")){
                    e.getChannel().sendMessage(String.format("%s is not an valid compacting method", args[1])).queue();
                    return;
                }
            }

            if (data.getMessageId() != null) {
                e.getGuild().getTextChannelById(data.getChannelId()).retrieveMessageById(data.getMessageId()).queue(m -> {if(m != null){ m.delete().queue();}});
            }
            EmbedBuilder eb = getRoleEmbed(roles, args[0], data.getSorting(), compact, data.getTitle());
            e.getChannel().sendMessage(eb.build()).queue(m -> {
                dataHandler.setMessage(e.getGuild().getIdLong(), args[0], m.getTextChannel().getIdLong(), m.getIdLong());
                for (Triple<String, String, Long> obj : roles) {
                    m.addReaction(obj.getLeft().replaceFirst("<", "").replaceFirst(">$", "")).queue();
                }
            });
        } else {
            e.getChannel().sendMessage(getUsage()).queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
        }

        e.getMessage().delete().queue();
    }
}
