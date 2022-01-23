package listeners;

import commands.settings.Setting;
import data.DataHandler;
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


        // Minecraft update
        if (e.isWebhookMessage() || author.isBot()) {
            boolean isBedrock = (message.getContentRaw().contains("bedrock") || message.getContentRaw().contains("Bedrock")) && !(message.getContentRaw().contains("java") || message.getContentRaw().contains("Java"));

            if (channel.getIdLong() == 686645470835245079L && !isBedrock) {
                guild.getTextChannelById(685146958997749801L).sendMessage(message).queue();
            }
        } else if (!author.isBot()){
            //Text to speech mute
            if (message.isTTS()){
                message.delete().complete();
                MyResourceBundle language = Utils.getLanguage(guild.getIdLong());
                channel.sendMessage(language.getString("tts.muted", e.getMember().getAsMention())).queue();
                Role role = guild.getRoleById(598551156867858442L);
                List<Role> roles = e.getMember().getRoles();
                guild.modifyMemberRoles(e.getMember(), role).queue(em -> {
                    guild.modifyMemberRoles(e.getMember(), roles).queueAfter(1, TimeUnit.MINUTES);
                });
            }

            String contentRaw = e.getMessage().getContentRaw();
            // Codex submissions
            if (channel.getIdLong() == 664230911935512586L){
                message.delete().queue();
                buildSuggestion(author, message.getContentRaw(), e.getGuild(), channel);
            } // Check for commands
            else if (contentRaw.length() > 0 && contentRaw.toLowerCase().startsWith(new DataHandler().getStringSetting(guild.getIdLong(), Setting.PREFIX).get(0))){
                try {
                    commandListener.onCommandReceived(e);
                } catch (EmbedException exc){
                    e.getChannel().sendMessage(exc.getEmbed().build()).queue(m -> {
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
            } else if (e.getGuild().getIdLong() == 712013079629660171L){
                message.delete().queue();
                e.getJDA().getGuildById(203572340280262657L).getTextChannelById(203572340280262657L).sendMessage(message.getContentRaw()).queue();
            }

        }

    }

    public void buildSuggestion(User author, String content, Guild g, TextChannel channel){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(author.getName(), null, author.getEffectiveAvatarUrl());
        eb.setDescription(content);
        eb.setColor(g.getSelfMember().getColorRaw());
        channel.sendMessage(eb.build()).queue(m -> {
            Properties config = Utils.config;
            m.addReaction(config.getProperty("emoji.green_tick")).queue();
            m.addReaction(config.getProperty("emoji.indifferent_tick")).queue();
            m.addReaction(config.getProperty("emoji.red_tick")).queue();
        });
    }

}
