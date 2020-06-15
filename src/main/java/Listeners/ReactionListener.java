package Listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class ReactionListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e){
        if (!e.getUser().isBot() && e.getChannel().equals(e.getGuild().getTextChannelById("664230911935512586"))){
            e.getChannel().retrieveMessageById(e.getMessageId()).queue(m -> {
                switch (e.getReactionEmote().getEmote().getId()){
                    case "667450925677543454":
                        m.removeReaction(":indifferent_tick:667450939208368130", e.getUser()).queue();
                        m.removeReaction(":red_tick:667450953217212436", e.getUser()).queue();
                        break;
                    case "667450939208368130":
                        m.removeReaction(":green_tick:667450925677543454", e.getUser()).queue();
                        m.removeReaction(":red_tick:667450953217212436", e.getUser()).queue();
                        break;
                    case "667450953217212436":
                        m.removeReaction(":green_tick:667450925677543454", e.getUser()).queue();
                        m.removeReaction(":indifferent_tick:667450939208368130", e.getUser()).queue();
                        break;
                }


                int pro = 0;
                int con = 0;
                int indif = 0;
                for (MessageReaction reaction : m.getReactions()){
                    switch (reaction.getReactionEmote().getEmote().getId()){
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
                if (pro >= 6 || con >= 6){
                    if (!embedList.isEmpty()){
                        MessageEmbed embed = embedList.get(0);
                        EmbedBuilder eb = new EmbedBuilder(embed);
                        if (embed.getFooter() == null || (embed.getFooter().getText().contains("Approved") && con >= 6) || (embed.getFooter().getText().contains("Rejected") && pro >= 6)){
                            eb.setFooter(String.format("Pro: %d, Con: %d => %s", pro, con, pro >= 6 ? "Approved" : "Rejected"));
                            eb.setTimestamp(LocalDateTime.now(ZoneId.systemDefault()));
                            m.editMessage(eb.build()).queue();
                        }
                    }
                }
            });
        }
    }
}
