package commands.roles;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

public class EditRoleAssign extends RoleCommand {

    public EditRoleAssign() {
        this.name = "editRoleAssign";
        this.category = "moderation";
        this.aliases = new String[]{"editRA"};
        this.arguments = "<category> sort {emoji|name|none} {compact|supercompact|normal}\n<category> <emoji> <name>";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        DataHandler dh = new DataHandler();
        long guildId = e.getGuild().getIdLong();
        if (args.length >= 3 && dh.getRoleCategories(guildId).contains(args[0])) {
            if (args[1].equalsIgnoreCase("sort")) {
                final Compacting compact;
                final Sorting sort;
                if (args[2].equalsIgnoreCase("emoji")) {
                    sort = Sorting.EMOJI;
                } else if (args[2].equalsIgnoreCase("name")) {
                    sort = Sorting.NAME;
                } else if (!args[2].equalsIgnoreCase("none")) {
                    e.getChannel().sendMessage(String.format("%s is not an valid sorting method", args[1])).queue();
                    return;
                } else {
                    sort = Sorting.NONE;
                }
                if (args.length == 4) {
                    if (args[3].equalsIgnoreCase("compact")) {
                        compact = Compacting.COMPACT;
                    } else if (args[3].equalsIgnoreCase("supercompact")) {
                        compact = Compacting.SUPER_COMPACT;
                    } else if (!args[3].equalsIgnoreCase("normal")) {
                        e.getChannel().sendMessage(String.format("%s is not an valid compacting method", args[1])).queue();
                        return;
                    } else {
                        compact = Compacting.NORMAL;
                    }
                } else {
                    compact = Compacting.NORMAL;
                }
                long[] message = dh.getMessage(guildId, args[0]);
                e.getGuild().getTextChannelById(message[0]).retrieveMessageById(message[1]).queue(m -> {
                    if (m != null) {
                        EmbedBuilder eb = getRoleEmbed(dh.getRoles(guildId, args[0]), args[0], sort, compact);
                        m.editMessage(eb.build()).queue();
                    }
                });
            } else {
                //Invalid sorting type
                //TODO: show error
            }
        } else if (hasEmoji(e.getMessage(), args[1])) {
            //TODO: Write name update code
        }
    }
}

