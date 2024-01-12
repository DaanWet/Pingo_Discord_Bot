package me.damascus2000.pingo.commands.roles;

import me.damascus2000.pingo.commands.settings.CommandState;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.data.handlers.RRDataHandler;
import me.damascus2000.pingo.data.models.RoleAssignData;
import me.damascus2000.pingo.data.models.RoleAssignRole;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

@Component
public class RoleAssign extends RoleCommand {

    public RoleAssign(){
        name = "roleassign";
        this.arguments = new String[]{"<category> [**compact**|**supercompact**|**normal**]"};
        this.description = "roleassign.description";
        this.example = "Games";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.ROLEASSIGN);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (args.length == 0 || args.length > 2)
            throw new MessageException(getUsage(e.getGuild().getIdLong()), 10);

        RRDataHandler dataHandler = new RRDataHandler();
        ArrayList<RoleAssignRole> roles = dataHandler.getRoles(e.getGuild().getIdLong(), args[0]);
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (roles == null)
            throw new MessageException(language.getString("roleassign.error.category.existing", args[0]));
        RoleAssignData data = dataHandler.getRoleAssignData(e.getGuild().getIdLong(), args[0]);
        Compacting compact = Objects.requireNonNullElse(data.getCompacting(), Compacting.NORMAL);
        if (args.length == 2){
            if (args[1].equalsIgnoreCase("compact")){
                compact = Compacting.COMPACT;
            } else if (args[1].equalsIgnoreCase("supercompact")){
                compact = Compacting.SUPER_COMPACT;
            } else if (!args[1].equalsIgnoreCase("normal")){
                throw new MessageException(language.getString("roleassign.error.sorting", args[1]));
            }
        }
        data.setCompacting(compact);
        dataHandler.setCompacting(e.getGuild().getIdLong(), args[0], compact, data.getSorting() == Sorting.CUSTOM ? data.getCustomS() : data.getSorting().toString());
        if (data.getMessageId() != null){
            e.getGuild().getTextChannelById(data.getChannelId()).retrieveMessageById(data.getMessageId()).queue(m -> {
                if (m != null){m.delete().queue();}
            });
        }
        EmbedBuilder eb = getRoleEmbed(roles, args[0], data, language);
        e.getChannel().sendMessageEmbeds(eb.build()).queue(m -> {
            dataHandler.setMessage(e.getGuild().getIdLong(), args[0], m.getTextChannel().getIdLong(), m.getIdLong());
            for (RoleAssignRole obj : roles){
                m.addReaction(obj.getEmoji().replaceFirst("<", "").replaceFirst(">$", "")).queue();
            }
        });
        e.getMessage().delete().queue();
    }
}
