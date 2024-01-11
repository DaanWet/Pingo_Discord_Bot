package me.damascus2000.pingo.commands.settings;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.apache.commons.lang3.LocaleUtils;
import me.damascus2000.pingo.exceptions.EmbedException;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;

import java.util.*;

public class Settings extends Command {

    public Settings(){
        this.name = "settings";
        this.category = Category.MODERATION;
        this.description = "settings.description";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        SettingsDataHandler dataHandler = new SettingsDataHandler();
        long guildId = e.getGuild().getIdLong();
        String prefix = dataHandler.getStringSetting(guildId, Setting.PREFIX).get(0);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(e.getGuild().getSelfMember().getColor());
        MyResourceBundle language = Utils.getLanguage(guildId);
        if (args.length == 0){
            eb.setTitle(language.getString("settings.title", "Pingo"));
            for (Setting.Type type : Setting.Type.values()){
                eb.addField(type.getName(), language.getString("settings.category", type.getName(), prefix), true);
            }
            e.getChannel().sendMessageEmbeds(eb.build()).queue();
            return;
        }
        Setting.Type type = Setting.Type.fromString(args[0]);
        if (type == null)
            throw new EmbedException(language.getString("settings.error.category", args[0], prefix));


        if (args.length == 1){
            eb.setTitle(String.format("%s Settings", type.getName()));
            List<Setting> settings = Setting.getTypeMap().get(type);
            eb = type.getTypeDescription().getDescription(settings, eb, prefix, language);
            e.getChannel().sendMessageEmbeds(eb.build()).queue();
        } else {
            Setting setting = Setting.fromString(args[1], type);
            if (setting == null)
                throw new EmbedException(language.getString("settings.error.setting", args[1], prefix, args[0], args[0]));

            String name = Utils.upperCaseFirst(setting.getName());
            if (args.length == 2){
                eb.setTitle(language.getString("settings.title", name));
                addField(eb, setting, null, dataHandler, e.getGuild(), prefix, language);
                for (Setting.SubSetting subs : setting.getSubSettings()){
                    addField(eb, setting, subs, dataHandler, e.getGuild(), prefix, language);
                }
                e.getChannel().sendMessageEmbeds(eb.build()).queue();
            } else {
                Setting.SubSetting subs = Setting.SubSetting.fromString(args[2]);
                String emoji = Utils.config.getProperty("emoji.green_tick");
                if (subs != null){
                    if (!setting.getSubSettings().contains(subs))
                        throw new EmbedException(language.getString("settings.error.subsetting", subs.toString().toLowerCase(), name));

                    if (args.length >= 4){
                        if (subs.isMultiple()){
                            if (args.length == 4 && args[3].equalsIgnoreCase("clear")){
                                handleClear(setting, subs, guildId, dataHandler);
                                e.getMessage().addReaction(emoji).queue();
                            } else if (args.length >= 5 && args[3].matches("(?i)^(add|remove)$")){
                                handleMultiSet(setting, subs, guildId, Arrays.copyOfRange(args, 4, args.length), args[3].equalsIgnoreCase("add"), dataHandler, e.getMessage(), language);
                            } else if (args.length == 4 && args[3].matches("(?i)^(enable|disable)$")){
                                dataHandler.setListEnabled(guildId, setting, subs, args[3].equalsIgnoreCase("enable"));
                                e.getMessage().addReaction(emoji).queue();
                            }
                        } else {
                            handleSet(setting, subs, guildId, args[3], dataHandler, e.getMessage(), language);
                        }
                    } else {
                        eb.setTitle(language.getString("settings.subtitle", name, subs.toString().toLowerCase()));
                        addField(eb, setting, subs, dataHandler, e.getGuild(), prefix, language);
                        e.getChannel().sendMessageEmbeds(eb.build()).queue();
                    }
                } else {
                    if (setting.isMultiple()){
                        if (args.length == 3 && args[2].equalsIgnoreCase("clear")){
                            e.getMessage().addReaction(emoji).queue();
                            handleClear(setting, null, guildId, dataHandler);
                        } else if (args.length >= 4 && args[2].matches("(?i)^(add|remove)$")){
                            handleMultiSet(setting, null, guildId, Arrays.copyOfRange(args, 3, args.length), args[2].equalsIgnoreCase("add"), dataHandler, e.getMessage(), language);
                        } else if (args.length == 3 && args[2].matches("(?i)^(enable|disable)$")){
                            dataHandler.setListEnabled(guildId, setting, null, args[2].equalsIgnoreCase("enable"));
                        }
                    } else {
                        handleSet(setting, null, guildId, args[2], dataHandler, e.getMessage(), language);
                        e.getMessage().addReaction(emoji).queue();
                    }
                }
            }
        }
    }

