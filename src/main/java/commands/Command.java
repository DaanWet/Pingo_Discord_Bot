package commands;

import commands.settings.CommandState;
import commands.settings.Setting;
import data.DataHandler;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import utils.MyResourceBundle;
import utils.Utils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public abstract class Command {

    public enum Category {
        CASINO("Casino"),
        MODERATION("Moderation"),
        PICTURES("Pictures"),
        UNO("Uno"),
        OTHER("Other");


        private final String display;

        Category(String display){
            this.display = display;
        }

        public String getDisplay(){
            return display;
        }

    }


    protected String name;
    protected String[] aliases = new String[0];
    protected Category category = Category.OTHER;
    protected String arguments = "";
    protected String description;
    protected boolean hidden = false;
    protected long priveligedGuild = -1;

    public abstract void run(String[] args, GuildMessageReceivedEvent e) throws Exception;

    protected CommandState canBeExecuted(long guildId, long channelId, Member member, Setting setting){
        DataHandler dataHandler = new DataHandler();
        CommandState state = CommandState.DISABLED;
        boolean enabled = dataHandler.getBoolSetting(guildId, setting);
        long userId = member.getIdLong();
        if (enabled){
            if (dataHandler.getListEnabled(guildId, setting, Setting.SubSetting.WHITELIST)){
                List<Pair<Long, Setting.LongType>> whitelist = dataHandler.getLongSetting(guildId, setting, Setting.SubSetting.WHITELIST);
                int i = 0;
                state = CommandState.CHANNEL;
                while (state != CommandState.ENABLED && i < whitelist.size()){
                    Pair<Long, Setting.LongType> pair = whitelist.get(i);
                    if (pair.getLeft().equals(channelId) || pair.getLeft().equals(userId) || member.getRoles().stream().map(ISnowflake::getIdLong).anyMatch(id -> id.equals(pair.getLeft()))){
                        state = CommandState.ENABLED;
                    }
                    i++;
                }
            } else {
                state = CommandState.ENABLED;
            }
            if (dataHandler.getListEnabled(guildId, setting, Setting.SubSetting.BLACKLIST)){
                List<Pair<Long, Setting.LongType>> blacklist = dataHandler.getLongSetting(guildId, setting, Setting.SubSetting.BLACKLIST);
                int i = 0;
                while (state != CommandState.CHANNEL && i < blacklist.size()){
                    Pair<Long, Setting.LongType> pair = blacklist.get(i);
                    if (pair.getLeft().equals(channelId)){
                        state = CommandState.CHANNEL;
                    } else if (pair.getLeft().equals(userId) || member.getRoles().stream().map(ISnowflake::getIdLong).anyMatch(id -> id.equals(pair.getLeft()))){
                        state = CommandState.USER;
                    }
                    i++;
                }
            }
            if (state == CommandState.ENABLED && setting.getSubSettings().contains(Setting.SubSetting.COOLDOWN)){
                int cooldown = dataHandler.getIntSetting(guildId, setting, Setting.SubSetting.COOLDOWN).get(0);
                if (cooldown != 0){
                    LocalDateTime t = dataHandler.getCooldown(guildId, userId, setting);
                    if (t != null && LocalDateTime.now().isBefore(t.plusSeconds(cooldown))){
                        state = CommandState.COOLDOWN;
                    }
                }
            }
        }
        return state;
    }


    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        Setting s = Setting.fromString(name, Setting.Type.COMMANDS);
        CommandState state = CommandState.ENABLED;
        if (s != null){
            state = canBeExecuted(guildId, channelId, member, s);
        }
        return state;
    }

    public String getDescription(){
        return description;
    }

    public String getDescription(ResourceBundle language){
        return language.getString(description);
    }


    public String getName(){
        //if (name == null) throw new ExecutionControl.NotImplementedException("Command should have a name");
        return name;
    }

    public String getArguments(){
        return arguments;
    }

    public String[] getAliases(){
        return aliases;
    }

    public Category getCategory(){
        return category;
    }

    public boolean isHidden(){
        return hidden;
    }


    public long getPriveligedGuild(){
        return priveligedGuild;
    }

    public String getUsage(){
        return String.format("Usage: !%s %s\n%s", name, arguments, description == null ? "" : description);
    }

    public boolean isCommandFor(String s){
        if (s.equalsIgnoreCase(name)){
            return true;
        }
        int ctr = 0;
        while (ctr < aliases.length && !s.equalsIgnoreCase(aliases[ctr])){
            //System.out.println(String.format("%s: %s", s, aliases[ctr]));
            ctr++;
        }

        return ctr < aliases.length;
    }

    protected MyResourceBundle getLanguage(GuildMessageReceivedEvent e){
        return Utils.getLanguage(e.getGuild().getIdLong());
    }
}
