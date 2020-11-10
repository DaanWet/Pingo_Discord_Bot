package commands.roles;

import commands.Command;
import utils.DataHandler;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class AddRoleAssign extends Command {

    public AddRoleAssign() {
        name = "addRoleAssign";
        aliases = new String[]{"addRole", "addRoleA", "addRA"};
        category = "Moderation";
        this.arguments = "<type> <emoji> <role> <name>";
        this.description = "Add a role to the role assigner";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length >= 4 && (EmojiManager.containsEmoji(args[1])) || (e.getMessage().getEmotes().size() == 1 && e.getMessage().getEmotes().get(0).getAsMention().equals(args[1]))) {
            Role role = null;
            try {
                role = e.getMessage().getMentionedRoles().size() == 0 ? e.getGuild().getRoleById(args[2]) : e.getMessage().getMentionedRoles().get(0);
            } catch (Exception exc) {
                e.getChannel().sendMessage("Usage: !addRoleAssign <type> <emoji> <role> <name>").queue();
            }
            StringBuilder name = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                name.append(args[i]).append(" ");
            }
            if (role != null) {
                DataHandler dataHandler = new DataHandler();
                long[] message = dataHandler.getMessage(e.getGuild().getIdLong() ,args[0]);
                String emote = args[1].replaceFirst("<", "").replaceFirst(">$", "");
                if (message != null) {
                    e.getGuild().getTextChannelById(message[0]).retrieveMessageById(message[1]).queue(m -> {
                        MessageEmbed me = m.getEmbeds().get(0);
                        EmbedBuilder eb = new EmbedBuilder(me);
                        eb.setDescription(me.getDescription().concat(String.format("\n\n%s\t%s", args[1], name.toString())));
                        m.editMessage(eb.build()).queue();
                        m.addReaction(emote).queue();
                    });
                }
                dataHandler.addRoleAssign(e.getGuild().getIdLong(), args[0], args[1], name.toString().trim(), role.getIdLong());
                e.getMessage().addReaction("✅").queue();
                e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
            }
        } else {
            e.getChannel().sendMessage("Usage: !addRoleAssign <type> <emoji> <role> <name>").queue();
        }
    }
}
