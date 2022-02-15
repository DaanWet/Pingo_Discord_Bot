package commands;


import commands.settings.Setting;
import data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.time.LocalDateTime;

public class FuckPingo extends Command {

    public FuckPingo(){
        this.name = "fuckpingo";
        this.description = "fuckpingo.description";
    }

    public void setNickName(String name){
        aliases = new String[]{String.format("fuck%s", name.toLowerCase())};
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{

        e.getChannel().sendMessage(getLanguage(e).getString("fuckpingo.answer")).queue();
        new SettingsDataHandler().setCooldown(e.getGuild().getIdLong(), e.getAuthor().getIdLong(), Setting.FUCKPINGO, LocalDateTime.now());
    }
}
