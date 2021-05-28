package commands.roles;

import commands.Command;
import commands.settings.Setting;
import utils.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class RemoveRoleAssign extends Command {


    public RemoveRoleAssign() {
        this.name = "removeRoleAssign";
        this.aliases = new String[]{"removeRole", "removeRoleA", "removeRA"};
        this.category = "Moderation";
        this.arguments = "<emoji>";
        this.description = "Removes a role from the board";
    }

    @Override
    public boolean canBeExecuted(long guildId, long channelId, long userId){
        DataHandler dataHandler = new DataHandler();
        Boolean setting = dataHandler.getBoolSetting(guildId, Setting.ROLEASSIGN);
        return setting == null || setting;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (/*e.getMessage().getEmotes().size() == 1&& */args.length >= 2) {
            DataHandler dataHandler = new DataHandler();
            long[] message = dataHandler.getMessage(e.getGuild().getIdLong(), args[0]);
            String emote = args[1].replaceFirst("<", "").replaceFirst(">$", "");
            if (message != null){
                e.getGuild().getTextChannelById(message[0]).retrieveMessageById(message[1]).queue(m -> {
                    MessageEmbed me = m.getEmbeds().get(0);
                    EmbedBuilder eb = new EmbedBuilder(me);
                    ArrayList<String> lines = new ArrayList<>(Arrays.asList(me.getDescription().split("\n")));
                    int i = 0;
                    boolean found = false;
                    while (!found && i < lines.size()) {
                        if (lines.get(i).contains(args[1])) {
                            found = true;
                        } else {
                            i++;
                        }
                    }
                    if (found) {
                        lines.remove(i);
                        lines.remove(i - 1);
                        eb.setDescription(String.join("\n", lines));
                        m.editMessage(eb.build()).queue();
                        m.removeReaction(emote).queue();
                        e.getMessage().addReaction("✅").queue();
                        for (Member member : e.getGuild().getMembers()) {
                            m.removeReaction(emote, member.getUser()).queue();
                        }
                        e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
                    }
                });
            }
            boolean found = dataHandler.removeRoleAssign(e.getGuild().getIdLong(), args[0], args[1]);
            if (!found){
                e.getChannel().sendMessage("No matching role found").queue(mes -> mes.delete().queueAfter(15, TimeUnit.SECONDS));
            }

        }
    }
}
