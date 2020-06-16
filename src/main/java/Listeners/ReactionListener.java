package Listeners;

import Commands.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

import static Commands.CommandHandler.pathname;

public class ReactionListener extends ListenerAdapter {
    private CommandHandler commandHandler;
    private Random random = new Random();

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e) {
        User user = e.getUser();
        if (!user.isBot()) {
            if (e.getChannel().equals(e.getGuild().getTextChannelById("664230911935512586"))) {
                e.getChannel().retrieveMessageById(e.getMessageId()).queue(m -> {
                    switch (e.getReactionEmote().getEmote().getId()) {
                        case "667450925677543454":
                            m.removeReaction(":indifferent_tick:667450939208368130", user).queue();
                            m.removeReaction(":red_tick:667450953217212436", user).queue();
                            break;
                        case "667450939208368130":
                            m.removeReaction(":green_tick:667450925677543454", user).queue();
                            m.removeReaction(":red_tick:667450953217212436", user).queue();
                            break;
                        case "667450953217212436":
                            m.removeReaction(":green_tick:667450925677543454", user).queue();
                            m.removeReaction(":indifferent_tick:667450939208368130", user).queue();
                            break;
                    }
                    int pro = 0;
                    int con = 0;
                    int indif = 0;
                    for (MessageReaction reaction : m.getReactions()) {
                        switch (reaction.getReactionEmote().getEmote().getId()) {
                            case "667450925677543454":
                                pro = reaction.getCount();
                                break;
                            case "667450939208368130":
                                indif = reaction.getCount();
                                break;
                            case "667450953217212436":
                                con = reaction.getCount();
                                break;
                        }
                    }
                    List<MessageEmbed> embedList = m.getEmbeds();
                    if (pro >= 6 || con >= 6) {
                        if (!embedList.isEmpty()) {
                            MessageEmbed embed = embedList.get(0);
                            EmbedBuilder eb = new EmbedBuilder(embed);
                            if (embed.getFooter() == null || (embed.getFooter().getText().contains("Approved") && con >= 6) || (embed.getFooter().getText().contains("Rejected") && pro >= 6)) {
                                eb.setFooter(String.format("Pro: %d, Con: %d => %s", pro, con, pro >= 6 ? "Approved" : "Rejected"));
                                eb.setTimestamp(LocalDateTime.now(ZoneId.systemDefault()));
                                m.editMessage(eb.build()).queue();
                            }
                        }
                    }
                });
            } else {
                e.getChannel().retrieveMessageById(e.getMessageId()).queue(m -> {
                    if (m.getAuthor().isBot() && !m.getEmbeds().isEmpty()){
                        MessageEmbed me = m.getEmbeds().get(0);
                        if (me.getTitle() != null && me.getTitle().contains("Delete pictures from ")){
                            EmbedBuilder eb = new EmbedBuilder(me);
                            int n = Integer.parseInt(me.getDescription().substring(0, me.getDescription().indexOf('.')));
                            String command = me.getTitle().substring(me.getTitle().lastIndexOf(' ') + 1);
                            File dir = new File(String.format("%s/%s", pathname, command));
                            int max = dir.listFiles().length;
                            switch (e.getReaction().getReactionEmote().getEmoji()){
                                case "◀":
                                    if (n != 0){
                                        n--;
                                        eb.setImage(String.format("http://zwervers.wettinck.be/%s/%d&%d=%d", command, n, random.nextInt(), random.nextInt()));
                                        eb.setDescription(String.format("%d.jpg", n));
                                        m.editMessage(eb.build()).queue();
                                    }
                                    m.removeReaction("◀", user).queue();
                                    break;
                                case "\uD83D\uDDD1":
                                    //remove image

                                    File foto = new File(String.format("%s/%s/%d.jpg", pathname, command, n));
                                    foto.delete();
                                    for (int i = n + 1; i < max; i++){
                                        foto = new File(String.format("%s/%s/%d.jpg", pathname, command, i));
                                        foto.renameTo(new File(String.format("%s/%s/%d.jpg", pathname, command, i - 1)));
                                    }
                                    eb.setImage(String.format("http://zwervers.wettinck.be/%s/%d&%d=%d", command, n, random.nextInt(), random.nextInt()));
                                    m.editMessage(eb.build()).queue();
                                    m.removeReaction("\uD83D\uDDD1", user).queue();
                                    if (dir.listFiles().length == 0){
                                        dir.delete();
                                        m.delete().queue();
                                    }

                                    break;
                                case "▶":
                                    if (n != max - 1){
                                        n++;
                                        eb.setImage(String.format("http://zwervers.wettinck.be/%s/%d&%d=%d", command, n, random.nextInt(), random.nextInt()));
                                        eb.setDescription(String.format("%d.jpg", n));
                                        m.editMessage(eb.build()).queue();

                                    }
                                    m.removeReaction("▶", user).queue();
                                    break;
                                case "❌":
                                    m.delete().queue();
                                    break;

                            }
                        }
                    }
                });
            }
        }
    }
}
