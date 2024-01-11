package me.damascus2000.pingo.commands.casino.bet;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.commands.settings.CommandState;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.Question;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;

public class StartBet extends Command {

    private final GameCompanion gameCompanion;

    public StartBet(GameCompanion gameCompanion){
        this.gameCompanion = gameCompanion;
        this.name = "startbet";
        this.aliases = new String[]{"sbet"};
        this.category = Category.CASINO;
        this.arguments = new String[]{"<question>"};
        this.description = "start_bet.description";
        this.example = "When will Pedro wake up?";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        CommandState betting = canBeExecuted(guildId, channelId, member, Setting.BETTING);
        CommandState custom = canBeExecuted(guildId, channelId, member, Setting.CUSTOMBET);
        return betting.worst(custom);
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (args.length == 0)
            throw new MessageException(language.getString("start_bet.error.no_question"));

        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(e.getAuthor().getName(), null, e.getAuthor().getAvatarUrl());
        eb.setTitle(Utils.concat(args, 0));
        Question<Pair<Integer, String>> bet = gameCompanion.addCustomBet(e.getGuild().getIdLong(), e.getAuthor().getIdLong(), args[0]);
        eb.setFooter(language.getString("start_bet.footer", bet.getID()));
        e.getChannel().sendMessageEmbeds(eb.build()).queue(m -> bet.setIds(m.getChannel().getIdLong(), m.getIdLong()));
        e.getMessage().delete().queue();

    }
}
