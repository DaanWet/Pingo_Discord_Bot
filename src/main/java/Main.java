import data.handlers.DataHandler;
import data.handlers.GeneralDataHandler;
import listeners.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.log4j.Logger;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import utils.MyProperties;
import utils.Utils;
import utils.logging.ErrorLayout;
import utils.logging.MyFileAppender;

import java.sql.SQLException;
import java.util.Properties;

/**
 * https://discordapp.com/api/oauth2/authorize?client_id=589027434611867668&permissions=738716736&scope=bot
 * https://discordapp.com/api/oauth2/authorize?client_id=589027434611867668&permissions=1573383248&scope=bot
 * https://discord.com/api/oauth2/authorize?client_id=589027434611867668&permissions=8&scope=bot
 */

public class Main {

    static final Logger logger = Logger.getLogger(Main.class.getName());


    public static void main(String[] args) throws Exception{
        Utils.loadProperties();
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
        Runtime.getRuntime().addShutdownHook(new Thread("shutdown") {
            @Override
            public void run(){
                fileAppender.close();
            }
        });
    }

    private static void start(String[] args) throws Exception{
        Utils.findAvailableLanguages();
        MyProperties config = Utils.config;
        DataHandler.setUserId(config.getProperty("jdbc.user"));
        DataHandler.setPASSWD(config.getProperty("jdbc.passwd"));
        DataHandler.setJdbcUrl(config.getProperty("jdbc.url"));
        new GeneralDataHandler().createDatabase();
        JDA jda = JDABuilder.createDefault(config.getProperty("token")).enableIntents(GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).build();
        GitHub github = new GitHubBuilder().withOAuthToken(config.getProperty("gh_token")).build();
        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.listening("!help"));
        jda.setAutoReconnect(true);
        MessageListener ml = new MessageListener(github);
        jda.addEventListener(ml);
        jda.addEventListener(new NicknameHandler());
        jda.addEventListener(new JoinListener());
        CommandHandler ch = ml.getCommandHandler();
        jda.addEventListener(new ReactionListener(ch, github, ch.getGameHandler(), ch.getDataCompanion()));


    }

}

