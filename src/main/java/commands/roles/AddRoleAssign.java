package commands.roles;

import org.apache.commons.lang3.tuple.Triple;
import utils.DataHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AddRoleAssign extends RoleCommand {

    public AddRoleAssign() {
        name = "addRoleAssign";
        aliases = new String[]{"addRole", "addRoleA", "addRA"};
        category = "Moderation";
        this.arguments = "<type> <emoji> <role> <name>";
        this.description = "Add a role to the role assigner";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (args.length >= 4 &&  hasEmoji(e.getMessage(), args[1])) {
            Role role = null;
            try {
                role = e.getMessage().getMentionedRoles().size() == 0 ? e.getGuild().getRoleById(args[2]) : e.getMessage().getMentionedRoles().get(0);
            } catch (Exception exc) {
                e.getChannel().sendMessage("Could not get the role \n Usage: !addRoleAssign <type> <emoji> <role> <name>").queue();
            }
            StringBuilder name = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                name.append(args[i]).append(" ");
            }
            if (role != null) {
                DataHandler dataHandler = new DataHandler();
                boolean succeeded = dataHandler.addRoleAssign(e.getGuild().getIdLong(), args[0], args[1], name.toString().trim(), role.getIdLong());
                if (succeeded){
                    e.getMessage().addReaction("✅").queue();
                    e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
                    RoleAssignData data = dataHandler.getRoleAssignData(e.getGuild().getIdLong() , args[0]);
                    String emote = args[1].replaceFirst("<", "").replaceFirst(">$", "");
                    if (data.getMessageId() != null) {
                        e.getGuild().getTextChannelById(data.getChannelId()).retrieveMessageById(data.getMessageId()).queue(m -> {
                            MessageEmbed me = m.getEmbeds().get(0);
                            ArrayList<Triple<String, String, Long>> roles = dataHandler.getRoles(e.getGuild().getIdLong(), args[0]);
                            m.editMessage(getRoleEmbed(roles, args[0], data.getSorting(), data.getCompacting(), data.getTitle()).build()).queue();
                            m.addReaction(emote).queue();
                        });
                    }
                } else {
                    e.getMessage().addReaction("❌").queue();
                    e.getChannel().sendMessage("Unable to add to database, that emoji is already used in the role picker").queue();
                }
            }
        } else {
            e.getChannel().sendMessage("Usage: !addRoleAssign <type> <emoji> <role> <name>").queue();
        }
    }
}
