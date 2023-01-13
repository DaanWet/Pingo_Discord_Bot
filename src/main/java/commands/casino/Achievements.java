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
            AchievementHandler handler = new AchievementHandler();
            long userId = e.getAuthor().getIdLong();
            Achievement.Type type = Achievement.values()[0].getType();
            StringBuilder sb = new StringBuilder();
            for (Achievement achievement : Achievement.values()){
                if (achievement.isHidden())
                    hidden++;
                else {
                    if (type != achievement.getType()){
                        eb.addField(type.description, sb.toString(), false);
                        type = achievement.getType();
                        sb = new StringBuilder();
                    }
                    boolean unlocked = handler.hasAchieved(guildId, userId, achievement);
                    sb.append(unlocked ? type.emoji : language.getString("achievements.locked")).append(" ").append(language.getString(achievement.getTitle()));
                    if (unlocked)
                        sb.append(": ||").append(language.getString(achievement.getDescription())).append("||");
                    else
                        sb.append(": ???");
                    sb.append("\n");
                }
            }
            eb.addField(type.description, sb.toString(), false);
            if (hidden > 0)
                eb.setFooter(language.getString("achievements.embed.hidden", hidden));
            e.getChannel().sendMessageEmbeds(eb.build()).queue();
        }
    }
}
