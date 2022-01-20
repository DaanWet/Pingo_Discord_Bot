package commands.suggestion;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.kohsuke.github.*;
import utils.MessageException;
import utils.MyResourceBundle;

import java.util.List;

public class ListIssues extends Command {

    private final GitHub gitHub;

    public ListIssues(GitHub gitHub){
        this.gitHub = gitHub;
        this.name = "issues";
        this.arguments = "{**bot** | **plugin**} [-l]";
        this.description = "issues.description";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        if (args.length == 0 || args.length > 2)
            throw new MessageException(getUsage());

        GHRepository repo;
        if (args[0].equalsIgnoreCase("bot")){
            repo = gitHub.getRepository("DaanWet/Pingo_Discord_Bot");
        } else if (args[0].equalsIgnoreCase("plugin")){
            repo = gitHub.getRepository("DaanWet/MinecraftTeamsPlugin");
        } else {
            throw new MessageException(getUsage());
        }


        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder sb = new StringBuilder();
        MyResourceBundle language = getLanguage(e);
        if (args.length == 1){
            List<GHIssue> issues = repo.listIssues(GHIssueState.OPEN).toList();
            eb.setTitle(language.getString("issues.title", repo.getName()));
            for (GHIssue issue : issues){
                sb.append(":small_blue_diamond:").append(issue.getTitle()).append("\n");
            }
        } else if (args[1].equalsIgnoreCase("-l")){
            List<GHLabel> labels = repo.listLabels().toList();
            eb.setTitle(language.getString("issues.label", repo.getName()));
            for (GHLabel label : labels){
                sb.append(":small_blue_diamond:").append(label.getName()).append("\n");
            }
        } else {
            throw new MessageException(getUsage());
        }
        eb.setFooter(repo.getHtmlUrl().toString());
        eb.setDescription(sb.toString());
        e.getChannel().sendMessage(eb.build()).queue();


    }
}
