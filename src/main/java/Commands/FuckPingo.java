package Commands;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class FuckPingo extends Command{

    public FuckPingo() {
        name = "fuckpingo";
    }

    public void setNickName(String name){
        aliases = new String[]{String.format("fuck%s", name.toLowerCase())};
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        e.getChannel().sendMessage("No, Fuck You").queue();
    }

    @Override
    public String getDescription() {
        return "Fuck pingo";
    }
}
