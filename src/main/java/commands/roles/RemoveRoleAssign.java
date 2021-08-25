package commands.roles;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.tuple.Triple;
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


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        if (args.length == 2 && hasEmoji(e.getMessage(), args[1])) {
            DataHandler dataHandler = new DataHandler();
            long guildId = e.getGuild().getIdLong();
            String emote = args[1].replaceFirst("<", "").replaceFirst(">$", "");
            boolean found = dataHandler.removeRoleAssign(guildId, args[0], args[1]);
            if (!found) {
                e.getChannel().sendMessage("No matching role found").queue(mes -> mes.delete().queueAfter(15, TimeUnit.SECONDS));
            } else {
                RoleAssignData data = dataHandler.getRoleAssignData(guildId, args[0]);
                if (data.getMessageId() != null) {
                    e.getGuild().getTextChannelById(data.getChannelId()).retrieveMessageById(data.getMessageId()).queue(m -> {
                        MessageEmbed me = m.getEmbeds().get(0);
                        ArrayList<Triple<String, String, Long>> roles = dataHandler.getRoles(guildId, args[0]);
                        m.editMessage(getRoleEmbed(roles, args[0], data.getSorting(), data.getCompacting()).build()).queue();
                        e.getMessage().addReaction("✅").queue();
                        for (MessageReaction mr : e.getMessage().getReactions()) {
                            if (mr.getReactionEmote().getAsReactionCode().equals(emote)){
                                mr.clearReactions().queue();
                            }
                        }
                        e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                }
            }
        }
    }
}
