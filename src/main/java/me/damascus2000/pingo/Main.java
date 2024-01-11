package me.damascus2000.pingo;

import me.damascus2000.pingo.commands.Voice;
import me.damascus2000.pingo.companions.VoiceCompanion;
import me.damascus2000.pingo.data.handlers.DataHandler;
import me.damascus2000.pingo.data.handlers.GeneralDataHandler;
import me.damascus2000.pingo.listeners.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.log4j.Logger;
import org.discordbots.api.client.DiscordBotListAPI;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import me.damascus2000.pingo.utils.MyProperties;
import me.damascus2000.pingo.utils.Utils;
import me.damascus2000.pingo.utils.logging.ErrorLayout;
import me.damascus2000.pingo.utils.logging.MyFileAppender;

import java.sql.SQLException;

/**
 * https://discordapp.com/api/oauth2/authorize?client_id=589027434611867668&permissions=738716736&scope=bot
 * https://discordapp.com/api/oauth2/authorize?client_id=589027434611867668&permissions=1573383248&scope=bot
 * https://discord.com/api/oauth2/authorize?client_id=589027434611867668&permissions=8&scope=bot
 */

@SpringBootApplication
public class Main {

    static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception{
        Utils.loadProperties();
        Logger.getRootLogger().removeAllAppenders();
        MyFileAppender fileAppender = new MyFileAppender();
        fileAppender.setLayout(new ErrorLayout());
        Logger.getRootLogger().addAppender(fileAppender);
        try {
            Utils.findAvailableLanguages();
            MyProperties config = Utils.config;
            DataHandler.setUserId(config.getProperty("jdbc.user"));
            DataHandler.setPASSWD(config.getProperty("jdbc.passwd"));
            DataHandler.setJdbcUrl(config.getProperty("jdbc.url"));
            new GeneralDataHandler().createDatabase();
        } catch (SQLException exc){
            logger.fatal("Setting up DB failed", exc);
        } catch (Exception exc){
            logger.fatal("Setting up JDA failed", exc);
        }
        SpringApplication.run(Main.class, args);

        Runtime.getRuntime().addShutdownHook(new Thread("shutdown") {
            @Override
            public void run(){
                fileAppender.close();
            }
        });
    }


    /*public static void main(String[] args) throws Exception{
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
    }*/

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
        DiscordBotListAPI api = new DiscordBotListAPI.Builder()
                .token(config.getProperty("top.gg.token"))
                .botId(config.getProperty("top.gg.botId"))
                .build();
        MessageListener ml = new MessageListener(github, api);
        jda.addEventListener(ml);
        jda.addEventListener(new NicknameHandler());
        jda.addEventListener(new JoinListener(api));
        CommandHandler ch = ml.getCommandHandler();
        VoiceCompanion vc = new VoiceCompanion();
        jda.addEventListener(new VoiceHandler(vc));
        jda.addEventListener(new ReactionListener(ch, github));
        ch.registerCommand(new Voice(vc));
        jda.awaitReady();
        api.setStats(jda.getGuilds().size());
    }

}

