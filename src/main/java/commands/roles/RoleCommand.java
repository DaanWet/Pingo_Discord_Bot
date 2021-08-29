package commands.roles;

import com.vdurmont.emoji.EmojiManager;
import commands.Command;
import emoji4j.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RoleCommand extends Command {

    public enum Sorting {
        EMOJI,
        NAME,
        NONE
    }

    public enum Compacting {
        COMPACT,
        SUPER_COMPACT,
        NORMAL
    }

    public RoleCommand(){
        this.category = "moderation";
    }

    protected EmbedBuilder getRoleEmbed(ArrayList<Triple<String, String, Long>> roles, String category, Sorting sort, Compacting compact, String title){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title == null ? String.format("%s Roles", category) : title);
        StringBuilder sb = new StringBuilder(String.format("Get your %s roles here, react to get the role", category));
        Stream<Triple<String, String, Long>> sorted;
        switch(sort){
            case EMOJI:
                sorted = roles.stream().sorted(Comparator.comparing(Triple::getLeft));
                break;
            case NAME:
                sorted = roles.stream().sorted(Comparator.comparing(Triple::getMiddle));
                break;
            default:
                sorted = roles.stream();
                break;

        }
        if (compact == Compacting.COMPACT || compact == Compacting.SUPER_COMPACT){
            StringBuilder sb1 = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            List<Triple<String, String, Long>> sr = sorted.collect(Collectors.toList());
            for (int i = 0; i < sr.size(); i++){
                if (i <= sr.size() / 2){
                    sb1.append(sr.get(i).getLeft()).append("\t").append(sr.get(i).getMiddle()).append("\n");
                    if (compact == Compacting.COMPACT) {
                        sb1.append("\n");
                    }
                } else {
                    sb2.append(sr.get(i).getLeft()).append("\t").append(sr.get(i).getMiddle()).append("\n");
                    if (compact == Compacting.COMPACT) {
                        sb2.append("\n");
                    }
                }
            }
            eb.addField("", sb1.toString().trim(), true);
            eb.addField("", sb2.toString().trim(), true);
        } else {
            sorted.forEachOrdered(role -> sb.append("\n\n").append(role.getLeft()).append("\t").append(role.getMiddle()));
        }
        eb.setDescription(sb.toString());
        return eb;
    }

    protected boolean hasEmoji(Message message, String arg){
        return EmojiUtils.isEmoji(arg) || EmojiManager.containsEmoji(arg) || (message.getEmotes().size() == 1 && message.getEmotes().get(0).getAsMention().equals(arg));
    }

    protected Compacting detectCompact(MessageEmbed me){
        if (me.getFields().size() > 0) {
            if (me.getFields().get(0).getValue().contains("\n\n")){
                return Compacting.SUPER_COMPACT;
            } else {
                return Compacting.COMPACT;
            }
        } else{
            return Compacting.NORMAL;
        }
    }
}
