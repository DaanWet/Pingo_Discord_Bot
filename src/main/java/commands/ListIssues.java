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
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 1) {
            try {
                GHRepository repo = null;

                if (args[0].equalsIgnoreCase("bot")) {

                    repo = gitHub.getRepository("DaanWet/Pingo_Discord_Bot");
                } else if (args[0].equalsIgnoreCase("plugin")){
                    repo = gitHub.getRepository("DaanWet/MinecraftTeamsPlugin");

                }
                if (repo != null){
                    List<GHIssue> issues = repo.listIssues(GHIssueState.OPEN).toList();
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle(String.format("%s issues", repo.getName()));
                    eb.setFooter(repo.getHtmlUrl().toString());
                    StringBuilder sb = new StringBuilder();
                    for (GHIssue issue : issues) {
                        sb.append(":small_blue_diamond:").append(issue.getTitle()).append("\n");
                    }
                    eb.setDescription(sb.toString());
                    e.getChannel().sendMessage(eb.build()).queue();
                }


            } catch (IOException | NullPointerException ioException) {
                e.getChannel().sendMessage(String.format("Oops, something went wrong: %s", ioException.getMessage())).queue();
                ioException.printStackTrace();
            }
        }
    }

    @Override
    public String getDescription() {
        return "Show all issues of a repo";
    }
}
