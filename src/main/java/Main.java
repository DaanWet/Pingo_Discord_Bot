import listeners.CommandHandler;
import listeners.JoinListener;
import listeners.MessageListener;
import listeners.NicknameHandler;
import listeners.ReactionListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.log4j.*;
import org.kohsuke.github.*;
import data.DataHandler;

import utils.logging.ErrorLayout;
import utils.logging.MyFileAppender;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * https://discordapp.com/api/oauth2/authorize?client_id=589027434611867668&permissions=738716736&scope=bot
 * https://discordapp.com/api/oauth2/authorize?client_id=589027434611867668&permissions=1573383248&scope=bot
 * https://discord.com/api/oauth2/authorize?client_id=589027434611867668&permissions=8&scope=bot
 */

public class Main {

    static final Logger logger = Logger.getLogger(Main.class.getName());


    public static void main(String[] args) throws Exception{
        /*int[] l = new int[]{1000000, 100000, 50000, 10000, 7500, 5000, 3000};
        double[] f = new double[]{0.01, 0.05, 0.105, 0.155, 0.17, 0.2, 0.31};
        double av = 0.0;
        double tot = 0;
        for (int i = 0; i < 7; i++){
            av += l[i] * f[i];
            System.out.println(tot += f[i]);
        }
        System.out.println(av);
        System.out.println(tot);
*/
        Files.createDirectories(Paths.get(MyFileAppender.folder));
        Logger.getRootLogger().removeAllAppenders();
        MyFileAppender fileAppender = new MyFileAppender();
        fileAppender.setLayout(new ErrorLayout());
        Logger.getRootLogger().addAppender(fileAppender);
        try {
            start(args);
        } catch (SQLException exc){
            logger.fatal("Setting up DB failed", exc);
        } catch (Exception exc){
            logger.fatal("Setting up JDA failed", exc);
        }
        Runtime.getRuntime().addShutdownHook(new Thread("shutdown"){
            @Override
            public void run(){
                fileAppender.close();
            }
        });
    }

    private static void start(String[] args) throws Exception{
        DataHandler.setUserId(args[2]);
        DataHandler.setPASSWD(args[3]);
        new DataHandler().createDatabase();
        JDA jda = JDABuilder.createDefault(args[0]).enableIntents(GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).build();
        GitHub github = new GitHubBuilder().withOAuthToken(args[1]).build();
        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.listening("!help"));
        jda.setAutoReconnect(true);
        MessageListener ml = new MessageListener(github);
        jda.addEventListener(ml);
        jda.addEventListener(new NicknameHandler());
        jda.addEventListener(new JoinListener());
        CommandHandler ch = ml.getCommandHandler();
        jda.addEventListener(new ReactionListener(ch, github, ch.getGameHandler()));


    }

}

