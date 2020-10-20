package commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Clean extends Command {

    public Clean(){
        this.name = "clean";
        this.category = "Moderation";
        this.aliases = new String[]{"cleanChannel"};
        this.description = "Cleans up a channel";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        e.getChannel().getIterableHistory().queue(list -> {
            list.forEach(m -> {
                if (m.getContentRaw().equals("[Original Message Deleted]")) m.delete().queue();
            });
            e.getMessage().delete().queue();
        });
    }
}
