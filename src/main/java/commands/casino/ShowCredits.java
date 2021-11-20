package commands.casino;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShowCredits extends Command {


    public ShowCredits() {
        this.name = "ShowCredits";
        this.aliases = new String[]{"bal", "credits", "balance"};
        this.category = "Casino";
        this.arguments = "[top]";
        this.description = "Show your current credit balance";
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        DataHandler dataHandler = new DataHandler();
        if (args.length == 0) {
            e.getChannel().sendMessage(String.format("Your current balance is **%d**", dataHandler.getCredits(e.getGuild().getIdLong(), e.getAuthor().getIdLong()))).queue();
        } else if (args.length == 1 && args[0].equalsIgnoreCase("top")) {
            Map<Long, Integer> map = Map.of(223837254118801408L, 50,738507567814279228L, 100, 484396841572499456L, 500); //dataHandler.getAllCredits(e.getGuild().getIdLong());
            Stream<Map.Entry<Long, Integer>> stream = map.entrySet().stream().sorted((entry1, entry2) -> entry2.getValue() - entry1.getValue());
            List<Map.Entry<Long, Integer>> sorted = stream.collect(Collectors.toList());
            EmbedBuilder eb = new EmbedBuilder();
            StringBuilder sb = new StringBuilder();
            eb.setTitle("Leaderboard");
            // e.getGuild().retrieveMembersByIds(sorted.stream().map(Map.Entry::getKey).collect(Collectors.toList())).onSuccess(list -> {
            //    Map<Long, Member> m = list.stream().collect(Collectors.toMap(Member::getIdLong, member -> member));
            for (int i = 0; i < sorted.size() && i < 10; i++) {
                sb.append("`").append(i + 1).append(i == 9 ? ".`" : ". `  ")
                        .append("<@!")
                        .append(sorted.get(i).getKey())
                        .append(">  **: ").append(sorted.get(i).getValue()).append(" **\n");

            }
            eb.setDescription(sb.toString());
            e.getChannel().sendMessage(eb.build()).queue();
            if (sorted.size() == 0) {
                eb.setDescription("No leaderboard yet, nobody has claimed credits yet.");
                e.getChannel().sendMessage(eb.build()).queue();
            }
        } else {
            e.getChannel().sendMessage(this.getUsage()).queue();
        }
    }
}
