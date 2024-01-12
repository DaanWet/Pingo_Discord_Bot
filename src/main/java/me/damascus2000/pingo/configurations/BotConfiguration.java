package me.damascus2000.pingo.configurations;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.discordbots.api.client.DiscordBotListAPI;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.List;

@Configuration
@PropertySource("classpath:config.properties")
public class BotConfiguration {

    @Value("${token}")
    private String token;

    @Value("${gh_token}")
    private String githubToken;

    @Value("${top.gg.token}")
    private String topGGToken;

    @Value("${top.gg.token}")
    private String topGGBotId;

    @Bean
    public GitHub getGithub() throws IOException{
        return new GitHubBuilder().withOAuthToken(githubToken).build();
    }

    @Bean
    public DiscordBotListAPI topGGApi(){
        return new DiscordBotListAPI.Builder()
                .token(topGGToken)
                .botId(topGGBotId)
                .build();
    }


    @Bean
    @Autowired
    public JDA discordClient(DiscordBotListAPI api, List<ListenerAdapter> listeners) throws LoginException, InterruptedException{
        JDA jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).build();

        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.listening("!help"));
        jda.setAutoReconnect(true);

        listeners.forEach(jda::addEventListener);
        jda.awaitReady();
        api.setStats(jda.getGuilds().size());
        return jda;
    }


}