    private void addField(EmbedBuilder eb, Setting setting, Setting.SubSetting subs, SettingsDataHandler dataHandler, Guild guild, String prefix, MyResourceBundle language){
        long guildId = guild.getIdLong();
        boolean close = true;
        StringBuilder fieldName = new StringBuilder();
        StringBuilder fieldValue = new StringBuilder();
        fieldValue.append("`").append(prefix).append("settings ")
                .append(setting.getType().toLowerCase()).append(" ").append(setting.getName().toLowerCase());

        boolean multiple = (subs != null && subs.isMultiple()) || (subs == null && setting.isMultiple());
        if (multiple){
            fieldValue.append(" [add|remove|clear|enable|disable]");
            fieldName.append(String.format("<%s> ", Utils.config.getProperty(dataHandler.getListEnabled(guildId, setting, subs) ? "emoji.green_tick" : "emoji.red_tick")));
        }

        Setting.ValueType v = setting.getValueType();
        if (subs != null){
            fieldName.append(Utils.upperCaseFirst(subs.toString().toLowerCase())).append(": ");
            fieldValue.append(" ").append(subs.toString().toLowerCase());
            v = subs.getValueType();
        } else {
            fieldName.append(Utils.upperCaseFirst(setting.getName())).append(": ");
        }

        switch (v){
            case BOOLEAN:
                fieldValue.append(" [on|off]");
                fieldName.insert(0, String.format("<%s>", Utils.config.getProperty(dataHandler.getBoolSetting(guildId, setting, subs) ? "emoji.green_tick" : "emoji.red_tick")));
                break;
            case LONG:
                fieldValue.append(" <channel|role|member>");
                List<Pair<Long, Setting.LongType>> longSettings = dataHandler.getLongSetting(guildId, setting, subs);
                if (longSettings != null){
                    StringBuilder sb = new StringBuilder();
                    for (Pair<Long, Setting.LongType> longSetting : longSettings){
                        switch (longSetting.getRight()){
                            case CHANNEL -> sb.append(guild.getTextChannelById(longSetting.getLeft()).getAsMention());
                            case ROLE -> sb.append(guild.getRoleById(longSetting.getLeft()).getAsMention());
                            case USER -> sb.append(guild.getMemberById(longSetting.getLeft()).getAsMention());
                        }
                        sb.append("\n");
                    }
                    fieldValue.insert(0, sb);
                } else {
                    if (!multiple){
                        fieldName.append(language.getString("settings.not_set"));
                    } else {
                        fieldValue.insert(0, language.getString("settings.none_set") + "\n");
                    }
                }
                break;
            case CHANNEL_LONG:
                fieldValue.append(" <channel>");
                longSettings = dataHandler.getLongSetting(guildId, setting, subs);
                if (longSettings != null){
                    StringBuilder sb = new StringBuilder();
                    for (Pair<Long, Setting.LongType> longSetting : longSettings){
                        sb.append(guild.getTextChannelById(longSetting.getLeft()).getAsMention());
                        sb.append("\n");
                    }
                    fieldValue.insert(0, sb);
                } else {
                    if (!multiple){
                        fieldName.append(language.getString("settings.not_set"));
                    } else {
                        fieldValue.insert(0, language.getString("settings.none_set") + "\n");
                    }
                }
                break;
            case ROLE_LONG:
                fieldValue.append(" <role>");
                longSettings = dataHandler.getLongSetting(guildId, setting, subs);
                if (longSettings != null){
                    StringBuilder sb = new StringBuilder();
                    for (Pair<Long, Setting.LongType> longSetting : longSettings){
                        sb.append(guild.getRoleById(longSetting.getLeft()).getAsMention());
                        sb.append("\n");
                    }
                    fieldValue.insert(0, sb);
                } else {
                    if (!multiple){
                        fieldName.append(language.getString("settings.not_set"));
                    } else {
                        fieldValue.insert(0, language.getString("settings.none_set") + "\n");
                    }
                }
                break;
            case INTEGER:
                fieldValue.append(" <number>");
                List<Integer> settings = dataHandler.getIntSetting(guildId, setting, subs);
                if (settings.size() == 1){
                    fieldName.append(settings.get(0));
                } else {
                    fieldName.append(settings);
                }
                break;
            case LANGUAGE:
                fieldValue.append(" <language_code>`\n").append(language.getString("general.language.available"));
                for (Locale locale : Utils.getAvailableLanguages().keySet()){
                    fieldValue.append("\n").append(locale.getDisplayName()).append(" (").append(locale).append(")");
                }
                close = false;
                break;
            case STRING:
                fieldValue.append(" <value>");
                List<String> sSettings = dataHandler.getStringSetting(guildId, setting, subs);
                if (sSettings.size() == 1){
                    fieldName.append(sSettings.get(0));
                } else {
                    fieldName.append(sSettings);
                }
                break;
        }

        fieldName.append(subs != null ? subs.getSuffix() : setting.getSuffix());
        if (close)
            fieldValue.append("`");
        eb.addField(fieldName.toString(), fieldValue.toString(), false);
    }

