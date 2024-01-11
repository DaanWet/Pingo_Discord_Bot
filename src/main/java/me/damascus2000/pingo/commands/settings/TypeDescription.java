package me.damascus2000.pingo.commands.settings;

import net.dv8tion.jda.api.EmbedBuilder;
import me.damascus2000.pingo.utils.MyResourceBundle;

import java.util.List;

public interface TypeDescription {

    EmbedBuilder getDescription(List<Setting> s, EmbedBuilder eb, String prefix, MyResourceBundle language);


}
