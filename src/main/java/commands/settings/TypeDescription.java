package commands.settings;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

public interface TypeDescription {

    EmbedBuilder getDescription(List<Setting> s, EmbedBuilder eb, String prefix);


}
