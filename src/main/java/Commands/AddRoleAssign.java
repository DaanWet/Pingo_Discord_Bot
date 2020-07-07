package Commands;

import Utils.DataHandler;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class AddRoleAssign extends Command{

    private DataHandler dataHandler;

    public AddRoleAssign() {
        name = "addRoleAssign";
        aliases = new String[]{"addRole", "addRoleA", "addRA"};
        category = "Moderation";
        dataHandler = new DataHandler();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length >= 4 && args[0].equalsIgnoreCase("Gaming") && (EmojiManager.containsEmoji(args[1])) || (e.getMessage().getEmotes().size() == 1 && e.getMessage().getEmotes().get(0).getAsMention().equals(args[1]))){
            Role role = null;
            try {
                role = e.getMessage().getMentionedRoles().size() == 0 ? e.getGuild().getRoleById(args[2]) : e.getMessage().getMentionedRoles().get(0);
            } catch (Exception exc){
                e.getChannel().sendMessage("Usage: !addRoleAssign <type> <emoji> <role> <name>").queue();
            }
            StringBuilder name = new StringBuilder();
            for (int i = 3; i < args.length; i++){
                name.append(args[i]).append(" ");
            }
            if (role != null){
                dataHandler.addRoleAssign(args[0], args[1].substring(1, args[1].length() - 1), name.toString().trim() ,role.getIdLong());
            }
        } else {
            e.getChannel().sendMessage("Usage: !addRoleAssign <type> <emoji> <role> <name>").queue();
        }
    }

    @Override
    public String getDescription() {
        return null;
    }
}
