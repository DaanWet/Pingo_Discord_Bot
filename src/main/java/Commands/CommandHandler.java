package commands;

import casino.GameHandler;
import commands.casino.*;
import commands.casino.blackjack.*;
import commands.casino.uno.Draw;
import commands.casino.uno.Play;
import commands.casino.uno.Uno;
import commands.pictures.AddPicture;
import commands.pictures.DeletePicture;
import commands.roles.AddRoleAssign;
import commands.roles.RemoveRoleAssign;
import commands.roles.RoleAssign;
import org.kohsuke.github.GitHub;
import utils.OpenExplorerData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandHandler {

    private Random random;

    private HashMap<String, Command> commands;
    public static final String pathname = "./Pictures";
    private GitHub gitHub;

    private GameHandler gameHandler;

    public CommandHandler(GitHub gitHub) {
        this.gitHub = gitHub;
        random = new Random();
        CommandHandler commh = this;
        gameHandler = new GameHandler();
        commands = new HashMap<>() {
            {
                put("help", new Help());
                put("add", new AddPicture(commh));
                put("fuckpingo", new FuckPingo());
                put("delete", new DeletePicture(commh));
                put("nickname", new Nickname());
                put("roleassign", new RoleAssign());
                put("addRA", new AddRoleAssign());
                put("removeRA", new RemoveRoleAssign());
                put("daily", new CollectCredits());
                put("weekly", new Weekly());
                put("balance", new ShowCredits());
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
                put("records", new Records());
                put("uno", new Uno(gameHandler));
                put("play", new Play(gameHandler));
                put("draw", new Draw(gameHandler));
            }

        };
        ((Help) commands.get("help")).setCommands(commands);
    }

    public OpenExplorerData getExplorerData(String command){
        return ((DeletePicture) commands.get("delete")).getExplorerData(command);
    }

    public GameHandler getGameHandler() {
        return gameHandler;
    }

    public void closeExplorer(String command, Message message){
        ((DeletePicture) commands.get("delete")).closeExplorer(command, message);
    }

    public Set<String> getPcommands(){
        File cdir = new File(pathname);
        Set<String> pcommands = new HashSet<>();
        for (File file : cdir.listFiles()) {
            pcommands.add(file.getName().toLowerCase());
        }
        return pcommands;
    }
    public void updateNickName(String name){
        ((FuckPingo) commands.get("fuckpingo")).setNickName(name);
    }


    public void onCommandReceived(GuildMessageReceivedEvent e) throws Exception{
        User author = e.getAuthor();
        TextChannel channel = e.getChannel();
        Message message = e.getMessage();
        updateNickName(e.getGuild().getMemberById("589027434611867668").getNickname());
        String[] words = message.getContentRaw().split(" ");
        String command = words[0].substring(1);
        for (Command c : commands.values()){
            if (c.isCommandFor(command)){
                if (!c.getCategory().equalsIgnoreCase("moderation") || e.getMember().hasPermission(Permission.ADMINISTRATOR)){
                    /*String[] args = Stream.concat(Arrays.stream(words, 1, words.length), Arrays.stream(words, 1, words.length))
                        .toArray(String[]::new);*/
                    if (!c.getBannedChannels().contains(channel.getIdLong())){
                        c.run(Arrays.stream(words, 1, words.length).toArray(String[]::new), e);
                    } else {
                        e.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                        channel.sendMessage("❌ You can't use that command here").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                    }

                } else {
                    e.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                    channel.sendMessage("❌ You don't have permission to run this command").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                }

            }
        }
        Set<String> pcommands = getPcommands();
        if (pcommands.contains(command.toLowerCase())) {
            try {
                File dir = new File(String.format("%s/%s", pathname, command));
                File foto = new File(String.format("%s/%s/%d.jpg", pathname, command, random.nextInt(dir.listFiles().length)));
                channel.sendFile(foto.getAbsoluteFile()).queue();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
