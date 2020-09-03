package commands.casino;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import utils.DataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShowCredits extends Command {

    private DataHandler dataHandler;

    public ShowCredits() {
        this.name = "ShowCredits";
        this.aliases = new String[]{"bal", "credits", "balance"};
        this.category = "Casino";
        this.dataHandler = new DataHandler();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        if (args.length == 0) {
            e.getChannel().sendMessage(String.format("Your current balance is **%d**", dataHandler.getCredits(e.getAuthor().getId()))).queue();
        } else if (args.length == 1 && args[0].equalsIgnoreCase("top")) {
            HashMap<String, Integer> map = dataHandler.getAllCredits();
            Stream<Map.Entry<String, Integer>> stream = map.entrySet().stream().sorted((entry1, entry2) ->  entry2.getValue() - entry1.getValue());
            List<Map.Entry<String, Integer>> sorted = stream.collect(Collectors.toList());
            EmbedBuilder eb = new EmbedBuilder();
            StringBuilder sb = new StringBuilder();
            eb.setTitle("Leaderboard");
            e.getGuild().retrieveMembersByIds(sorted.stream().map(key -> Command.isLong(key.getKey())).collect(Collectors.toList())).onSuccess(list -> {
                Map<String, Member> m = list.stream().collect(Collectors.toMap(Member::getId, member -> member));
                for (int i = 0; i < sorted.size() && i <= 10; i++) {
                    sb.append("`").append(i+1).append(i == 10 ? ".`" : ". `  ")
                            .append(m.get(sorted.get(i).getKey()).getAsMention())
                            .append("  **: ").append(sorted.get(i).getValue()).append(" **\n");

                }
                eb.setDescription(sb.toString());
                e.getChannel().sendMessage(eb.build()).queue();
            });

        } else {
            e.getChannel().sendMessage(this.getUsage()).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Show your current credit balance";
    }
}
