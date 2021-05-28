package commands.settings;

import java.util.*;

public enum Setting {
    BETTING("betting", Type.COMMANDS, ValueType.BOOLEAN, false),
    ROLEASSIGN("roleAssign", Type.COMMANDS, ValueType.BOOLEAN, false),
    BLACKJACK("blackjack", Type.COMMANDS, ValueType.BOOLEAN, false),
    UNO("uno", Type.COMMANDS, ValueType.BOOLEAN, false),
    AMONGUS("amongUs", Type.COMMANDS, ValueType.BOOLEAN, false),
    CLEAN("clean", Type.COMMANDS, ValueType.BOOLEAN, false),
    FUCKPINGO("fuckPingo", Type.COMMANDS, ValueType.BOOLEAN, false),
    NICKNAME("nickname", Type.COMMANDS, ValueType.BOOLEAN, false),
    POLL("poll", Type.COMMANDS, ValueType.BOOLEAN, false),

    PREFIX("prefix", Type.GENERAL, ValueType.STRING, false);


    private final String name;
    private final Setting.Type type;
    private final Setting.ValueType valueType;
    private final boolean multiple;

    Setting(String name, Setting.Type type, Setting.ValueType valueType, boolean multiple) {
        this.name = name;
        this.type = type;
        this.valueType = valueType;
        this.multiple = multiple;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type.getName();
    }

    public String getValueType() {
        return valueType.getName();
    }

    public boolean isMultiple() {
        return multiple;
    }

    public static HashMap<Setting.Type, List<Setting>> getTypeMap() {
        HashMap<Setting.Type, List<Setting>> map = new HashMap<>();
        Arrays.stream(Setting.values()).forEachOrdered(s -> map.getOrDefault(s.type, new ArrayList<>()).add(s));
        return map;
    }

    public static Setting fromString(String name, Type type) {
        Optional<Setting> optional = Arrays.stream(Setting.values()).filter(s -> s.name.equalsIgnoreCase(name) && s.type == type).findFirst();
        return optional.orElse(null);

    }

    public enum Type {
        COMMANDS("Commands"),
        GENERAL("General");
        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Type fromString(String name){
            Optional<Type> optional = Arrays.stream(Type.values()).filter(s -> s.name.equalsIgnoreCase(name)).findFirst();
            return optional.orElse(null);
        }
    }

    public enum ValueType {
        STRING("String"),
        BOOLEAN("Boolean"),
        INTEGER("Integer"),
        LONG("Long");
        private final String name;

        ValueType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
