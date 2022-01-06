package commands.settings;

import java.util.*;

public enum Setting {
    BETTING("betting", Type.COMMANDS, ValueType.BOOLEAN, false, true, List.of(SubSetting.BLACKLIST, SubSetting.WHITELIST), "", ""),
    ROLEASSIGN("roleAssign", Type.COMMANDS, ValueType.BOOLEAN, false, true, List.of(SubSetting.BLACKLIST, SubSetting.WHITELIST), "", ""),
    BLACKJACK("blackjack", Type.COMMANDS, ValueType.BOOLEAN, false, true, List.of(SubSetting.COOLDOWN, SubSetting.BLACKLIST, SubSetting.WHITELIST), "", ""),
    UNO("uno", Type.COMMANDS, ValueType.BOOLEAN, false, true, List.of(SubSetting.COOLDOWN, SubSetting.BLACKLIST, SubSetting.WHITELIST), "", ""),
    AMONGUS("amongUs", Type.COMMANDS, ValueType.BOOLEAN, false, false, List.of(SubSetting.BLACKLIST, SubSetting.WHITELIST), "", ""),
    CLEAN("clean", Type.COMMANDS, ValueType.BOOLEAN, false, false, List.of(SubSetting.COOLDOWN, SubSetting.BLACKLIST, SubSetting.WHITELIST), "", ""),
    FUCKPINGO("fuckPingo", Type.COMMANDS, ValueType.BOOLEAN, false, false, List.of(SubSetting.COOLDOWN, SubSetting.BLACKLIST, SubSetting.WHITELIST), "", ""),
    NICKNAME("nickname", Type.COMMANDS, ValueType.BOOLEAN, false, false, List.of(SubSetting.COOLDOWN, SubSetting.BLACKLIST, SubSetting.WHITELIST), "", ""),
    POLL("poll", Type.COMMANDS, ValueType.BOOLEAN, false, true, List.of(SubSetting.COOLDOWN, SubSetting.BLACKLIST, SubSetting.WHITELIST), "", ""),
    CUSTOMBET("custombet", Type.COMMANDS, ValueType.BOOLEAN, false, true, List.of(SubSetting.COOLDOWN, SubSetting.BLACKLIST, SubSetting.WHITELIST), "", ""),


    PREFIX("prefix", Type.GENERAL, ValueType.STRING, false, "!", List.of(), "", ""),

    UNO_PLACE("unoPlace", Type.UNO, ValueType.INTEGER, false, -1, List.of(), "", "");

    public enum Type {
        COMMANDS("Commands", new String[]{"Command"}, (list, eb, prefix) -> {
            eb.addField("Enable/Disable Command", String.format("Turn a specific command (or module) on or off.\n `%ssettings commands <command> [on|off]`", prefix), false);
            eb.addField("Set cooldown", String.format("Set a cooldown for a specific command.\n`%ssettings commands <command> cooldown <value>`", prefix), false);
            eb.addField("Whitelist or Blacklist", String.format("Enable whitelist or blacklist mode and whitelist or blacklist certain roles, users or channels from using a specific command (or module).\n" +
                                                                        "`%ssettings commands <command> [whitelist|blacklist] [add|remove|enable|disable] <user|role|channel>`", prefix), false
            );
            StringBuilder sb = new StringBuilder();
            list.forEach(s -> sb.append("`").append(s.name).append(s.getDescription().equals("") ? "`" : "` - ").append(s.getDescription()).append("\n"));
            return eb.addField("Available commands or modules are", sb.toString(), false);
        }),
        GENERAL("General", (list, eb, prefix) -> {
            return eb.addField("Prefix", String.format("Change the prefix with `%ssettings general prefix <newprefix>`", prefix), false);
        }),
        UNO("Uno", (list, eb, prefix) -> {
            return eb.addField("Uno", "Change uno idk ", false);
        });

        private final String name;
        private final TypeDescription typeDescription;
        private final String[] aliases;

