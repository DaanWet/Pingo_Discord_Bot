package me.damascus2000.pingo.commands.settings;

import me.damascus2000.pingo.utils.MyResourceBundle;

public enum CommandState {
    DISABLED(4, "command.disabled"),
    CHANNEL(3, "command.channel"),
    USER(2, "command.user"),
    COOLDOWN(1, "command.cooldown"),
    ENABLED(0, "");

    private final int priority;
    private final String error;

    CommandState(int priority, String error){
        this.priority = priority;
        this.error = error;
    }

    public CommandState worst(CommandState other){
        if (this.priority > other.priority){
            return this;
        }
        return other;
    }

    public String getError(MyResourceBundle language){
        return language.getString(error);
    }
}
