package Commands;

import Utils.OpenExplorerData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.*;
import java.util.*;

public class CommandHandler {

    private Random random;

    private Map<String, Command> commands;
    public static final String pathname = "./Pictures";
    private final ArrayList<String> rythmcommands = new ArrayList<>(Arrays.asList("!play", "!stop", "!np", "!queue", "!skipto", "!next", "!skip"));

    public CommandHandler() {
        random = new Random();
        commands = Map.of(
                "help", new Help(),
                "add", new AddPicture(this),
                "fuckpingo", new FuckPingo(),
                "delete", new DeletePicture(this),
                "nickname", new Nickname(),
                "roleassign", new RoleAssign(),
        ((Help) commands.get("help")).setCommands(commands);
    }

    public OpenExplorerData getExplorerData(String command){
        return ((DeletePicture) commands.get("delete")).getExplorerData(command);
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


    public void onCommandReceived(GuildMessageReceivedEvent e) {
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
                    c.run(Arrays.stream(words, 1, words.length).toArray(String[]::new), e);
                } else {
                    channel.sendMessage("❌ You don't have permission to run this command").queue();
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
