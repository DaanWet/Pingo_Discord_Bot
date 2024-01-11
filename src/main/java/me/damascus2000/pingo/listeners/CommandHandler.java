package me.damascus2000.pingo.listeners;

import me.damascus2000.pingo.commands.*;
import me.damascus2000.pingo.commands.casino.*;
import me.damascus2000.pingo.commands.casino.bet.*;
import me.damascus2000.pingo.commands.casino.blackjack.*;
import me.damascus2000.pingo.commands.casino.uno.*;
import me.damascus2000.pingo.commands.pictures.AddPicture;
import me.damascus2000.pingo.commands.pictures.DeletePicture;
import me.damascus2000.pingo.commands.roles.AddRoleAssign;
import me.damascus2000.pingo.commands.roles.EditRoleAssign;
import me.damascus2000.pingo.commands.roles.RemoveRoleAssign;
import me.damascus2000.pingo.commands.roles.RoleAssign;
import me.damascus2000.pingo.commands.settings.CommandState;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.commands.settings.Settings;
import me.damascus2000.pingo.commands.suggestion.EditSuggestion;
import me.damascus2000.pingo.commands.suggestion.ListIssues;
import me.damascus2000.pingo.commands.suggestion.Suggest;
import me.damascus2000.pingo.companions.DataCompanion;
import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.discordbots.api.client.DiscordBotListAPI;
import org.kohsuke.github.GitHub;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.utils.Utils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {

    public static String pathname;
    private final Random random;
    private final HashMap<String, Command> commands;
    private final GameCompanion gameCompanion;
    private final DataCompanion dataCompanion;

    public CommandHandler(GitHub gitHub, DiscordBotListAPI topGGApi, GameCompanion gameCompanion){
        pathname = Utils.config.getProperty("pictures.path");
        random = new Random();
        CommandHandler commh = this;
        this.gameCompanion = gameCompanion;
        dataCompanion = new DataCompanion();
        commands = new HashMap<>();
        Help help = new Help(gameCompanion);
        registerCommand(help);
        registerCommand(new AddPicture());
        registerCommand(new FuckPingo());
        registerCommand(new DeletePicture(commh, dataCompanion));
        registerCommand(new Nickname());
        registerCommand(new RoleAssign());
        registerCommand(new AddRoleAssign());
        registerCommand(new RemoveRoleAssign());
        registerCommand(new CollectCredits());
        registerCommand(new Weekly());
        registerCommand(new ShowCredits(dataCompanion));
        registerCommand(new BlackJack(gameCompanion));
        registerCommand(new Stand(gameCompanion));
        registerCommand(new Hit(gameCompanion));
        registerCommand(new DoubleDown(gameCompanion));
        registerCommand(new Split(gameCompanion));
        registerCommand(new Suggest());
        registerCommand(new ListIssues(gitHub));
        registerCommand(new EditSuggestion());
        registerCommand(new AdminAbuse());
        registerCommand(new Clean());
        registerCommand(new Records(dataCompanion));
        registerCommand(new Uno(gameCompanion, help));
        registerCommand(new Play(gameCompanion));
        registerCommand(new Draw(gameCompanion));
        registerCommand(new Challenge(gameCompanion));
        registerCommand(new AmongUs());
        registerCommand(new Eval());
        registerCommand(new TeamPicker());
        registerCommand(new Poll());
        registerCommand(new EditRoleAssign());
        registerCommand(new Settings());
        registerCommand(new StartBet(gameCompanion));
        registerCommand(new Bet(gameCompanion));
        registerCommand(new EndBet(gameCompanion));
        registerCommand(new Arguments());
        registerCommand(new Ping(gameCompanion));
        registerCommands(new Blackbox(gameCompanion), new EndBlackbox(gameCompanion));
        registerCommand(new Level(dataCompanion));
        registerCommand(new Achievements());
        registerCommand(new Vote(topGGApi));
        help.setCommands(commands);
    }

    public void registerCommand(Command command){
        this.commands.put(command.getName(), command);
    }

    public void registerCommands(Command... commands){
        for (Command c : commands){
            registerCommand(c);
        }
    }

    public GameCompanion getGameHandler(){
        return gameCompanion;
    }

    public DataCompanion getDataCompanion(){
        return dataCompanion;
    }

    public Set<String> getPcommands(){
        File cdir = new File(pathname);
        Set<String> pcommands = new HashSet<>();
        for (File file : cdir.listFiles()){
            pcommands.add(file.getName().toLowerCase());
        }
        return pcommands;
    }

    public void updateNickName(String name){
        ((FuckPingo) commands.get("fuckpingo")).setNickName(name);
    }


    public void onCommandReceived(GuildMessageReceivedEvent e) throws Exception{
        TextChannel channel = e.getChannel();
        Message message = e.getMessage();
        updateNickName(e.getGuild().getSelfMember().getEffectiveName());
        String[] words = split(message.getContentRaw()).toArray(new String[]{});
        String prefix = new SettingsDataHandler().getStringSetting(e.getGuild().getIdLong(), Setting.PREFIX).get(0);
        String command = words[0].substring(prefix.length());

        boolean commandFound = false;
        Iterator<Command> iterator = commands.values().iterator();


        while (!commandFound && iterator.hasNext()){
            Command c = iterator.next();
            if (c.isCommandFor(command) && (c.getPriveligedGuild() == -1 || c.getPriveligedGuild() == e.getGuild().getIdLong()) && (!c.isBeta() || c.canBeta(e.getGuild().getIdLong()))){
                if (c.getCategory() == Command.Category.MODERATION && !e.getMember().hasPermission(Permission.ADMINISTRATOR))
                    throw new MessageException(CommandState.USER.getError(Utils.getLanguage(e.getGuild().getIdLong())), 5);

                CommandState state = c.canBeExecuted(e.getGuild().getIdLong(), channel.getIdLong(), message.getMember());
                if (state != CommandState.ENABLED)
                    throw new MessageException(state.getError(Utils.getLanguage(e.getGuild().getIdLong())), 5);

                c.run(Arrays.stream(words, 1, words.length).filter(arg -> !arg.trim().isEmpty()).toArray(String[]::new), e);
                commandFound = true;
            }
        }
        Set<String> pcommands = getPcommands();
        if (pcommands.contains(command.toLowerCase()) && e.getGuild().getIdLong() == Utils.config.get("special.guild")){
            try {
                File dir = new File(String.format("%s/%s", pathname, command));
                File photo = new File(String.format("%s/%s/%d.jpg", pathname, command, random.nextInt(dir.listFiles().length)));
                channel.sendFile(photo.getAbsoluteFile()).queue();
            } catch (Exception exc){
                exc.printStackTrace();
            }
        }
    }

    private ArrayList<String> split(String subjectString){
        ArrayList<String> matchList = new ArrayList<>();
        Pattern regex = Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'([^'\\\\]*(?:\\\\.[^'\\\\]*)*)'|“([^“”\\\\]*(?:\\\\.[^“”\\\\]*)*)”|“([^“\\\\]*(?:\\\\.[^“\\\\]*)*)“|”([^”\\\\]*(?:\\\\.[^”\\\\]*)*)”|‘([^‘’\\\\]*(?:\\\\.[^‘’\\\\]*)*)’|‘([^‘\\\\]*(?:\\\\.[^‘\\\\]*)*)‘|’([^’\\\\]*(?:\\\\.[^’\\\\]*)*)’|[^\\s]+");
        Matcher regexMatcher = regex.matcher(subjectString);
        while (regexMatcher.find()){
            if (regexMatcher.group(1) != null){
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null){
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else if (regexMatcher.group(3) != null){
                // Add left right double-quoted string without the quotes
                matchList.add(regexMatcher.group(3));
            } else if (regexMatcher.group(4) != null){
                // Add left double-quoted string without the quotes
                matchList.add(regexMatcher.group(4));
            } else if (regexMatcher.group(5) != null){
                // Add right double-quoted string without the quotes
                matchList.add(regexMatcher.group(5));
            } else if (regexMatcher.group(6) != null){
                // Add left right single-quoted string without the quotes
                matchList.add(regexMatcher.group(6));
            } else if (regexMatcher.group(7) != null){
                // Add left single-quoted string without the quotes
                matchList.add(regexMatcher.group(7));
            } else if (regexMatcher.group(8) != null){
                // Add right single-quoted string without the quotes
                matchList.add(regexMatcher.group(8));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }
        return matchList;
    }
}

