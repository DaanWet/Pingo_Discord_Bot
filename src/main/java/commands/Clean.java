package commands;

import commands.settings.Setting;
import data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.time.LocalDateTime;

public class Clean extends Command {

    public Clean(){
        this.name = "clean";
        this.category = Category.MODERATION;
        this.aliases = new String[]{"cleanChannel"};
        this.description = "clean.description";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        e.getChannel().getIterableHistory().queue(list -> {
            list.forEach(m -> {
                if (m.getContentRaw().equals("[Original Message Deleted]")) m.delete().queue();
            });
            e.getMessage().delete().queue();
        });
        new SettingsDataHandler().setCooldown(e.getGuild().getIdLong(), e.getAuthor().getIdLong(), Setting.CLEAN, LocalDateTime.now());
    }
}
