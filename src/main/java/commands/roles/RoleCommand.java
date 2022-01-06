package commands.roles;

import com.vdurmont.emoji.EmojiManager;
import commands.Command;
import data.models.RoleAssignData;
import data.models.RoleAssignRole;
import emoji4j.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public abstract class RoleCommand extends Command {

    public enum Sorting {
        EMOJI,
        NAME,
        CUSTOM,
        NONE;

        public static boolean isSort(String value){
            return Arrays.stream(Sorting.values()).map(Enum::toString).anyMatch(s -> s.equalsIgnoreCase(value));
        }
    }

    public enum Compacting {
        COMPACT,
        SUPER_COMPACT,
        NORMAL
    }

    public RoleCommand(){
        this.category = "moderation";
    }

    protected EmbedBuilder getRoleEmbed(ArrayList<RoleAssignRole> roles, String category, RoleAssignData data){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(data.getTitle() == null ? String.format("%s Roles", category) : data.getTitle());
        StringBuilder sb = new StringBuilder(String.format("Get your %s roles here, react to get the role", category));
        ArrayList<RoleAssignRole> sorted;
        switch (data.getSorting()){
            case EMOJI -> sorted = (ArrayList<RoleAssignRole>) roles.stream().sorted(Comparator.comparing(RoleAssignRole::getEmoji)).collect(Collectors.toList());
            case NAME -> sorted = (ArrayList<RoleAssignRole>) roles.stream().sorted(Comparator.comparing(RoleAssignRole::getName)).collect(Collectors.toList());
            case CUSTOM -> {
                sorted = new ArrayList<>();
                for (String s : data.getCustomS().split(" ")){
                    RoleAssignRole r = null;
                    int i = 0;
                    while (r == null && i < roles.size()){
                        if (roles.get(i).getEmoji().equalsIgnoreCase(s)){
                            r = roles.get(i);
                            roles.remove(i);
                        }
                        i++;
                    }
                    sorted.add(r);
                }
                sorted.addAll(roles);
            }
            default -> sorted = roles;
        }
        Compacting compact = data.getCompacting();
        if (compact == Compacting.COMPACT || compact == Compacting.SUPER_COMPACT){
            StringBuilder sb1 = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            for (int i = 0; i < sorted.size(); i++){
                if (i <= sorted.size() / 2){
                    sb1.append(sorted.get(i).getEmoji()).append("\t").append(sorted.get(i).getName()).append("\n");
                    if (compact == Compacting.COMPACT){
                        sb1.append("\n");
                    }
                } else {
                    sb2.append(sorted.get(i).getEmoji()).append("\t").append(sorted.get(i).getName()).append("\n");
                    if (compact == Compacting.COMPACT){
                        sb2.append("\n");
                    }
                }
            }
            eb.addField("", sb1.toString().trim(), true);
            eb.addField("", sb2.toString().trim(), true);
        } else {
            sorted.forEach(role -> sb.append("\n\n").append(role.getEmoji()).append("\t").append(role.getName()));
        }
        eb.setDescription(sb.toString());
        return eb;
    }

    protected boolean hasEmoji(Message message, String arg){
        return EmojiUtils.isEmoji(arg) || EmojiManager.containsEmoji(arg) || (message.getEmotes().size() == 1 && message.getEmotes().get(0).getAsMention().equals(arg));
    }

    protected Compacting detectCompact(MessageEmbed me){
        if (me.getFields().size() > 0){
            if (me.getFields().get(0).getValue().contains("\n\n")){
                return Compacting.SUPER_COMPACT;
            } else {
                return Compacting.COMPACT;
            }
        } else {
            return Compacting.NORMAL;
        }
    }
}
