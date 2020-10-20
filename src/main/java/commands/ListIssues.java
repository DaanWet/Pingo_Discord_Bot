package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListIssues extends Command {

    private GitHub gitHub;

    public ListIssues(GitHub gitHub) {
        this.gitHub = gitHub;
        this.name = "issues";
        this.arguments = "{**bot** | **plugin**} [-l]";
        this.description = "Show all issues of a repo";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 1 || args.length == 2) {
            try {
                GHRepository repo = null;
                if (args[0].equalsIgnoreCase("bot")) {
                    repo = gitHub.getRepository("DaanWet/Pingo_Discord_Bot");
                } else if (args[0].equalsIgnoreCase("plugin")) {
                    repo = gitHub.getRepository("DaanWet/MinecraftTeamsPlugin");
                }
                if (repo != null) {
                    EmbedBuilder eb = new EmbedBuilder();
                    StringBuilder sb = new StringBuilder();
                    if (args.length == 1) {
                        List<GHIssue> issues = repo.listIssues(GHIssueState.OPEN).toList();
                        eb.setTitle(String.format("%s issues", repo.getName()));
                        for (GHIssue issue : issues) {
                            sb.append(":small_blue_diamond:").append(issue.getTitle()).append("\n");
                        }
                    } else if (args[1].equalsIgnoreCase("-l")) {
                        List<GHLabel> labels = repo.listLabels().toList();
                        eb.setTitle(String.format("%s labels", repo.getName()));
                        for (GHLabel label : labels) {
                            sb.append(":small_blue_diamond:").append(label.getName()).append("\n");
                        }
                    } else {
                        e.getChannel().sendMessage(getUsage()).queue();
                    }
                    eb.setFooter(repo.getHtmlUrl().toString());
                    eb.setDescription(sb.toString());
                    e.getChannel().sendMessage(eb.build()).queue();
                } else {
                    e.getChannel().sendMessage(getUsage()).queue();
                }
            } catch (IOException | NullPointerException ioException) {
                e.getChannel().sendMessage(String.format("Oops, something went wrong: %s", ioException.getMessage())).queue();
                ioException.printStackTrace();
            }
        } else {
            e.getChannel().sendMessage(getUsage()).queue();
        }
    }
}
