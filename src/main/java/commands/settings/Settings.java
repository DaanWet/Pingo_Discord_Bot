package commands.settings;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.DataHandler;
import utils.Utils;

import java.awt.*;
import java.util.Arrays;
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
        String prefix = dataHandler.getStringSetting(guildId, Setting.PREFIX).get(0);
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
                            addField(eb, setting, null, dataHandler, e.getGuild(), prefix);
                            for (Setting.SubSetting subs : setting.getSubSettings()) {
                                addField(eb, setting, subs, dataHandler, e.getGuild(), prefix);
                            }
                            e.getChannel().sendMessage(eb.build()).queue();
                        } else {
                            Setting.SubSetting subs = Setting.SubSetting.fromString(args[2]);
                            if (subs != null) {
                                if (setting.getSubSettings().contains(subs)) {
                                    if (args.length >= 4) {
                                        if (subs.isMultiple()) {
                                            if (args.length == 4 && args[3].equalsIgnoreCase("clear")) {
                                                handleClear(setting, subs, guildId, dataHandler);
                                                e.getMessage().addReaction(":greentick:804432208483844146").queue();
                                            } else if (args.length >= 5 && args[3].matches("(?i)^(add|remove)$")) {
                                                handleMultiSet(setting, subs, guildId, Arrays.copyOfRange(args, 4, args.length), args[3].equalsIgnoreCase("add"), dataHandler, e.getMessage());
                                            } else if (args.length == 4 && args[3].matches("(?i)^(enable|disable)$")){
                                                dataHandler.setListEnabled(guildId, setting, subs, args[3].equalsIgnoreCase("enable"));
                                                e.getMessage().addReaction(":greentick:804432208483844146").queue();
                                            }
                                        } else {
                                            handleSet(setting, subs, guildId, args[3], dataHandler, e.getMessage());
                                        }
                                    } else {
                                        eb.setTitle(String.format("%s %s Settings", name, subs.toString().toLowerCase()));
                                        addField(eb, setting, subs, dataHandler, e.getGuild(), prefix);
                                        e.getChannel().sendMessage(eb.build()).queue();
                                    }
                                } else {
                                    eb.setTitle(String.format("%s is not a subsetting for %s", subs.toString().toLowerCase(), name));
                                    eb.setColor(Color.RED);
                                    e.getChannel().sendMessage(eb.build()).queue();
                                }
                            } else {
                                if (setting.isMultiple()) {
                                    if (args.length == 3 && args[2].equalsIgnoreCase("clear")) {
                                        e.getMessage().addReaction(":greentick:804432208483844146").queue();
                                        handleClear(setting, null, guildId, dataHandler);
                                    } else if (args.length >= 4 && args[2].matches("(?i)^(add|remove)$")) {
                                        handleMultiSet(setting, null, guildId, Arrays.copyOfRange(args, 3, args.length), args[2].equalsIgnoreCase("add"), dataHandler, e.getMessage());
                                    }  else if (args.length == 3 && args[2].matches("(?i)^(enable|disable)$")){
                                        dataHandler.setListEnabled(guildId, setting, subs, args[2].equalsIgnoreCase("enable"));
                                    }
                                } else {
                                    handleSet(setting, null, guildId, args[2], dataHandler, e.getMessage());
                                    e.getMessage().addReaction(":greentick:804432208483844146").queue();
                                }
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

   private void addField(EmbedBuilder eb, Setting setting, Setting.SubSetting subs, DataHandler dataHandler, Guild guild, String prefix){
        long guildId = guild.getIdLong();
        StringBuilder fieldName = new StringBuilder();
        StringBuilder fieldValue = new StringBuilder();
        fieldValue.append("`").append(prefix).append("settings ")
                .append(setting.getType().toLowerCase()).append(" ").append(setting.getName().toLowerCase());

        boolean multiple = (subs != null && subs.isMultiple()) || (subs == null && setting.isMultiple());
        if (multiple) {
            fieldValue.append(" [add|remove|clear|enable|disable]");
            fieldName.append(dataHandler.getListEnabled(guildId, setting, subs) ? "<:greentick:804432208483844146> " : "<:redtick:804432244469923890> ");
        }

        Setting.ValueType v = setting.getValueType();
        if (subs != null) {
            fieldName.append(Utils.upperCaseFirst(subs.toString().toLowerCase())).append(": ");
            fieldValue.append(" ").append(subs.toString().toLowerCase());
            v = subs.getValueType();
        } else {
            fieldName.append(Utils.upperCaseFirst(setting.getName())).append(": ");
        }

        switch (v) {
            case BOOLEAN:
                fieldValue.append(" [on|off]");
                fieldName.insert(0, dataHandler.getBoolSetting(guildId, setting, subs) ? "<:greentick:804432208483844146>" : "<:redtick:804432244469923890>");
                break;
            case LONG:
                fieldValue.append(" <channel|role|member>");
                List<Pair<Long, Setting.LongType>> longSettings = dataHandler.getLongSetting(guildId, setting, subs);
                if (longSettings != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Pair<Long, Setting.LongType> longSetting : longSettings) {
                        switch (longSetting.getRight()) {
                            case CHANNEL:
                                sb.append(guild.getTextChannelById(longSetting.getLeft()).getAsMention());
                                break;
                            case ROLE:
                                sb.append(guild.getRoleById(longSetting.getLeft()).getAsMention());
                                break;
                            case USER:
                                sb.append(guild.getMemberById(longSetting.getLeft()).getAsMention());
                                break;
                        }
                        sb.append("\n");
                    }
                    fieldValue.insert(0, sb);
                } else {
                    if (!multiple){
                        fieldName.append("not set");
                    } else {
                        fieldValue.insert(0, "None set\n");
                    }
                }
                break;
            case CHANNEL_LONG:
                fieldValue.append(" <channel>");
                longSettings = dataHandler.getLongSetting(guildId, setting, subs);
                if (longSettings != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Pair<Long, Setting.LongType> longSetting : longSettings) {
                        sb.append(guild.getTextChannelById(longSetting.getLeft()).getAsMention());
                        sb.append("\n");
                    }
                    fieldValue.insert(0, sb);
                } else {
                    if (!multiple){
                        fieldName.append("not set");
                    } else {
                        fieldValue.insert(0, "None set\n");
                    }
                }
                break;
            case ROLE_LONG:
                fieldValue.append(" <role>");
                longSettings = dataHandler.getLongSetting(guildId, setting, subs);
                if (longSettings != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Pair<Long, Setting.LongType> longSetting : longSettings) {
                        sb.append(guild.getRoleById(longSetting.getLeft()).getAsMention());
                        sb.append("\n");
                    }
                    fieldValue.insert(0, sb);
                } else {
                    if (!multiple){
                        fieldName.append("not set");
                    } else {
                        fieldValue.insert(0, "None set\n");
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
        fieldValue.append("`");
        eb.addField(fieldName.toString(), fieldValue.toString(), false);
    }

    public void handleSet(Setting setting, Setting.SubSetting subSetting, long guildId, String value, DataHandler dataHandler, Message message) throws Exception {
        switch (subSetting == null ? setting.getValueType() : subSetting.getValueType()) {
            case BOOLEAN:
                if (value.matches("(?i)^(on|enable|off|disable)$")) {
                    dataHandler.setBoolSetting(guildId, setting, subSetting, value.matches("(?i)^(on|enable)$"), null);
                    message.addReaction(":greentick:804432208483844146").queue();
                } else {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Invalid input");
                    eb.setColor(Color.RED);
                    message.getChannel().sendMessage(eb.build()).queue();
                }
                break;
            case STRING:
                dataHandler.setStringSetting(guildId, setting, subSetting, value, null);
                message.addReaction(":greentick:804432208483844146").queue();
                break;
            case LONG:
                if (message.getMentions().size() == 1) {
                    IMentionable mention = message.getMentions().get(0);
                    Setting.LongType type = getLongType(mention);
                    if (type == null) {
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle("Invalid input");
                        eb.setColor(Color.RED);
                        eb.setDescription(mention.getClass().toString());
                        message.getChannel().sendMessage(eb.build()).queue();
                    } else {
                        dataHandler.setLongSetting(guildId, setting, subSetting, mention.getIdLong(), type, null);
                        message.addReaction(":greentick:804432208483844146").queue();
                    }
                }
                break;
            case INTEGER:
                if (Utils.isInteger(value)) {
                    dataHandler.setIntSetting(guildId, setting, subSetting, Utils.getInt(value), null);
                } else {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Invalid input");
                    eb.setColor(Color.RED);
                    message.getChannel().sendMessage(eb.build()).queue();
                }
                break;
            // TODO: CHANNEL_LONG AND ROLE_LONG
        }
    }

    private void handleMultiSet(Setting setting, Setting.SubSetting subSetting, long guildId, String[] values, boolean add, DataHandler dataHandler, Message message) throws Exception {
        boolean good = true;
        Boolean clear = add ? null : false;
        StringBuilder wrongValues = new StringBuilder();
        String wrongType = "";
        switch (subSetting == null ? setting.getValueType() : subSetting.getValueType()) {
            case INTEGER:
                for (String value : values) {
                    if (Utils.isInteger(value)) {
                        dataHandler.setIntSetting(guildId, setting, subSetting, Utils.getInt(value), clear);
                    } else {
                        good = false;
                        wrongValues.append(value).append(", ");
                        wrongType = "a number";
                    }
                }
                break;
            case STRING:
                for (String value : values) {
                    dataHandler.setStringSetting(guildId, setting, subSetting, value, clear);
                }
                message.addReaction(":greentick:804432208483844146").queue();
                break;
            case LONG:
                List<IMentionable> mentions = message.getMentions();
                int i = 0;
                for (String value : values) {
                    // Apparently discord's mention is different as a mention than it is a raw text in the message
                    if (i < mentions.size() && value.replaceFirst("!", "").equalsIgnoreCase(mentions.get(i).getAsMention())) {
                        IMentionable mention = mentions.get(i);
                        Setting.LongType type = getLongType(mention);
                        if (type == null) {
                            good = false;
                            wrongValues.append(mention.getAsMention()).append(", ");
                        } else {
                            dataHandler.setLongSetting(guildId, setting, subSetting, mention.getIdLong(), type, clear);
                        }
                        i++;
                    } else if (Utils.isLong(value) != null) {
                        Long id = Utils.isLong(value);
                        TextChannel channel = message.getGuild().getTextChannelById(id);
                        Role role = message.getGuild().getRoleById(id);
                        Member member = message.getGuild().getMemberById(id);
                        if (channel != null) {
                            dataHandler.setLongSetting(guildId, setting, subSetting, id, Setting.LongType.CHANNEL, clear);
                        } else if (role != null) {
                            dataHandler.setLongSetting(guildId, setting, subSetting, id, Setting.LongType.ROLE, clear);
                        } else if (member != null) {
                            dataHandler.setLongSetting(guildId, setting, subSetting, id, Setting.LongType.USER, clear);
                        } else {
                            good = false;
                            wrongValues.append(value).append(", ");
                        }
                    } else {
                        good = false;
                        wrongValues.append(value).append(", ");
                    }
                }
                if (!good) {
                    wrongType = "a channel, role or member";
                }
                break;
            // TODO: CHANNEL_LONG AND ROLE_LONG
        }
        if (good) {
            message.addReaction(":greentick:804432208483844146").queue();
        } else {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Invalid input");
            eb.setDescription(String.format("Could not %s %s as it is not %s", add ? "add" : "remove", wrongValues.substring(0, wrongValues.length() - 2), wrongType));
            eb.setColor(Color.RED);
            message.getChannel().sendMessage(eb.build()).queue();
        }
    }

    private void handleClear(Setting setting, Setting.SubSetting subSetting, long guildId, DataHandler dataHandler) throws Exception {
        switch (subSetting == null ? setting.getValueType() : subSetting.getValueType()) {
            case INTEGER:
                dataHandler.setIntSetting(guildId, setting, subSetting, 0, true);
                break;
            case STRING:
                dataHandler.setStringSetting(guildId, setting, subSetting, null, true);
                break;
            case LONG:
            case CHANNEL_LONG:
            case ROLE_LONG:
                dataHandler.setLongSetting(guildId, setting, subSetting, 0, null, true);
                break;
            case BOOLEAN:
                dataHandler.setBoolSetting(guildId, setting, subSetting, false, true);
                break;
        }
    }

    private Setting.LongType getLongType(IMentionable mention) {
        Setting.LongType type = null;
        if (mention instanceof TextChannel) {
            type = Setting.LongType.CHANNEL;
        } else if (mention instanceof Role) {
            type = Setting.LongType.ROLE;
        } else if (mention instanceof Member) {
            type = Setting.LongType.USER;
        } else if (mention instanceof User) {
            type = Setting.LongType.USER;
        }
        return type;
    }
}