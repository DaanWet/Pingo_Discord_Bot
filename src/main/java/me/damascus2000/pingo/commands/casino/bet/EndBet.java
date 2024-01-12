package me.damascus2000.pingo.commands.casino.bet;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.commands.settings.CommandState;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.Question;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.services.MemberService;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

@Component
public class EndBet extends Command {

    private final GameCompanion gameCompanion;
    private final MemberService memberService;

    public EndBet(GameCompanion gameCompanion, MemberService memberService){
        this.gameCompanion = gameCompanion;
        this.memberService = memberService;
        this.name = "endbet";
        this.aliases = new String[]{"ebet"};
        this.arguments = new String[]{"<bet id> <winners>"};
        this.description = "end_bet.description";
        this.example = "2 @Jef @Peter";
        this.category = Category.CASINO;
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        CommandState betting = canBeExecuted(guildId, channelId, member, Setting.BETTING);
        CommandState custom = canBeExecuted(guildId, channelId, member, Setting.CUSTOMBET);
        return betting.worst(custom);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        int id = args.length == 0 ? -1 : Utils.getInt(args[0]);
        Question<Pair<Integer, String>> customBet = gameCompanion.getCustomBet(guildId, id);
        if (id == -1 || customBet == null)
            throw new MessageException(language.getString("bet.error.id"));

        if (customBet.isEnded())
            throw new MessageException(language.getString("bet.error.ended", id));

        if (customBet.getUserId() != e.getAuthor().getIdLong())
            throw new MessageException(language.getString("end_bet.error.perm"));

        if (args.length == 1 || e.getMessage().getMentionedMembers().size() == 0)
            throw new MessageException(language.getString("end_bet.error.no_winner"));


        ArrayList<Long> winners = new ArrayList<>();
        for (Member m : e.getMessage().getMentionedMembers()){
            if (!customBet.didAnswer(m.getIdLong()))
                throw new MessageException(language.getString("end_bet.error.no_bet", m.getEffectiveName()));
            winners.add(m.getIdLong());
        }
        customBet.end();
        int winnerTotal = 0;
        int prize = 0;
        for (Map.Entry<Long, Pair<Integer, String>> entry : customBet.getAnswers().entrySet()){
            if (winners.contains(entry.getKey()))
                winnerTotal += entry.getValue().getLeft();
            else {
                memberService.addCredits(guildId, entry.getKey(), -entry.getValue().getLeft());
                prize += entry.getValue().getLeft();
            }
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(language.getString("end_bet.ended", customBet.getID()), String.format("https://discord.com/channels/%d/%d/%d", guildId, customBet.getChannelId(), customBet.getMessageId()));
        eb.appendDescription(language.getString("end_bet.prize", prize + winnerTotal));

        for (long winner : winners){
            double percentage = (double) customBet.getAnswer(winner).getLeft() / winnerTotal;
            int won = (int) (percentage * prize);
            memberService.addCredits(guildId, winner, won);
            eb.appendDescription("\n").appendDescription(language.getString("end_bet.winner", String.format("<@!%d>", winner), won, customBet.getAnswer(winner).getRight()));
        }
        e.getChannel().sendMessageEmbeds(eb.build()).queue();
        e.getMessage().delete().queue();


    }
}
