package commands.casino.bet;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import companions.GameCompanion;
import companions.Question;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

public class Blackbox extends Command {


    private final GameCompanion gameCompanion;

    public Blackbox(GameCompanion gameCompanion){
        this.gameCompanion = gameCompanion;
        this.name = "blackbox";
        this.description = "blackbox.description";
        this.arguments = new String[]{"<question>"};
        this.category = Category.CASINO;
        this.example = "Who will marry first";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (args.length == 0)
            throw new MessageException(language.getString("start_bet.error.no_question"));

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(e.getGuild().getSelfMember().getColor());
        eb.setAuthor(e.getAuthor().getName(), null, e.getAuthor().getAvatarUrl());
        String question = Utils.concat(args, 0);
        eb.setTitle(question);
        Question<String> bet = gameCompanion.addBlackBox(e.getGuild().getIdLong(), e.getAuthor().getIdLong(), question);
        eb.setDescription(language.getString("blackbox.embed.description", bet.getID()));
        eb.setFooter(language.getString("start_bet.footer", bet.getID()));
        e.getChannel().sendMessageEmbeds(eb.build()).queue(m -> bet.setIds(m.getChannel().getIdLong(), m.getIdLong()));
        e.getMessage().delete().queue();
    }
}
