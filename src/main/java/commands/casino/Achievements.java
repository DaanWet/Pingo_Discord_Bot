package commands.casino;

import commands.Command;
import companions.Achievement;
import data.handlers.AchievementHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MyResourceBundle;
import utils.Utils;

public class Achievements extends Command {


    public Achievements(){
        this.name = "achievements";
        this.description = "achievements.description";
        this.category = Category.OTHER;
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
            AchievementHandler handler = new AchievementHandler();
            long userId = e.getAuthor().getIdLong();
            for (Achievement achievement : Achievement.values()){
                if (achievement.isHidden())
                    hidden++;
                else {
                    eb.appendDescription(language.getString(achievement.getTitle()));
                    boolean unlocked = handler.hasAchieved(guildId, userId, achievement);
                    eb.appendDescription(" - ").appendDescription(language.getString(unlocked ? "achievements.unlocked" : "achievements.locked"));
                    eb.appendDescription("\n");
                }
            }
            if (hidden > 0)
                eb.setFooter(language.getString("achievements.embed.hidden", hidden));
            e.getChannel().sendMessageEmbeds(eb.build()).queue();
        }
    }
}
