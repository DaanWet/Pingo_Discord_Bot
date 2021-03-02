package commands.roles;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import commands.Command;
import emoji4j.EmojiUtils;
import utils.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

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
                } else {
                    e.getMessage().addReaction("❌").queue();
                    e.getChannel().sendMessage("Unable to add to database");
                }
            }
        } else {
            e.getChannel().sendMessage("Usage: !addRoleAssign <type> <emoji> <role> <name>").queue();
        }
    }
}
