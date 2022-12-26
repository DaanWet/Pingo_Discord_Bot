package commands.casino.bet;

import commands.Command;
import companions.GameCompanion;
import companions.Question;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EndBlackbox extends Command {

    private final GameCompanion gameCompanion;

    public EndBlackbox(GameCompanion gameCompanion){
        this.gameCompanion = gameCompanion;
        this.name = "endBlackbox";
        this.arguments = new String[]{"<id>"};
        this.aliases = new String[]{"ebb"};
        this.category = Category.CASINO;
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (args.length == 0)
            throw new MessageException(language.getString("blackbox.error.id"));

        if (!Utils.isInteger(args[0]))
            throw new MessageException(language.getString("blackbox.error.id"));
        Question<String> bb = gameCompanion.getBlackBox(Utils.getInt(args[0]));
        if (bb == null)
            throw new MessageException(language.getString("blackbox.error.id"));
        bb.end();
        HashMap<Long, String> answers = bb.getAnswers();
        HashMap<String, Integer> counted = new HashMap<>();
        //answers.values().stream().map(String::trim).distinct().collect(Collectors.toMap(Function.identity(), v -> Collections.frequency(answers.values(), v)));
        for (String answer : answers.values()){
            answer = answer.trim();
            counted.put(answer, counted.getOrDefault(answer, 0) + 1);
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(bb.getQuestion());
        List<Map.Entry<String, Integer>> sorted = counted.entrySet().stream().sorted((entry1, entry2) -> entry2.getValue() - entry1.getValue()).collect(Collectors.toList());
        for (Map.Entry<String, Integer> entry : sorted){
            eb.appendDescription(String.format("%s %s (%d)\n", Utils.config.getProperty("emoji.list.dot"), entry.getKey(), entry.getValue()));
        }
        e.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
}