    public void handleSet(Setting setting, Setting.SubSetting subSetting, long guildId, String value, SettingsDataHandler dataHandler, Message message, MyResourceBundle language) throws Exception{
        String green = Utils.config.getProperty("emoji.green_tick");
        switch (subSetting == null ? setting.getValueType() : subSetting.getValueType()){
            case BOOLEAN -> {
                if (!value.matches("(?i)^(on|enable|off|disable)$"))
                    throw new EmbedException(language.getString("settings.error.input.title"), language.getString("settings.error.input.boolean"));
                dataHandler.setBoolSetting(guildId, setting, subSetting, value.matches("(?i)^(on|enable)$"), null);
                message.addReaction(green).queue();
            }
            case STRING -> {
                dataHandler.setStringSetting(guildId, setting, subSetting, value, null);
                message.addReaction(green).queue();
            }
            case LONG -> {
                List<IMentionable> mentions = message.getMentions();
                if (mentions.size() == 1 && value.replaceFirst("!", "").equalsIgnoreCase(mentions.get(0).getAsMention())){
                    IMentionable mention = mentions.get(0);
                    Setting.LongType type = getLongType(mention);
                    if (type == null)
                        throw new EmbedException(language.getString("settings.error.input.title"), language.getString("settings.error.input.mention"));

                    dataHandler.setLongSetting(guildId, setting, subSetting, mention.getIdLong(), type, null);
                    message.addReaction(green).queue();
                } else if (!parseLong(setting, subSetting, guildId, null, message, value, dataHandler)){
                    throw new EmbedException(language.getString("settings.error.input.title"), language.getString("settings.error.input.long"));
                }
            }
            case INTEGER -> {
                if (!Utils.isInteger(value))
                    throw new EmbedException(language.getString("settings.error.input.title"), language.getString("settings.error.input.integer"));
                dataHandler.setIntSetting(guildId, setting, subSetting, Utils.getInt(value), null);
            }
            case CHANNEL_LONG -> {
                List<TextChannel> mentionedChannels = message.getMentionedChannels();
                if (mentionedChannels.size() == 1 && value.replaceFirst("!", "").equalsIgnoreCase(mentionedChannels.get(0).getAsMention())){
                    dataHandler.setLongSetting(guildId, setting, subSetting, mentionedChannels.get(0).getIdLong(), Setting.LongType.CHANNEL, null);
                    message.addReaction(green).queue();
                } else {
                    Long id = Utils.isLong(value);
                    if (id == null || message.getGuild().getTextChannelById(id) == null)
                        throw new EmbedException(language.getString("settings.error.input.title"), language.getString("settings.error.input.channel"));
                    dataHandler.setLongSetting(guildId, setting, subSetting, id, Setting.LongType.CHANNEL, null);
                }
            }
            case ROLE_LONG -> {
                List<Role> mentionedRoles = message.getMentionedRoles();
                if (mentionedRoles.size() == 1 && value.replaceFirst("!", "").equalsIgnoreCase(mentionedRoles.get(0).getAsMention())){
                    dataHandler.setLongSetting(guildId, setting, subSetting, mentionedRoles.get(0).getIdLong(), Setting.LongType.ROLE, null);
                    message.addReaction(green).queue();
                } else {
                    Long id = Utils.isLong(value);
                    if (id == null || message.getGuild().getRoleById(id) == null)
                        throw new EmbedException(language.getString("settings.error.input.title"), language.getString("settings.error.input.role"));
                    dataHandler.setLongSetting(guildId, setting, subSetting, id, Setting.LongType.ROLE, null);
                }
            }
            case LANGUAGE -> {
                Map<Locale, ResourceBundle> languages = Utils.getAvailableLanguages();
                if (!languages.containsKey(LocaleUtils.toLocale(value))){
                    StringBuilder sb = new StringBuilder(language.getString("settings.error.input.language"));
                    for (Locale locale : languages.keySet()){
                        sb.append("\n").append(locale.getDisplayName()).append(" (").append(locale).append(")");
                    }
                    throw new EmbedException(language.getString("settings.error.input.title"), sb.toString());
                }
                dataHandler.setStringSetting(guildId, setting, value, null);
            }
        }
    }


