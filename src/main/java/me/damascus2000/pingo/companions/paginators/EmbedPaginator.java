package me.damascus2000.pingo.companions.paginators;

import me.damascus2000.pingo.utils.MyProperties;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class EmbedPaginator {

    /**
     * Current page of the embed
     * 0 is first page and -1 is last page
     */
    protected int page = 0;
    protected int maxPage = 0;

    public abstract MessageEmbed createEmbed(long guild);

    public MessageEmbed createEmbed(long guild, int page){
        this.page = page;
        return createEmbed(guild);
    }

    public void sendMessage(TextChannel channel){
        sendMessage(channel, null);
    }

    public void sendMessage(TextChannel channel, @Nullable Consumer<Message> consumer){
        channel.sendMessageEmbeds(createEmbed(channel.getGuild().getIdLong()))
                .queue(m ->
                       {
                           if (consumer != null)
                               consumer.accept(m);
                           MyProperties config = Utils.config;
                           m.addReaction(config.getProperty("emoji.first")).queue();
                           m.addReaction(config.getProperty("emoji.previous")).queue();
                           m.addReaction(config.getProperty("emoji.next")).queue();
                           m.addReaction(config.getProperty("emoji.last")).queue();
                       });
    }


    public void nextPage(){
        page = Math.min(page + 1, maxPage);
    }

    public void previousPage(){
        if (page != 0)
            page--;
    }

    public void firstPage(){
        page = 0;
    }

    public void lastPage(){
        page = maxPage;
    }

    public int getPage(){
        return page;
    }

}
