package commands;

import commands.settings.CommandState;
import commands.settings.Setting;
import companions.Achievement;
import companions.GameCompanion;
import data.handlers.GeneralDataHandler;
import data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.apache.commons.lang3.StringUtils;
import utils.MyResourceBundle;
import utils.Utils;

import java.time.LocalDateTime;
import java.util.List;

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
    protected String[] arguments = new String[]{""};
    protected String description;
    protected String example = "";
    protected boolean hidden = false;
    protected long priveligedGuild = -1;

    public abstract void run(String[] args, GuildMessageReceivedEvent e) throws Exception;

    protected CommandState canBeExecuted(long guildId, long channelId, Member member, Setting setting){
        SettingsDataHandler dataHandler = new SettingsDataHandler();
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

    public String getDescription(MyResourceBundle language){
        return language.getString(description);
    }


    public String getName(){
        return name;
    }

    public String[] getArguments(){
        return arguments;
    }

    public String getExample(){
        return example;
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


    public String getUsage(long guildId){
        MyResourceBundle language = Utils.getLanguage(guildId);
        String prefix = new SettingsDataHandler().getStringSetting(guildId, Setting.PREFIX).get(0);
        StringBuilder sb = new StringBuilder();
        for (String arg : arguments){
            sb.append(String.format("\n%s%s %s", prefix, name, arg));
        }
        return language.getString("command.usage", sb.toString(), prefix, name);
    }

    public EmbedBuilder getHelp(EmbedBuilder eb, MyResourceBundle language, String prefix){
        eb.setTitle(language.getString("help.command", StringUtils.capitalize(name)));
        StringBuilder sb = new StringBuilder();
        for (String arg : arguments){
            sb.append(String.format("%s%s %s\n", prefix, name, arg));
        }
        eb.addField(language.getString("help.usage"), sb.toString(), false);
        eb.setDescription(getDescription(language));
        if (aliases.length != 0){
            eb.addField(language.getString("help.aliases"), String.join(", ", aliases), false);
        }
        eb.addField(language.getString("help.example"), String.format("%s%s %s", prefix, name, example), false);
        eb.setFooter(language.getString("help.embed.cmd.footer"));
        return eb;
    }

    public EmbedBuilder getHelp(long guildId){
        String prefix = new SettingsDataHandler().getStringSetting(guildId, Setting.PREFIX).get(0);
        MyResourceBundle language = Utils.getLanguage(guildId);
        EmbedBuilder eb = new EmbedBuilder();
        return getHelp(eb, language, prefix);
    }


    public boolean isCommandFor(String s){
        if (s.equalsIgnoreCase(name)){
            return true;
        }
        int ctr = 0;
        while (ctr < aliases.length && !s.equalsIgnoreCase(aliases[ctr])){
            ctr++;
        }

        return ctr < aliases.length;
    }

    protected MyResourceBundle getLanguage(GuildMessageReceivedEvent e){
        return Utils.getLanguage(e.getGuild().getIdLong());
    }

    protected void checkAchievements(TextChannel textChannel, long userId, GameCompanion gameCompanion){
        for (Achievement achievement : Achievement.values()){
            if (achievement.isAchieved(textChannel.getGuild().getIdLong(), userId, gameCompanion)){
                // notify
            }
        }
    }

    protected void checkLevel(TextChannel textChannel, Member member, int startXP, int endXP){
        int startLevel = Utils.getLevel(startXP);
        int endLevel = Utils.getLevel(endXP);
        while(startLevel < endLevel){
            startLevel++;
            textChannel.sendMessage(String.format("Good job %s! You andvanced to level %d", member.getAsMention(), startLevel)).queue();
        }
    }

}