    private void handleMultiSet(Setting setting, Setting.SubSetting subSetting, long guildId, String[] values, boolean add, SettingsDataHandler dataHandler, Message message, MyResourceBundle language) throws Exception{
        boolean good = true;
        Boolean clear = add ? null : false;
        StringBuilder wrongValues = new StringBuilder();
        String wrongType = "";
        switch (subSetting == null ? setting.getValueType() : subSetting.getValueType()){
            case INTEGER:
                for (String value : values){
                    if (Utils.isInteger(value)){
                        dataHandler.setIntSetting(guildId, setting, subSetting, Utils.getInt(value), clear);
                    } else {
                        good = false;
                        wrongValues.append(value).append(", ");
                        wrongType = "settings.error.input.list.integer";
                    }
                }
                break;
            case STRING:
                for (String value : values){
                    dataHandler.setStringSetting(guildId, setting, subSetting, value, clear);
                }
                break;
            case LONG:
                List<IMentionable> mentions = message.getMentions();
                int i = 0;
                for (String value : values){
                    // Apparently discord's mention is different as a mention than it is a raw text in the message
                    if (i < mentions.size() && value.replaceFirst("!", "").equalsIgnoreCase(mentions.get(i).getAsMention())){
                        IMentionable mention = mentions.get(i);
                        Setting.LongType type = getLongType(mention);
                        if (type == null){
                            good = false;
                            wrongValues.append(mention.getAsMention()).append(", ");
                        } else {
                            dataHandler.setLongSetting(guildId, setting, subSetting, mention.getIdLong(), type, clear);
                        }
                        i++;
                    } else if (Utils.isLong(value) != null){
                        if (!parseLong(setting, subSetting, guildId, clear, message, value, dataHandler)){
                            good = false;
                            wrongValues.append(value).append(", ");
                        }
                    } else {
                        good = false;
                        wrongValues.append(value).append(", ");
                    }
                }
                if (!good){
                    wrongType = "settings.error.input.list.long";
                }
                break;
            case CHANNEL_LONG:
                List<TextChannel> mentionedChannels = message.getMentionedChannels();
                i = 0;
                for (String value : values){
                    if (i < mentionedChannels.size() && value.replaceFirst("!", "").equalsIgnoreCase(mentionedChannels.get(i).getAsMention())){
                        dataHandler.setLongSetting(guildId, setting, subSetting, mentionedChannels.get(0).getIdLong(), Setting.LongType.CHANNEL, null);
                    } else {
                        Long id = Utils.isLong(value);
                        if (id != null && message.getGuild().getTextChannelById(id) != null){
                            dataHandler.setLongSetting(guildId, setting, subSetting, id, Setting.LongType.CHANNEL, null);
                        } else {
                            good = false;
                            wrongValues.append(value).append(", ");
                        }
                    }
                }
                if (!good){
                    wrongType = "settings.error.input.list.channel";
                }
                break;
            case ROLE_LONG:
                List<Role> mentionedRoles = message.getMentionedRoles();
                i = 0;
                for (String value : values){
                    if (i < mentionedRoles.size() && value.replaceFirst("!", "").equalsIgnoreCase(mentionedRoles.get(i).getAsMention())){
                        dataHandler.setLongSetting(guildId, setting, subSetting, mentionedRoles.get(0).getIdLong(), Setting.LongType.ROLE, null);
                    } else {
                        Long id = Utils.isLong(value);
                        if (id != null && message.getGuild().getRoleById(id) != null){
                            dataHandler.setLongSetting(guildId, setting, subSetting, id, Setting.LongType.ROLE, null);
                        } else {
                            good = false;
                            wrongValues.append(value).append(", ");
                        }
                    }
                }
                if (!good){
                    wrongType = "settings.error.input.list.role";
                }
                break;
        }
        if (!good)
            throw new EmbedException(language.getString("settings.error.input.title", language.getString("settings.error.input.list", add ? "add" : "remove", wrongValues.substring(0, wrongValues.length() - 2), language.getString(wrongType))));

        message.addReaction(Utils.config.getProperty("emoji.green_tick")).queue();
    }

