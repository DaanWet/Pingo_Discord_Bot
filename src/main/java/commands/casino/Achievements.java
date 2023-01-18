package commands.casino;

import commands.Command;
import companions.Achievement;
import data.handlers.AchievementHandler;
import data.handlers.CreditDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Achievements extends Command {


    public Achievements(){
        this.name = "achievements";
        this.description = "achievements.description";
        this.category = Category.OTHER;
        this.beta = true;
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        if (args.length == 0){
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(language.getString("achievements.embed.title", e.getMember().getEffectiveName()));
            eb.setColor(e.getMember().getColor());
            int hidden = 0;
            int more = -1;
            AchievementHandler handler = new AchievementHandler();
            long userId = e.getAuthor().getIdLong();
            Achievement.Type type = Achievement.values()[0].getType();
            StringBuilder sb = new StringBuilder();
            for (Achievement achievement : Achievement.values()){
                if (type != achievement.getType()){
                    if (hidden + more > 0)
                        sb.append(language.getString("achievements.embed.hidden", hidden + more));
                    hidden = 0;
                    more = -1;
                    eb.addField(type.description, sb.toString(), false);
                    type = achievement.getType();
                    sb = new StringBuilder();
                }
                boolean unlocked = handler.hasAchieved(guildId, userId, achievement);
                if (achievement.isHidden() && !unlocked){
                    hidden++;
                } else if (unlocked || more == -1){
                    sb.append(unlocked ? type.emoji : language.getString("achievements.locked")).append(" ").append(language.getString(achievement.getTitle()));
                    if (unlocked)
                        sb.append(": ||").append(language.getString(achievement.getDescription())).append("||");
                    else
                        sb.append(": ???");
                    sb.append("\n");
                    if (!unlocked)
                        more++;
                } else {
                    more++;
                }
            }
            if (hidden + more > 0)
                sb.append(language.getString("achievements.embed.hidden", hidden + more));
            eb.addField(type.description, sb.toString(), false);
            e.getChannel().sendMessageEmbeds(eb.build()).queue();
        } else if (args.length == 1 && args[0].matches("(?i)^(top|global)$")) {
            AchievementHandler handler = new AchievementHandler();
            boolean global = args[0].equalsIgnoreCase("global");
            HashMap<Achievement, Integer> map = global ? handler.getAchievementCount() : handler.getAchievementCount(guildId);
            Stream<Map.Entry<Achievement, Integer>> stream = map.entrySet().stream().sorted((entry1, entry2) -> entry2.getValue() - entry1.getValue());
            List<Map.Entry<Achievement, Integer>> sorted = stream.collect(Collectors.toList());
            EmbedBuilder eb = new EmbedBuilder();
            StringBuilder sb = new StringBuilder();
            CreditDataHandler creditDataHandler = new CreditDataHandler();
            int players = global ? creditDataHandler.getPlayers() : creditDataHandler.getPlayers(guildId);
            for (Map.Entry<Achievement, Integer> entry : sorted){
                Achievement ach = entry.getKey();
                sb.append(ach.getType().emoji).append(" ").append(language.getString(ach.getTitle())).append(": ").append(entry.getValue()).append(" (").append(entry.getValue() * 100.0/players).append("%)\n");
            }
            eb.setDescription(sb.toString());
            eb.setTitle(language.getString(global? "achievements.embed.global" : "achievements.embed.server"));
            e.getChannel().sendMessageEmbeds(eb.build()).queue();
        }
    }
}
