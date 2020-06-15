package Commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class FuckPingo extends Command{

    public FuckPingo() {
        name = "fuckpingo";
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
