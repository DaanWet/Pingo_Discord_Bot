package commands;


import commands.settings.Setting;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

import java.time.LocalDateTime;

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
        new DataHandler().setCooldown(e.getGuild().getIdLong(), e.getAuthor().getIdLong(), Setting.FUCKPINGO, LocalDateTime.now());
    }
}
