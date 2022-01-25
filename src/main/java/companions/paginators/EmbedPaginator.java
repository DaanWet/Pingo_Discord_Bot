package companions.paginators;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import utils.Utils;

import javax.annotation.Nullable;
import java.util.Properties;
import java.util.function.Consumer;

public abstract class EmbedPaginator {

    /**
     * Current page of the embed
     * 1 is first page and -1 is last page
     */
    protected int page = 1;

    public abstract MessageEmbed createEmbed();

    public MessageEmbed createEmbed(int page){
        this.page = page;
        return createEmbed();
    }

    public void sendMessage(TextChannel channel){
        sendMessage(channel, null);
    }

    public void sendMessage(TextChannel channel, @Nullable Consumer<Message> consumer){
        channel.sendMessageEmbeds(createEmbed())
                .queue(m ->
                       {
                           if (consumer != null)
                               consumer.accept(m);
                           Properties config = Utils.config;
                           m.addReaction(config.getProperty("emoji.first")).queue();
                           m.addReaction(config.getProperty("emoji.previous")).queue();
                           m.addReaction(config.getProperty("emoji.next")).queue();
                           m.addReaction(config.getProperty("emoji.last")).queue();
                       });
    }


    public void nextPage(){
        page++;
    }

    public void previousPage(){
        if (page != 1)
            page--;
    }

    public void firstPage(){
        page = 1;
    }

    public void lastPage(){
        page = -1;
    }

    public int getPage(){
        return page;
    }

}
