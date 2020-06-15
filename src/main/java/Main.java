import Commands.CommandHandler;
import Listeners.MessageListener;
import Listeners.NicknameHandler;
import Listeners.ReactionListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

/**
 * https://discordapp.com/api/oauth2/authorize?client_id=589027434611867668&permissions=738716736&scope=bot
 * https://discordapp.com/api/oauth2/authorize?client_id=589027434611867668&permissions=1573383248&scope=bot
 */

public class Main {

    public static void main(String[] args) throws Exception{
        JDA jda = new JDABuilder(args[0]).build();
        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("with your balls"));
        jda.setAutoReconnect(true);
        jda.addEventListener(new MessageListener());
        jda.addEventListener(new NicknameHandler());
        jda.addEventListener(new ReactionListener());
    }
}
