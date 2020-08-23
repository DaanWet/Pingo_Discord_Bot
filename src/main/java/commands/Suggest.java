package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class Suggest extends Command {

    public Suggest() {
        this.name = "suggest";
        this.aliases = new String[]{"issue", "suggestion"};
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length >= 4) {
            String repo;
            if (args[0].equalsIgnoreCase("bot")) {
                repo = "DaanWet/Pingo_Discord_Bot";
            } else if (args[0].equalsIgnoreCase("plugin")) {
                repo = "DaanWet/MinecraftTeamsPlugin";
            } else {
                repo = "";
            }
            boolean t = true;
            StringBuilder title = new StringBuilder();
            StringBuilder descript = new StringBuilder();

            for (int i = 1; i < args.length; i++) {
                if (t) {
                    if (args[i].equalsIgnoreCase("-d")) {
                        t = false;
                    } else {
                        title.append(args[i]).append(" ");
                    }
                } else {
                    descript.append(args[i]).append(" ");
                }
            }
            // If no description is given send error
            if (t) {
                e.getChannel().sendMessage("You need to add a description, Usage: !suggest [bot | plugin] <title> -d <description>").queue();
            } else {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setAuthor(e.getAuthor().getName(), null, e.getAuthor().getAvatarUrl());
                eb.setTitle(title.toString());
                eb.setDescription(descript.toString());
                eb.setFooter(repo.length() != 0 ? String.format("Repo: %s", repo) : "");
                e.getGuild().getTextChannelById(747228850353733739L).sendMessage(eb.build()).queue(m -> {
                    m.addReaction(":green_tick:667450925677543454").queue();
                    m.addReaction(":indifferent_tick:667450939208368130").queue();
                    m.addReaction(":red_tick:667450953217212436").queue();
                });
                e.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
            }
        } else {
            e.getChannel().sendMessage("False usage, use: !suggest [bot | plugin] <title> -d <description>").queue();
        }
    }

    @Override
    public String getDescription() {
        return "Create a new suggestion for the bot";
    }
}
