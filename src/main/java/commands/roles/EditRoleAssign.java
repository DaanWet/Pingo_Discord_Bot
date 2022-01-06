package commands.roles;

import data.DataHandler;
import data.models.RoleAssignData;
import data.models.RoleAssignRole;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;

import java.util.ArrayList;
import java.util.Objects;

public class EditRoleAssign extends RoleCommand {

    public EditRoleAssign(){
        this.name = "editRoleAssign";
        this.category = "moderation";
        this.aliases = new String[]{"editRA"};
        this.arguments = "<category> sort {emoji|name|none|\"<custom_emoji_order>\"} {compact|supercompact|normal}\n<category> <emoji> <name>\n<category> {title} <newtitle>";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        DataHandler dh = new DataHandler();
        long guildId = e.getGuild().getIdLong();
        if (args.length == 0)
            throw new MessageException("No category given\n" + getUsage());
        if (args.length == 1)
            throw new MessageException("Please supply what you want to edit: sort, title or an emoji which name you want to edit\n" + getUsage());
        if (args.length == 2)
            throw new MessageException("No new value given");
        if (!dh.getRoleCategories(guildId).contains(args[0]))
            throw new MessageException("No valid category provided\n" + getUsage());

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
                    throw new MessageException(String.format("%s is not an valid sorting method", args[2]));
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
                    throw new MessageException(String.format("%s is not an valid compacting method", args[1]));
                } else {
                    compact = Compacting.NORMAL;
                }
                data.setCompacting(compact);
            } else {
                compact = Objects.requireNonNullElse(data.getCompacting(), Compacting.NORMAL);
            }
            dh.setCompacting(guildId, args[0], compact, sort == Sorting.CUSTOM ? args[2] : sort.toString());
            editEmbed(data, e.getGuild(), args[0], dh);
        } else if (args[1].equalsIgnoreCase("title")){
            StringBuilder name = new StringBuilder();
            for (int i = 2; i < args.length; i++){
                name.append(args[i]).append(" ");
            }
            dh.setTitle(guildId, category, name.toString().trim());
            e.getMessage().addReaction("✅").queue();
        } else if (hasEmoji(e.getMessage(), args[1])){
            StringBuilder name = new StringBuilder();
            for (int i = 2; i < args.length; i++){
                name.append(args[i]).append(" ");
            }
            boolean succeeded = dh.editRoleName(guildId, category, args[1], name.toString().trim());
            if (!succeeded){
                e.getMessage().addReaction("❌").queue();
                throw new MessageException(String.format("No such emoji for category %s", args[0]));
            }
            e.getMessage().addReaction("✅").queue();
            RoleAssignData data = dh.getRoleAssignData(guildId, args[0]);
            editEmbed(data, e.getGuild(), args[0], dh);

        } else {
            throw new MessageException(String.format("%s is not a valid emoji.\n%s", args[1], getUsage()));
        }

    }

    private void editEmbed(RoleAssignData data, Guild guild, String category, DataHandler dh){
        if (data.getMessageId() != null){
            guild.getTextChannelById(data.getChannelId()).retrieveMessageById(data.getMessageId()).queue(m -> {
                if (m != null){
                    EmbedBuilder eb = getRoleEmbed(dh.getRoles(guild.getIdLong(), category), category, data);
                    m.editMessage(eb.build()).queue();
                }
            });
        }
    }


}

