package commands.suggestion;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.Utils;

public class EditSuggestion extends Command {

    public EditSuggestion(){
        this.name = "editSuggestion";
        this.aliases = new String[]{"editIssue", "editSuggest", "editI"};
        this.category = "Moderation";
        this.arguments = "<messageId> {**-r** | **-t** | **-d** | **-l**} <edit>";
        this.description = "Edits a suggestion";
        this.priveligedGuild = 203572340280262657L;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (args.length < 3)
            throw new MessageException(getUsage());

        Long messageid = Utils.isLong(args[0]);


        if (messageid == null)
            throw new MessageException(getUsage());

        if (!args[1].matches("(?i)-[rdtl]"))
            throw new MessageException(getUsage());

        String edit = Utils.concat(args, 2);
        e.getGuild().getTextChannelById(747228850353733739L).retrieveMessageById(messageid).queue(m -> {
            MessageEmbed me = m.getEmbeds().get(0);
            EmbedBuilder eb = new EmbedBuilder(me);
            if (args[1].equalsIgnoreCase("-r")){
                if (edit.equalsIgnoreCase("bot")){
                    eb.setFooter(String.format("Repo: %s", "DaanWet/Pingo_Discord_Bot"));
                } else if (args[0].equalsIgnoreCase("plugin")){
                    eb.setFooter(String.format("Repo: %s", "DaanWet/MinecraftTeamsPlugin"));
                }
            } else if (args[1].equalsIgnoreCase("-t")){
                eb.setTitle(edit);
            } else if (args[1].equalsIgnoreCase("-d")){
                eb.setDescription(edit);
            } else if (args[1].equalsIgnoreCase("-l")){
                eb.clearFields();
                eb.addField("Labels", edit, false);
            }
            m.editMessage(eb.build()).queue();
        });


    }
}