    private void handleClear(Setting setting, Setting.SubSetting subSetting, long guildId, SettingsDataHandler dataHandler) throws Exception{
        switch (subSetting == null ? setting.getValueType() : subSetting.getValueType()){
            case INTEGER -> dataHandler.setIntSetting(guildId, setting, subSetting, 0, true);
            case STRING -> dataHandler.setStringSetting(guildId, setting, subSetting, null, true);
            case LONG, CHANNEL_LONG, ROLE_LONG -> dataHandler.setLongSetting(guildId, setting, subSetting, 0, null, true);
            case BOOLEAN -> dataHandler.setBoolSetting(guildId, setting, subSetting, false, true);
        }
    }

    private Setting.LongType getLongType(IMentionable mention){
        Setting.LongType type = null;
        if (mention instanceof TextChannel){
            type = Setting.LongType.CHANNEL;
        } else if (mention instanceof Role){
            type = Setting.LongType.ROLE;
        } else if (mention instanceof Member){
            type = Setting.LongType.USER;
        } else if (mention instanceof User){
            type = Setting.LongType.USER;
        }
        return type;
    }

    private boolean parseLong(Setting setting, Setting.SubSetting subSetting, Long guildId, Boolean clear, Message message, String value, SettingsDataHandler dataHandler) throws Exception{
        Long id = Utils.isLong(value);
        TextChannel channel = message.getGuild().getTextChannelById(id);
        Role role = message.getGuild().getRoleById(id);
        Member member = message.getGuild().getMemberById(id);
        if (channel != null){
            dataHandler.setLongSetting(guildId, setting, subSetting, id, Setting.LongType.CHANNEL, clear);
        } else if (role != null){
            dataHandler.setLongSetting(guildId, setting, subSetting, id, Setting.LongType.ROLE, clear);
        } else if (member != null){
            dataHandler.setLongSetting(guildId, setting, subSetting, id, Setting.LongType.USER, clear);
        } else {
            return false;
        }
        return true;
    }
}