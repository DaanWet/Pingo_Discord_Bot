package commands;


import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class FuckPingo extends Command{

    public FuckPingo() {
        this.name = "fuckpingo";
        this.description = "Fuck pingo";
    }

    public void setNickName(String name){
        aliases = new String[]{String.format("fuck%s", name.toLowerCase())};
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        e.getChannel().sendMessage("No, Fuck You").queue();
    }
}
