package commands.suggestion;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

public class EditSuggestion extends Command {

    public EditSuggestion(){
        this.name = "editSuggestion";
        this.aliases = new String[]{"editIssue", "editSuggest", "editI"};
        this.category = Category.MODERATION;
        this.arguments = new String[]{"<messageId> {**-r** | **-t** | **-d** | **-l**} <edit>"};
        this.description = "suggestion.edit.description";
        this.example = "123456789101112131 -d \"This is the new Description\"";
        this.priveligedGuild = Utils.config.get("special.guild");
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long guildId = e.getGuild().getIdLong();
        if (args.length < 3)
            throw new MessageException(getUsage(guildId));

        Long messageid = Utils.isLong(args[0]);


        if (messageid == null)
            throw new MessageException(getUsage(guildId));

        if (!args[1].matches("(?i)-[rdtl]"))
            throw new MessageException(getUsage(guildId));

        String edit = Utils.concat(args, 2);
        MyResourceBundle language = getLanguage(e);
        e.getGuild().getTextChannelById(Utils.config.get("special.suggestion")).retrieveMessageById(messageid).queue(m -> {
            MessageEmbed me = m.getEmbeds().get(0);
            EmbedBuilder eb = new EmbedBuilder(me);
            if (args[1].equalsIgnoreCase("-r")){
                if (edit.equalsIgnoreCase("bot")){
                    eb.setFooter(language.getString("suggestion.footer", Utils.config.getProperty("repo.bot")));
                } else if (edit.equalsIgnoreCase("plugin")){
                    eb.setFooter(language.getString("suggestion.footer", Utils.config.getProperty("repo.plugin")));
                }
            } else if (args[1].equalsIgnoreCase("-t")){
                eb.setTitle(edit);
            } else if (args[1].equalsIgnoreCase("-d")){
                eb.setDescription(edit);
            } else if (args[1].equalsIgnoreCase("-l")){
                eb.clearFields();
                eb.addField(language.getString("suggestion.labels"), edit, false);
            }
            m.editMessageEmbeds(eb.build()).queue();
        });


    }
}
