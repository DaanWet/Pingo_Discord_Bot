package me.damascus2000.pingo.commands.casino.bet;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.commands.settings.CommandState;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.Question;
import me.damascus2000.pingo.data.handlers.CreditDataHandler;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class Bet extends Command {

    private final GameCompanion gameCompanion;

    public Bet(GameCompanion gameCompanion){
        this.gameCompanion = gameCompanion;
        this.name = "bet";
        this.arguments = new String[]{"<bet id> <credits> <answer>"};
        this.description = "bet.description";
        this.category = Category.CASINO;
        this.example = "2 100 \"Tomorrow 10:00\"";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        CommandState betting = canBeExecuted(guildId, channelId, member, Setting.BETTING);
        CommandState custom = canBeExecuted(guildId, channelId, member, Setting.CUSTOMBET);
        return betting.worst(custom);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        MyResourceBundle language = getLanguage(e);
        int id = args.length == 0 ? -1 : Utils.getInt(args[0]);
        Question<Pair<Integer, String>> customBet = gameCompanion.getCustomBet(e.getGuild().getIdLong(), id);
        if (id == -1 || customBet == null)
            throw new MessageException(language.getString("bet.error.id"));


        if (customBet.isEnded())
            throw new MessageException(language.getString("bet.error.ended", id));

        if (customBet.didAnswer(e.getAuthor().getIdLong()))
            throw new MessageException(language.getString("bet.error.already_made"));

        long userId = e.getAuthor().getIdLong();
        int bet = args.length == 1 ? 0 : Utils.getInt(args[1]);
        if (bet < 10)
            throw new MessageException(language.getString("credit.error.least"));

        if (new CreditDataHandler().getCredits(e.getGuild().getIdLong(), userId) < bet)
            throw new MessageException(language.getString("credit.error.not_enough", bet));

        if (args.length == 2)
            throw new MessageException(language.getString("bet.error.answer"));


        String answer = Utils.concat(args, 2);
        customBet.addAnswer(userId, Pair.of(bet, answer));
        e.getMessage().addReaction(Utils.config.getProperty("emoji.checkmark")).queue();
        e.getGuild().getTextChannelById(customBet.getChannelId()).retrieveMessageById(customBet.getMessageId()).queue(m -> {
            MessageEmbed me = m.getEmbeds().get(0);
            EmbedBuilder eb = new EmbedBuilder(me);
            eb.appendDescription("\n").appendDescription(language.getString("bet.success", String.format("<@!%d>", userId), bet, answer));
            m.editMessageEmbeds(eb.build()).queue();
        });

    }
}
