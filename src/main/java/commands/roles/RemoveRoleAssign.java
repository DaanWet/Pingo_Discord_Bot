package commands.roles;

import commands.settings.CommandState;
import commands.settings.Setting;
import data.handlers.RRDataHandler;
import data.models.RoleAssignData;
import data.models.RoleAssignRole;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RemoveRoleAssign extends RoleCommand {


    public RemoveRoleAssign(){
        this.name = "removeRoleAssign";
        this.aliases = new String[]{"removeRole", "removeRoleA", "removeRA"};
        this.arguments = "<category> <emoji>";
        this.description = "roleassign.remove.description";
    }

    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.ROLEASSIGN);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        RRDataHandler dataHandler = new RRDataHandler();
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (args.length == 0)
            throw new MessageException(language.getString("roleassign.error.no_category"));
        if (args.length == 1)
            throw new MessageException(language.getString("roleassign.remove.error.emoji"));
        if (args.length > 2 || !dataHandler.getRoleCategories(e.getGuild().getIdLong()).contains(args[0]))
            throw new MessageException(language.getString("roleassign.error.category"));
        if (!hasEmoji(e.getMessage(), args[1]))
            throw new MessageException(language.getString("roleassign.edit.error.emoji", args[1]));

        long guildId = e.getGuild().getIdLong();
        String emote = args[1].replaceFirst("<", "").replaceFirst(">$", "");
        boolean found = dataHandler.removeRoleAssign(guildId, args[0], args[1]);
        if (!found)
            throw new MessageException(language.getString("roleassign.remove.error.role"), 15);

        RoleAssignData data = dataHandler.getRoleAssignData(guildId, args[0]);
        if (data.getMessageId() != null){
            e.getGuild().getTextChannelById(data.getChannelId()).retrieveMessageById(data.getMessageId()).queue(m -> {
                MessageEmbed me = m.getEmbeds().get(0);
                ArrayList<RoleAssignRole> roles = dataHandler.getRoles(guildId, args[0]);
                m.editMessageEmbeds(getRoleEmbed(roles, args[0], data, language).build()).queue();
                e.getMessage().addReaction(Utils.config.getProperty("emoji.checkmark")).queue();
                for (MessageReaction mr : e.getMessage().getReactions()){
                    if (mr.getReactionEmote().getAsReactionCode().equals(emote)){
                        mr.clearReactions().queue();
                    }
                }
                e.getMessage().delete().queueAfter((int) Utils.config.get("timeout"), TimeUnit.SECONDS);
            });
        }

    }
}
