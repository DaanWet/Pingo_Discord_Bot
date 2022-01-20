package listeners;

import commands.*;
import commands.casino.*;
import commands.casino.bet.Bet;
import commands.casino.bet.EndBet;
import commands.casino.bet.StartBet;
import commands.casino.blackjack.*;
import commands.casino.uno.Challenge;
import commands.casino.uno.Draw;
import commands.casino.uno.Play;
import commands.casino.uno.Uno;
import commands.pictures.AddPicture;
import commands.pictures.DeletePicture;
import commands.roles.AddRoleAssign;
import commands.roles.EditRoleAssign;
import commands.roles.RemoveRoleAssign;
import commands.roles.RoleAssign;
import commands.settings.CommandState;
import commands.settings.Setting;
import commands.settings.Settings;
import commands.suggestion.EditSuggestion;
import commands.suggestion.ListIssues;
import commands.suggestion.Suggest;
import companions.GameHandler;
import companions.paginators.OpenExplorerData;
import data.DataHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.kohsuke.github.GitHub;
import utils.MessageException;
import utils.Utils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {

    public static final String pathname = "./Pictures";
    private final Random random;
    private final HashMap<String, Command> commands;
    private final GameHandler gameHandler;

    public CommandHandler(GitHub gitHub){
        random = new Random();
        CommandHandler commh = this;
        gameHandler = new GameHandler();
        commands = new HashMap<>() {
            {
                put("help", new Help(gameHandler));
                put("add", new AddPicture(commh));
                put("fuckpingo", new FuckPingo());
                put("delete", new DeletePicture(commh));
                put("nickname", new Nickname());
                put("roleassign", new RoleAssign());
                put("addRA", new AddRoleAssign());
                put("removeRA", new RemoveRoleAssign());
                put("daily", new CollectEventCredits());
                put("weekly", new Weekly());
                put("balance", new ShowCredits(gameHandler));
                put("blackjack", new BlackJack(gameHandler));
                put("stand", new Stand(gameHandler));
                put("hit", new Hit(gameHandler));
                put("double", new DoubleDown(gameHandler));
                put("split", new Split(gameHandler));
                put("suggest", new Suggest());
                put("issues", new ListIssues(gitHub));
                put("editI", new EditSuggestion());
                put("adminAbuse", new AdminAbuse());
                put("clean", new Clean());
                put("records", new Records(gameHandler));
                put("uno", new Uno(gameHandler));
                put("play", new Play(gameHandler));
                put("draw", new Draw(gameHandler));
                put("challenge", new Challenge(gameHandler));
                put("amongus", new AmongUs());
                put("eval", new Eval());
                put("teampicker", new TeamPicker());
                put("poll", new Poll());
                put("editRA", new EditRoleAssign());
                put("settings", new Settings());
                put("startbet", new StartBet(gameHandler));
                put("bet", new Bet(gameHandler));
                put("endbet", new EndBet(gameHandler));
            }

        };
        ((Help) commands.get("help")).setCommands(commands);
    }

    public OpenExplorerData getExplorerData(String command){
        return ((DeletePicture) commands.get("delete")).getExplorerData(command);
    }

    public GameHandler getGameHandler(){
        return gameHandler;
    }

    public void closeExplorer(String command, Message message){
        ((DeletePicture) commands.get("delete")).closeExplorer(command, message);
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
        String prefix = new DataHandler().getStringSetting(e.getGuild().getIdLong(), Setting.PREFIX).get(0);
        String command = words[0].substring(prefix.length());

        boolean commandFound = false;
        Iterator<Command> iterator = commands.values().iterator();


        while (!commandFound && iterator.hasNext()){
            Command c = iterator.next();
            if (c.isCommandFor(command) && (c.getPriveligedGuild() == -1 || c.getPriveligedGuild() == e.getGuild().getIdLong())){
                if (c.getCategory().equalsIgnoreCase("moderation") && !e.getMember().hasPermission(Permission.ADMINISTRATOR))
                    throw new MessageException(CommandState.USER.getError(Utils.getLanguage(e.getGuild().getIdLong())), 5);

                CommandState state = c.canBeExecuted(e.getGuild().getIdLong(), channel.getIdLong(), message.getMember());
                if (state != CommandState.ENABLED)
                    throw new MessageException(state.getError(Utils.getLanguage(e.getGuild().getIdLong())), 5);

                c.run(Arrays.stream(words, 1, words.length).filter(arg -> !arg.trim().isEmpty()).toArray(String[]::new), e);
                commandFound = true;
            }
        }
        Set<String> pcommands = getPcommands();
        if (pcommands.contains(command.toLowerCase()) && e.getGuild().getIdLong() == 203572340280262657L){
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

