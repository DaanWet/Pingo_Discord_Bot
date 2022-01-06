package commands.settings;

public enum CommandState {
    DISABLED(4, "❌ That command is currently disabled"),
    CHANNEL(3, "❌ You can't use that command in this channel"),
    USER(2, "❌ You don't have permission to run this command"),
    COOLDOWN(1, "❌ That command is still on cooldown"),
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

    public String getError(){
        return error;
    }
}
