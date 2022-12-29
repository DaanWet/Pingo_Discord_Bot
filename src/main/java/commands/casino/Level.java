package commands.casino;

import commands.Command;
import data.handlers.GeneralDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.Utils;

import java.util.Collections;

public class Level extends Command {

    public Level(){
        this.name = "level";
        this.description = "level.description";
        this.category = Category.OTHER;
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        GeneralDataHandler dataHandler = new GeneralDataHandler();
        int xp = dataHandler.getXP(e.getGuild().getIdLong(), e.getAuthor().getIdLong());
        int level = Utils.getLevel(xp);
        int total = (int) Math.ceil(Utils.piecewise.value(level));
        int progress = xp - Utils.getXP(level);
        double percent =(double) progress / total;
        int number = (int) Math.round(percent * 15);

        String p = String.join("", Collections.nCopies(number, "+"));
        String m = String.join("", Collections.nCopies(15 - number, "-"));
        String progressbar = String.format("[%s%s] (%d/%dXP)", p, m, progress, total);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(e.getMember().getColor());
        eb.addField(String.format("Level %d", level), progressbar + "\nTotal xp: " + total, false);
        eb.setAuthor(e.getMember().getEffectiveName(), null, e.getAuthor().getAvatarUrl());
        e.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
}
