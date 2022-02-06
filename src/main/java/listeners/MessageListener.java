package listeners;

import commands.settings.Setting;
import data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.kohsuke.github.GitHub;
import utils.EmbedException;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    static final Logger logger = Logger.getLogger(MessageListener.class.getName());
    private final CommandHandler commandListener;

    public MessageListener(GitHub github){
        this.commandListener = new CommandHandler(github);

    }

    public CommandHandler getCommandHandler(){
        return commandListener;
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent e){
        if (e.getMessage().getContentRaw().equals("[Original Message Deleted]")) e.getMessage().delete().queue();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e){
        User author = e.getAuthor();
        TextChannel channel = e.getChannel();
        Message message = e.getMessage();
        Guild guild = e.getGuild();

        Properties config = Utils.config;
        // Minecraft update
        if (e.isWebhookMessage() || author.isBot()){
            if (channel.getIdLong() == (long) config.get("special.mc") && (!message.getContentRaw().startsWith("**Minecraft - Beta"))){
                guild.getTextChannelById((long) config.get("special.mc2")).sendMessage(message).queue();
            }
        } else if (!author.isBot()){
            //Text to speech mute
            if (message.isTTS()){
                message.delete().complete();
                MyResourceBundle language = Utils.getLanguage(guild.getIdLong());
                channel.sendMessage(language.getString("tts.muted", e.getMember().getAsMention())).queue();
                Role role = guild.getRoleById((long) config.get("special.muted"));
                List<Role> roles = e.getMember().getRoles();
                guild.modifyMemberRoles(e.getMember(), role).queue(em -> guild.modifyMemberRoles(e.getMember(), roles).queueAfter(1, TimeUnit.MINUTES));
            }

            String contentRaw = e.getMessage().getContentRaw();
            // Codex submissions
            if (channel.getIdLong() == (long) config.get("special.codex")){
                message.delete().queue();
                buildSuggestion(author, message.getContentRaw(), e.getGuild(), channel);
            } // Check for commands
            else if (contentRaw.length() > 0 && contentRaw.toLowerCase().startsWith(new SettingsDataHandler().getStringSetting(guild.getIdLong(), Setting.PREFIX).get(0))){
                try {
                    commandListener.onCommandReceived(e);
                } catch (EmbedException exc){
                    e.getChannel().sendMessageEmbeds(exc.getEmbed().build()).queue(m -> {
                        if (exc.getDelete() != 0)
                            m.delete().queueAfter(exc.getDelete(), TimeUnit.SECONDS);
                    });
                    logger.info(String.format("Content: %s, error: %s", contentRaw, exc.getMessage()));
                } catch (MessageException exc){
                    e.getChannel().sendMessage(exc.getMessage()).queue(m -> {
                        if (exc.getDelete() != 0)
                            m.delete().queueAfter(exc.getDelete(), TimeUnit.SECONDS);
                    });
                    logger.info(String.format("Content: %s, error: %s", contentRaw, exc.getMessage()));

                } catch (Exception exc){
                    MDC.put("Guild", e.getGuild().getId());
                    MDC.put("User", e.getAuthor().getId());
                    MDC.put("Channel", e.getChannel().getId());
                    MDC.put("Message", e.getMessageId());
                    MDC.put("Content", contentRaw);
                    logger.error(exc.getLocalizedMessage(), exc);
                    e.getChannel().sendMessage(String.format("Oops, something went wrong: %s", exc.getLocalizedMessage())).queue();
                }
                MDC.clear();
            }
        }

    }

    public void buildSuggestion(User author, String content, Guild g, TextChannel channel){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(author.getName(), null, author.getEffectiveAvatarUrl());
        eb.setDescription(content);
        eb.setColor(g.getSelfMember().getColorRaw());
        channel.sendMessageEmbeds(eb.build()).queue(m -> {
            Properties config = Utils.config;
            m.addReaction(config.getProperty("emoji.green_tick")).queue();
            m.addReaction(config.getProperty("emoji.indifferent_tick")).queue();
            m.addReaction(config.getProperty("emoji.red_tick")).queue();
        });
    }

}
