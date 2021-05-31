package commands.settings;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;
import utils.Utils;

import java.awt.*;
import java.util.List;

public class Settings extends Command {

    public Settings() {
        this.name = "settings";
        this.category = "Moderation";
        this.description = "Show or edit the settings for pingo";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        DataHandler dataHandler = new DataHandler();
        long guildId = e.getGuild().getIdLong();
        String prefix = dataHandler.getStringSetting(guildId, Setting.PREFIX);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(e.getGuild().getSelfMember().getColor());
        if (args.length == 0) {
            eb.setTitle("Pingo Settings");
            for (Setting.Type type : Setting.Type.values()) {
                eb.addField(type.getName(), String.format("To see all %s settings, use `%ssettings %s`", type.getName(), prefix, type.getName().toLowerCase()), true);
            }
            e.getChannel().sendMessage(eb.build()).queue();
        } else {
            Setting.Type type = Setting.Type.fromString(args[0]);
            if (type != null) {
                if (args.length == 1) {
                    eb.setTitle(String.format("%s Settings", type.getName()));
                    List<Setting> settings = Setting.getTypeMap().get(type);
                    eb = type.getTypeDescription().getDescription(settings, eb, prefix);
                    e.getChannel().sendMessage(eb.build()).queue();
                } else {
                    Setting setting = Setting.fromString(args[1], type);
                    if (setting != null) {
                        String name = Utils.upperCaseFirst(setting.getName());
                        if (args.length == 2) {
                            eb.setTitle(String.format("%s Settings", name));
                            addField(eb, setting, null, dataHandler, guildId, prefix);
                            for (Setting.SubSetting subs : setting.getSubSettings()) {
                                addField(eb, setting, subs, dataHandler, guildId, prefix);
                            }
                            e.getChannel().sendMessage(eb.build()).queue();
                        } else {
                            Setting.SubSetting subs = Setting.SubSetting.fromString(args[2]);
                            if (subs != null) {
                                if (setting.getSubSettings().contains(subs)) {
                                    if (args.length == 4) {
                                        handleSet(eb, setting, subs, guildId, args[3], dataHandler, e.getMessage());
                                    } else {
                                        eb.setTitle(String.format("%s %s Settings", name, subs.toString().toLowerCase()));
                                        addField(eb, setting, subs, dataHandler, guildId, prefix);
                                        e.getChannel().sendMessage(eb.build()).queue();
                                    }
                                } else {
                                    eb.setTitle(String.format("%s is not a subsetting for %s", subs.toString().toLowerCase(), name));
                                    eb.setColor(Color.RED);
                                    e.getChannel().sendMessage(eb.build()).queue();
                                }
                            } else {
                                handleSet(eb, setting, null, guildId, args[2], dataHandler, e.getMessage());
                            }
                        }
                    } else {
                        eb.setTitle(String.format("%s is not a setting, use `%ssettings %s` to view all possible settings for the %s category", args[1], prefix, args[0], args[0]));
                        eb.setColor(Color.RED);
                        e.getChannel().sendMessage(eb.build()).queue();
                    }
                }
            } else {
                eb.setTitle(String.format("%s is not a settings category, use `%ssettings` to view all possible categories", args[0], prefix));
                eb.setColor(Color.RED);
                e.getChannel().sendMessage(eb.build()).queue();
            }
        }

    }

    public void addField(EmbedBuilder eb, Setting setting, Setting.SubSetting subs, DataHandler dataHandler, long guildId, String prefix) {
        StringBuilder fieldName = new StringBuilder();
        StringBuilder fieldValue = new StringBuilder();
        fieldName.append("The ").append(Utils.upperCaseFirst(setting.getName()));
        fieldValue.append("Modify this setting using \n`").append(prefix).append("settings ")
                .append(setting.getType()).append(" ").append(name);
        Setting.ValueType v = setting.getValueType();
        if (subs != null) {
            fieldName.append(" ").append(subs.toString().toLowerCase());
            fieldValue.append(" ").append(subs.toString().toLowerCase());
            v = subs.getValueType();
        }
        fieldName.append(" setting is currently ");
        if ((subs != null && subs.isMultiple()) || (subs == null && setting.isMultiple())) {
            fieldValue.append(" [add|remove|clear]");
        }
        switch (v) {
            case BOOLEAN:
                fieldValue.append(" [on|off]");
                fieldName.append("turned ").append(dataHandler.getBoolSetting(guildId, setting, subs) ? "on <:greentick:804432208483844146>" : "off <:redtick:804432244469923890>");
                break;
            case LONG:
                fieldValue.append(" <channel|role|member>");
                fieldName.append(dataHandler.getLongSetting(guildId, setting, subs));
                break;
            case CHANNEL_LONG:
                fieldValue.append(" <channel>");
                fieldName.append(dataHandler.getLongSetting(guildId, setting, subs));
                break;
            case ROLE_LONG:
                fieldValue.append(" <role>");
                fieldName.append(dataHandler.getLongSetting(guildId, setting, subs));
                break;
            case INTEGER:
                fieldValue.append(" <number>");
                fieldName.append(dataHandler.getIntSetting(guildId, setting, subs));
                break;
            case STRING:
                fieldValue.append(" <value>");
                fieldName.append(dataHandler.getStringSetting(guildId, setting, subs));
                break;
        }

        fieldName.append(subs != null ? subs.getSuffix() : setting.getSuffix());
        fieldValue.append("`");
        eb.addField(fieldName.toString(), fieldValue.toString(), false);
    }

    public void handleSet(EmbedBuilder eb, Setting setting, Setting.SubSetting subSetting, long guildId, String value, DataHandler dataHandler, Message message) {
        switch (subSetting == null ? setting.getValueType() : subSetting.getValueType()) {
            case BOOLEAN:
                if (value.matches("(?i)^(on|enable|off|disable)$")) {
                    dataHandler.setBoolSetting(guildId, setting, subSetting, value.matches("(?i)(on|enable)"));
                    message.addReaction(":greentick:804432208483844146").queue();
                } else {
                    eb.setTitle("Invalid input");
                    eb.setColor(Color.RED);
                    message.getChannel().sendMessage(eb.build()).queue();
                }
                break;
            case STRING:
                dataHandler.setStringSetting(guildId, setting, subSetting, value);
                message.addReaction(":greentick:804432208483844146").queue();
                break;
            case LONG:
                if (message.getMentions().size() == 1){
                    IMentionable mention = message.getMentions().get(0);
                    Setting.LongType type = null;
                    if (mention instanceof TextChannel){
                        type = Setting.LongType.CHANNEL;
                    } else if (mention instanceof Role){
                        type = Setting.LongType.ROLE;
                    } else if (mention instanceof Member){
                        type = Setting.LongType.USER;
                    }
                    if (type == null){
                        eb.setTitle("Invalid input");
                        eb.setColor(Color.RED);
                        eb.setDescription(mention.getClass().toString());
                    } else {
                        dataHandler.setLongSetting(guildId, setting, subSetting, mention.getIdLong(), type);
                        message.addReaction(":greentick:804432208483844146").queue();
                    }
                }
                break;
            case INTEGER:
                if (Utils.isInteger(value)) {
                    dataHandler.setIntSetting(guildId, setting, subSetting, Utils.getInt(value));
                } else {
                    eb.setTitle("Invalid input");
                    eb.setColor(Color.RED);
                    message.getChannel().sendMessage(eb.build()).queue();
                }
                break;
        }
    }
}

