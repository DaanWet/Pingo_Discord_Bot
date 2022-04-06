package commands.roles;

import data.handlers.RRDataHandler;
import data.models.RoleAssignData;
import data.models.RoleAssignRole;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyProperties;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

public class EditRoleAssign extends RoleCommand {

    public EditRoleAssign(){
        this.name = "editRoleAssign";
        this.aliases = new String[]{"editRA"};
        this.arguments = "<category> sort <**emoji**|**name**|**none**|\"custom_emoji_order\"> **compact**|**supercompact**|**normal**\n<category> <emoji> <name>\n<category> **title** <newtitle>";
        this.description = "roleassign.edit.description";
        this.example = "Games sort name compact";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        RRDataHandler dh = new RRDataHandler();
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        if (args.length == 0)
            throw new MessageException(language.getString("roleassign.error.no_category"));
        if (args.length == 1)
            throw new MessageException(language.getString("roleassign.edit.error.mode"));
        if (args.length == 2)
            throw new MessageException(language.getString("roleassign.edit.error.value"));
        if (!dh.getRoleCategories(guildId).contains(args[0]))
            throw new MessageException(language.getString("roleassign.error.category"));

        if (args[1].equalsIgnoreCase("sort")){
            RoleAssignData data = dh.getRoleAssignData(guildId, args[0]);
            final Compacting compact;
            final Sorting sort;
            if (args[2].equalsIgnoreCase("emoji")){
                sort = Sorting.EMOJI;
            } else if (args[2].equalsIgnoreCase("name")){
                sort = Sorting.NAME;
            } else if (args[2].equalsIgnoreCase("none")){
                sort = Sorting.NONE;
            } else {
                // Validate emoji's
                ArrayList<RoleAssignRole> roles = dh.getRoles(guildId, args[0]);
                int i = 0;
                String[] emojis = args[2].split(" ");
                boolean correct = true;
                while (correct && i < emojis.length){
                    int finalI = i;
                    if (roles.stream().noneMatch(r -> r.getEmoji().equals(emojis[finalI]))){
                        correct = false;
                    }
                    i++;
                }
                if (!correct)
                    throw new MessageException(language.getString("roleassign.error.sorting", args[2]));
                sort = Sorting.CUSTOM;
                data.setCustomS(args[2]);
            }
            data.setSorting(sort);
            if (args.length == 4){
                if (args[3].equalsIgnoreCase("compact")){
                    compact = Compacting.COMPACT;
                } else if (args[3].equalsIgnoreCase("supercompact")){
                    compact = Compacting.SUPER_COMPACT;
                } else if (!args[3].equalsIgnoreCase("normal")){
                    throw new MessageException(language.getString("roleassign.edit.error.compacting", args[1]));
                } else {
                    compact = Compacting.NORMAL;
                }
                data.setCompacting(compact);
            } else {
                compact = Objects.requireNonNullElse(data.getCompacting(), Compacting.NORMAL);
            }
            dh.setCompacting(guildId, args[0], compact, sort == Sorting.CUSTOM ? args[2] : sort.toString());
            editEmbed(data, e.getGuild(), args[0], dh, language);
        } else if (args[1].equalsIgnoreCase("title")){
            StringBuilder name = new StringBuilder();
            for (int i = 2; i < args.length; i++){
                name.append(args[i]).append(" ");
            }
            dh.setTitle(guildId, args[0], name.toString().trim());
            e.getMessage().addReaction(Utils.config.getProperty("emoji.checkmark")).queue();
        } else if (hasEmoji(e.getMessage(), args[1])){
            StringBuilder name = new StringBuilder();
            for (int i = 2; i < args.length; i++){
                name.append(args[i]).append(" ");
            }
            boolean succeeded = dh.editRoleName(guildId, args[0], args[1], name.toString().trim());
            MyProperties config = Utils.config;
            if (!succeeded){
                e.getMessage().addReaction(config.getProperty("emoji.cancel")).queue();
                throw new MessageException(language.getString("roleassign.edit.error.emoji", args[0]));
            }
            e.getMessage().addReaction(config.getProperty("emoji.checkmark")).queue();
            RoleAssignData data = dh.getRoleAssignData(guildId, args[0]);
            editEmbed(data, e.getGuild(), args[0], dh, language);

        } else {
            throw new MessageException(language.getString("roleassign.error.emoji", args[1]));
        }

    }

    private void editEmbed(RoleAssignData data, Guild guild, String category, RRDataHandler dh, MyResourceBundle language){
        if (data.getMessageId() != null){
            guild.getTextChannelById(data.getChannelId()).retrieveMessageById(data.getMessageId()).queue(m -> {
                if (m != null){
                    EmbedBuilder eb = getRoleEmbed(dh.getRoles(guild.getIdLong(), category), category, data, language);
                    m.editMessageEmbeds(eb.build()).queue();
                }
            });
        }
    }


}

