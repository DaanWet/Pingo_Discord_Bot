package commands.roles;

import commands.Command;
import utils.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class RemoveRoleAssign extends Command {

    private DataHandler dataHandler;

    public RemoveRoleAssign(){
        this.name = "removeRoleAssign";
        this.aliases = new String[]{"removeRole", "removeRoleA", "removeRA"};
        this.category = "Moderation";
        dataHandler = new DataHandler();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (e.getMessage().getEmotes().size() == 1){
            long[] message = dataHandler.getMessage("gaming");
            String emote = args[0].substring(1, args[0].length() - 1);
            System.out.println(emote);
            e.getGuild().getTextChannelById(message[0]).retrieveMessageById(message[1]).queue(m -> {
                MessageEmbed me = m.getEmbeds().get(0);
                EmbedBuilder eb = new EmbedBuilder(me);
                ArrayList<String> lines = new ArrayList<>(Arrays.asList(me.getDescription().split("\n")));
                int i = 0;
                boolean found = false;
                while (!found && i < lines.size()){
                    if (lines.get(i).contains(emote)){
                        found = true;
                    } else {
                        i++;
                    }
                }
                if (found){
                    lines.remove(i);
                    lines.remove(i - 1);
                    dataHandler.removeRoleAssign("gaming", emote);
                    eb.setDescription(String.join("\n", lines));
                    m.editMessage(eb.build()).queue();
                    m.removeReaction(emote).queue();
                    e.getMessage().addReaction(":green_tick:667450925677543454").queue();
                    for (Member member : e.getGuild().getMembers()){
                        m.removeReaction(emote, member.getUser()).queue();
                    }

                    e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
                } else {
                    e.getChannel().sendMessage("No matching role found").queue(mes -> mes.delete().queueAfter(15, TimeUnit.SECONDS));
                }
            });
        }
    }

    @Override
    public String getDescription() {
        return "Removes a role from the board";
    }
}