        Type(String name, String[] aliases, TypeDescription typeDescription){
            this.name = name;
            this.typeDescription = typeDescription;
            this.aliases = aliases;
        }

        Type(String name, TypeDescription typeDescription){
            this.name = name;
            this.typeDescription = typeDescription;
            this.aliases = new String[]{};
        }

        public static Type fromString(String name){
            Optional<Type> optional = Arrays.stream(Type.values()).filter(s -> s.name.equalsIgnoreCase(name) || Arrays.stream(s.aliases).anyMatch(al -> al.equalsIgnoreCase(name))).findFirst();
            return optional.orElse(null);
        }

        public String getName(){
            return name;
        }

        public TypeDescription getTypeDescription(){
            return typeDescription;
        }
    }

    public enum ValueType {
        STRING("String"),
        BOOLEAN("Boolean"),
        INTEGER("Integer"),
        LONG("Long"),
        CHANNEL_LONG("Channel_Long"),
        ROLE_LONG("Role_Long");
        private final String name;

        ValueType(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }

    public enum LongType {
        CHANNEL,
        ROLE,
        USER
    }

    public enum SubSetting {
        COOLDOWN(ValueType.INTEGER, false, 0, "", " seconds"),
        BLACKLIST(ValueType.LONG, true, List.of(), "", ""),
        WHITELIST(ValueType.LONG, true, List.of(), "", "");

        private final boolean multiple;
        private final ValueType valueType;
        private final Object defaultValue;
        private final String description;
        private final String suffix;

        SubSetting(Setting.ValueType valueType, boolean multiple, Object defaultValue, String description, String suffix){
            this.multiple = multiple;
            this.valueType = valueType;
            this.defaultValue = defaultValue;
            this.description = description;
            this.suffix = suffix;
        }

        public static SubSetting fromString(String name){
            Optional<SubSetting> optional = Arrays.stream(SubSetting.values()).filter(s -> s.toString().toLowerCase().equalsIgnoreCase(name)).findFirst();
            return optional.orElse(null);
        }

        public boolean isMultiple(){
            return multiple;
        }

        public ValueType getValueType(){
            return valueType;
        }

        public Object getDefaultValue(){
            return defaultValue;
        }

        public String getDescription(){
            return description;
        }

        public String getSuffix(){
            return suffix;
        }
    }

    private final String name;
    private final Setting.Type type;
    private final Setting.ValueType valueType;
    private final boolean multiple;
    private final Object defaultValue;
    private final List<SubSetting> subSettings;
    private final String description;
    private final String suffix;

    Setting(String name, Setting.Type type, Setting.ValueType valueType, boolean multiple, Object defaultValue, List<SubSetting> subSettings,
            String description, String suffix){
        this.name = name;
        this.type = type;
        this.valueType = valueType;
        this.multiple = multiple;
        this.defaultValue = defaultValue;
        this.subSettings = subSettings;
        this.description = description;
        this.suffix = suffix;
    }

    public static HashMap<Setting.Type, ArrayList<Setting>> getTypeMap(){
        HashMap<Setting.Type, ArrayList<Setting>> map = new HashMap<>();
        Arrays.stream(Setting.values()).forEach(s -> map.merge(s.type, new ArrayList<>(List.of(s)), (currentL, newL) -> {
            currentL.addAll(newL);
            return currentL;
        }));
        return map;
    }

    public static Setting fromString(String name, Type type){
        Optional<Setting> optional = Arrays.stream(Setting.values()).filter(s -> s.name.equalsIgnoreCase(name) && s.type == type).findFirst();
        return optional.orElse(null);

    }

    public String getName(){
        return name;
    }

    public String getType(){
        return type.getName();
    }

    public ValueType getValueType(){
        return valueType;
    }

    public boolean isMultiple(){
        return multiple;
    }

    public Object getDefaultValue(){
        return defaultValue;
    }

    public List<SubSetting> getSubSettings(){
        return subSettings;
    }

    public String getDescription(){
        return description;
    }

    public String getSuffix(){
        return suffix;
    }

}
