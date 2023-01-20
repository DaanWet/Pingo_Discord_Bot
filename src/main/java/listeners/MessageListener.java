package listeners;

import commands.settings.Setting;
import companions.GameCompanion;
import companions.Question;
import data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.discordbots.api.client.DiscordBotListAPI;
import org.kohsuke.github.GitHub;
import utils.*;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    static final Logger logger = Logger.getLogger(MessageListener.class.getName());
    private final CommandHandler commandListener;
    private final GameCompanion gameCompanion;

    public MessageListener(GitHub github, DiscordBotListAPI topGGApi){
        this.gameCompanion = new GameCompanion();
        this.commandListener = new CommandHandler(github, topGGApi, gameCompanion);
    }

    public CommandHandler getCommandHandler(){
        return commandListener;
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent e){
        if (e.getMessage().getContentRaw().equals("[Original Message Deleted]")) e.getMessage().delete().queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        if (!e.isFromGuild()){
            String[] args = e.getMessage().getContentRaw().split(" ");
            if (args.length > 1 && Utils.isInteger(args[0])){
                Question<String> bb = gameCompanion.getBlackBox(Utils.getInt(args[0]));
                MyResourceBundle language = new MyResourceBundle("i18n", Locale.ENGLISH);
                if (bb == null){
                    e.getChannel().sendMessage(language.getString("blackbox.error.id")).queue();
                    logger.info(String.format("Content: %s, error: %s", e.getMessage().getContentRaw(), language.getString("blackbox.error.id")));
                } else {
                    if (bb.isEnded()){
                        e.getChannel().sendMessage(language.getString("blackbox.error.ended")).queue();
                        logger.info(String.format("Content: %s, error: %s", e.getMessage().getContentRaw(), language.getString("blackbox.error.ended")));
                    }
                    bb.addAnswer(e.getAuthor().getIdLong(), Utils.concat(args, 1));
                    e.getMessage().addReaction(Utils.config.getProperty("emoji.checkmark")).queue();
                }

            }
        }
    }
    public void onGuildMessageReceived(GuildMessageReceivedEvent e){
        User author = e.getAuthor();
        TextChannel channel = e.getChannel();
        Message message = e.getMessage();
        Guild guild = e.getGuild();

        MyProperties config = Utils.config;
        // Minecraft update
        if (e.isWebhookMessage() || author.isBot()){
            boolean isBedrock = (message.getContentRaw().contains("bedrock") || message.getContentRaw().contains("Bedrock")) && !(message.getContentRaw().contains("java") || message.getContentRaw().contains("Java"));

            if (channel.getIdLong() == config.get("special.mc") && !isBedrock){
                guild.getTextChannelById(config.get("special.mc2")).sendMessage(message).queue();
            }
        } else if (!author.isBot()){
            //Text to speech mute
            if (message.isTTS()){
                message.delete().complete();
                MyResourceBundle language = Utils.getLanguage(guild.getIdLong());
                channel.sendMessage(language.getString("tts.muted", e.getMember().getAsMention())).queue();
                Role role = guild.getRoleById(config.get("special.muted"));
                List<Role> roles = e.getMember().getRoles();
                guild.modifyMemberRoles(e.getMember(), role).queue(em -> guild.modifyMemberRoles(e.getMember(), roles).queueAfter(1, TimeUnit.MINUTES));
            }

            String contentRaw = e.getMessage().getContentRaw();
            // Codex submissions
            if (channel.getIdLong() == config.get("special.codex")){
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
            MyProperties config = Utils.config;
            m.addReaction(config.getProperty("emoji.green_tick")).queue();
            m.addReaction(config.getProperty("emoji.indifferent_tick")).queue();
            m.addReaction(config.getProperty("emoji.red_tick")).queue();
        });
    }




}
