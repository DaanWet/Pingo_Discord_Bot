package Listeners;

import Commands.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    private final CommandHandler commandListener = new CommandHandler();
    public CommandHandler getCommandHandler(){
        return commandListener;
    }
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        User author = e.getAuthor();
        TextChannel channel = e.getChannel();
        Message message = e.getMessage();
        Guild guild = e.getGuild();

        // Minecraft update
        if (e.isWebhookMessage() || author.isBot()) {
            if (channel.equals(guild.getTextChannelById(686645470835245079L)) && (!message.getContentRaw().startsWith("**Minecraft (Bedrock)"))) {
                guild.getTextChannelById(598619747076276224L).sendMessage(message).queue();
            }
        } else if (!author.isBot()) {
            //Text to speech mute
            if (message.isTTS()) {
                message.delete().complete();
                Role role = guild.getRoleById(598551156867858442L);
                List<Role> roles = e.getMember().getRoles();
                channel.sendMessage(String.format("%s has been muted, cause TTS sucks", e.getMember().getAsMention())).queue();
                guild.modifyMemberRoles(e.getMember(),  role).queue(em -> {
                    guild.modifyMemberRoles(e.getMember(), roles).queueAfter(1, TimeUnit.MINUTES);
                });
            }

            String contentRaw = e.getMessage().getContentRaw();
            // Codex submissions
            if (channel.equals(e.getGuild().getTextChannelById("664230911935512586"))){
                message.delete().queue();
                EmbedBuilder eb = new EmbedBuilder();
                eb.setAuthor(author.getName(), null, author.getEffectiveAvatarUrl());
                eb.setDescription(message.getContentRaw());
                eb.setColor(e.getGuild().getSelfMember().getColorRaw());
                channel.sendMessage(eb.build()).queue(m -> {
                    m.addReaction(":green_tick:667450925677543454").queue();
                    m.addReaction(":indifferent_tick:667450939208368130").queue();
                    m.addReaction(":red_tick:667450953217212436").queue();
                });
            } // Check for commands
            else if (contentRaw.length() > 0 && contentRaw.charAt(0) == '!') {
                commandListener.onCommandReceived(e);
            }

        }



    }

}
