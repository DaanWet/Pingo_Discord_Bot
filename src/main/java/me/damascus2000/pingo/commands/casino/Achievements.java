package me.damascus2000.pingo.commands.casino;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.companions.Achievement;
import me.damascus2000.pingo.models.AchievementCountDTO;
import me.damascus2000.pingo.services.AchievementService;
import me.damascus2000.pingo.services.MemberService;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Achievements extends Command {


    public Achievements(MemberService memberService, AchievementService achievementService){
        this.memberService = memberService;
        this.achievementService = achievementService;
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
                boolean unlocked = achievementService.hasAchieved(guildId, userId, achievement);
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
        } else if (args.length == 1 && args[0].matches("(?i)^(top|global)$")){
            boolean global = args[0].equalsIgnoreCase("global");
            achievementService.getAchievementCount();
            List<AchievementCountDTO> map = global ? achievementService.getAchievementCount() : achievementService.getAchievementCount(guildId);
            map = map.stream().sorted(Comparator.comparingLong(AchievementCountDTO::getCount).reversed()).collect(Collectors.toList());
            EmbedBuilder eb = new EmbedBuilder();
            StringBuilder sb = new StringBuilder();
            int players = global ? memberService.getPlayerCount() : memberService.getPlayerCount(guildId);
            for (AchievementCountDTO entry : map){
                Achievement ach = entry.getAchievement();
                sb.append(ach.getType().emoji).append(" ").append(language.getString(ach.getTitle())).append(": ").append(entry.getCount()).append(" (").append(entry.getCount() * 100.0 / players).append("%)\n");
            }
            eb.setDescription(sb.toString());
            eb.setTitle(language.getString(global ? "achievements.embed.global" : "achievements.embed.server"));
            e.getChannel().sendMessageEmbeds(eb.build()).queue();
        }
    }
}
