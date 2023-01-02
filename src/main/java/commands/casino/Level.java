package commands.casino;

import commands.Command;
import companions.DataCompanion;
import companions.paginators.XPPaginator;
import data.handlers.GeneralDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.log4j.MDC;
import utils.MessageException;
import utils.Utils;

import java.util.Collections;

public class Level extends Command {


    private final DataCompanion handler;

    public Level(DataCompanion handler){
        this.name = "level";
        this.description = "level.description";
        this.arguments = new String[]{"[**top**|**global**]"};
        this.category = Category.OTHER;
        this.handler = handler;
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        GeneralDataHandler dataHandler = new GeneralDataHandler();
        if (args.length == 0){
            int xp = dataHandler.getXP(e.getGuild().getIdLong(), e.getAuthor().getIdLong());
            int currentLevel = Utils.getLevel(xp);
            int nextXP = Utils.getXP(currentLevel + 1);
            int previousXP = Utils.getXP(currentLevel);
            int xpGoal = nextXP - previousXP;
            int progress = xp - previousXP;
            double percent =(double) progress / xpGoal;
            int number = (int) Math.round(percent * 15);
            MDC.put("xp", xp);
            MDC.put("level", currentLevel);
            MDC.put("xpGoal", xpGoal);
            MDC.put("progress", progress);
            String p = String.join("", Collections.nCopies(number, "+"));
            String m = String.join("", Collections.nCopies(15 - number, "-"));
            String progressbar = String.format("[%s%s] (%d/%dXP)", p, m, progress, xpGoal);
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(e.getMember().getColor());
            eb.addField(String.format("Level %d", currentLevel), progressbar + "\nTotal xp: " + xp, false);
            eb.setAuthor(e.getMember().getEffectiveName(), null, e.getAuthor().getAvatarUrl());
            e.getChannel().sendMessageEmbeds(eb.build()).queue();
        } else if (args.length == 1 && args[0].matches("(?i)^(top|global)$")){
            boolean global = args[0].equalsIgnoreCase("global");
            XPPaginator paginator = new XPPaginator(global, e.getGuild().getIdLong());
            paginator.sendMessage(e.getChannel(), m -> handler.addEmbedPaginator(e.getGuild().getIdLong(), m.getIdLong(), paginator));
        } else {
            throw new MessageException(this.getUsage(e.getGuild().getIdLong()));
        }
    }

}
