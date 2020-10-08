package listeners;

import casino.GameHandler;
import commands.CommandHandler;
import net.dv8tion.jda.api.Permission;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import casino.uno.UnoGame;
import casino.uno.UnoHand;
import utils.DataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONObject;
import utils.ImageHandler;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static commands.CommandHandler.pathname;

public class ReactionListener extends ListenerAdapter {
    private CommandHandler commandHandler;
    private GitHub gitHub;
    private Random random = new Random();
    private GameHandler gameHandler;

    public ReactionListener(CommandHandler commandHandler, GitHub gitHub, GameHandler gameHandler) {
        this.commandHandler = commandHandler;
        this.gitHub = gitHub;
        this.gameHandler = gameHandler;
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e) {
        User user = e.getUser();
        if (!user.isBot()) {
            if (e.getChannel().getIdLong() == 664230911935512586L) {
                handleSuggestionReaction(e);
            } else if (e.getChannel().getIdLong() == 747228850353733739L) {
                handleBotSuggestion(e);
            } else {
                e.getChannel().retrieveMessageById(e.getMessageId()).queue(m -> {
                    if (m.getAuthor().isBot() && !m.getEmbeds().isEmpty()) {
                        MessageEmbed me = m.getEmbeds().get(0);
                        if (me.getTitle() != null) {
                            if (me.getTitle().contains("Delete pictures from ")) {
                                handleDeleteExplorerReaction(e, m, me);
                            } else if (me.getTitle().contains("Gaming Roles")) {
                                handleRoleReaction(e.getReactionEmote().getAsReactionCode(), e.getGuild(), e.getMember(), true);
                            } else if (me.getTitle().equals("A game of casino.uno is going to start!")) {
                                handleUnoReaction(e.getMember(), m, e.getReactionEmote());
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent e) {
        e.retrieveMember().queue(user -> {
            if (user != null && !user.getUser().isBot()) {
                e.getChannel().retrieveMessageById(e.getMessageId()).queue(m -> {
                    if (m.getAuthor().isBot() && !m.getEmbeds().isEmpty()) {
                        MessageEmbed me = m.getEmbeds().get(0);
                        if (me.getTitle() != null && me.getTitle().contains("Gaming Roles")) {
                            handleRoleReaction(e.getReactionEmote().getAsReactionCode(), e.getGuild(), user, false);
                        }
                    }
                });
            }
        });
    }

    public void handleBotSuggestion(GuildMessageReactionAddEvent e) {
        if (e.getMember().getIdLong() == 223837254118801408L) {
            if (e.getReactionEmote().isEmoji() && e.getReactionEmote().getEmoji().equals("✅")) {
                e.retrieveMessage().queue(m -> {
                    if (m.getEmbeds().size() == 1) {
                        e.getReaction().retrieveUsers().queue(users -> {
                            boolean added = false;
                            for (User user : users) {
                                if (user.isBot() && user.getIdLong() == 589027434611867668L) {
                                    added = true;
                                }
                            }
                            if (!added) {
                                try {
                                    MessageEmbed me = m.getEmbeds().get(0);
                                    if (me.getFooter() != null && me.getFooter().getText() != null) {
                                        GHRepository repo = gitHub.getRepository(me.getFooter().getText().split(" ")[1]);
                                        GHIssueBuilder issue = repo.createIssue(me.getTitle()).body(me.getDescription());
                                        for (MessageEmbed.Field f : me.getFields()) {
                                            if (f.getName().equalsIgnoreCase("Labels")) {
                                                for (String label : me.getFields().get(0).getValue().split(", ")) {
                                                    issue = issue.label(label);
                                                }
                                            }
                                        }
                                        issue.create();
                                        m.addReaction("✅").queue();
                                    }
                                } catch (IOException | NullPointerException ioException) {
                                    e.getChannel().sendMessage(String.format("Oops, something went wrong: %s", ioException.getMessage())).queue();
                                    ioException.printStackTrace();
                                }
                            }
                        });

                    }
                });
            }
        }
    }

    public void handleSuggestionReaction(GuildMessageReactionAddEvent e) {
        User user = e.getUser();
        e.getChannel().retrieveMessageById(e.getMessageId()).queue(m -> {
            switch (e.getReactionEmote().getEmote().getId()) {
                case "667450925677543454":
                    m.removeReaction(":indifferent_tick:667450939208368130", user).queue();
                    m.removeReaction(":red_tick:667450953217212436", user).queue();
                    break;
                case "667450939208368130":
                    m.removeReaction(":green_tick:667450925677543454", user).queue();
                    m.removeReaction(":red_tick:667450953217212436", user).queue();
                    break;
                case "667450953217212436":
                    m.removeReaction(":green_tick:667450925677543454", user).queue();
                    m.removeReaction(":indifferent_tick:667450939208368130", user).queue();
                    break;
            }
            int pro = 0;
            int con = 0;
            int indif = 0;
            for (MessageReaction reaction : m.getReactions()) {
                switch (reaction.getReactionEmote().getEmote().getId()) {
                    case "667450925677543454":
                        pro = reaction.getCount();
                        break;
                    case "667450939208368130":
                        indif = reaction.getCount();
                        break;
                    case "667450953217212436":
                        con = reaction.getCount();
                        break;
                }
            }
            List<MessageEmbed> embedList = m.getEmbeds();
            if (pro >= 6 || con >= 6) {
                if (!embedList.isEmpty()) {
                    MessageEmbed embed = embedList.get(0);
                    EmbedBuilder eb = new EmbedBuilder(embed);
                    if (embed.getFooter() == null || (embed.getFooter().getText().contains("Approved") && con >= 6) || (embed.getFooter().getText().contains("Rejected") && pro >= 6)) {
                        eb.setFooter(String.format("Pro: %d, Con: %d => %s", pro, con, pro >= 6 ? "Approved" : "Rejected"));
                        eb.setTimestamp(LocalDateTime.now(ZoneId.systemDefault()));
                        m.editMessage(eb.build()).queue();
                    }
                }
            }
        });
    }

    public void handleDeleteExplorerReaction(GuildMessageReactionAddEvent e, Message m, MessageEmbed me) {
        User user = e.getUser();
        EmbedBuilder eb = new EmbedBuilder(me);
        int n = Integer.parseInt(me.getDescription().substring(0, me.getDescription().indexOf('.')));
        String command = me.getTitle().substring(me.getTitle().lastIndexOf(' ') + 1);
        String emoji = e.getReactionEmote().getEmoji();

        if (commandHandler.getExplorerData(command).getPlayerId().equals(e.getUserId()) || emoji.equals("❌")) {
            File dir = new File(String.format("%s/%s", pathname, command));
            int max = dir.listFiles().length;
            switch (emoji) {
                case "◀":
                    m.removeReaction(emoji, user).queue();
                    if (n != 0) {
                        n--;
                        eb.setImage(String.format("http://zwervers.wettinck.be/%s/%d&%d=%d", command, n, random.nextInt(), random.nextInt()));
                        eb.setDescription(String.format("%d.jpg", n));
                        m.editMessage(eb.build()).queue();
                    }
                    break;
                case "\uD83D\uDDD1":
                    //remove image

                    File foto = new File(String.format("%s/%s/%d.jpg", pathname, command, n));
                    foto.delete();
                    for (int i = n + 1; i < max; i++) {
                        foto = new File(String.format("%s/%s/%d.jpg", pathname, command, i));
                        foto.renameTo(new File(String.format("%s/%s/%d.jpg", pathname, command, i - 1)));
                    }
                    eb.setImage(String.format("http://zwervers.wettinck.be/%s/%d&%d=%d", command, n, random.nextInt(), random.nextInt()));
                    m.editMessage(eb.build()).queue();
                    if (dir.listFiles().length == 0) {
                        dir.delete();
                        commandHandler.closeExplorer(command, m);
                    } else {
                        m.removeReaction(emoji, user).queue();
                    }
                    break;
                case "▶":
                    m.removeReaction(emoji, user).queue();
                    if (n != max - 1) {
                        n++;
                        eb.setImage(String.format("http://zwervers.wettinck.be/%s/%d&%d=%d", command, n, random.nextInt(), random.nextInt()));
                        eb.setDescription(String.format("%d.jpg", n));
                        m.editMessage(eb.build()).queue();

                    }
                    break;
                case "❌":
                    commandHandler.closeExplorer(command, m);
                    break;
            }
        }
    }

    public void handleRoleReaction(String emote, Guild g, Member m, boolean add) {
        ArrayList<JSONObject> gameroles = new DataHandler().getGameRoles();
        for (JSONObject obj : gameroles) {
            if (emote.equals(obj.get("emoji").toString().substring(1))) {
                if (add) {
                    g.addRoleToMember(m, Objects.requireNonNull(g.getRoleById((long) obj.get("role")))).queue();
                } else {
                    g.removeRoleFromMember(m, Objects.requireNonNull(g.getRoleById((long) obj.get("role")))).queue();
                }

            }
        }
    }

    public void handleUnoReaction(Member member, Message message, MessageReaction.ReactionEmote emoji) {
        UnoGame unoGame = gameHandler.getUnoGame();
        Guild guild = message.getGuild();
        if (emoji.isEmoji() && unoGame != null && message.getIdLong() == unoGame.getMessageID()) {
            ArrayList<UnoHand> hands = unoGame.getHands();
            switch (emoji.getEmoji()) {
                case "▶️":
                    if (unoGame.getStarter() == member.getIdLong() && unoGame.getTurn() == -1) {
                        int turn = unoGame.start();
                        if (turn != -1) {
                            guild.createCategory("Uno")
                                    .addRolePermissionOverride(589030386726600714L, Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList())
                                    .addRolePermissionOverride(203572340280262657L, Collections.emptyList(), Collections.singletonList(Permission.VIEW_CHANNEL)).queue(category -> {
                                unoGame.setCategory(category.getIdLong());
                                guild.modifyCategoryPositions().selectPosition(category.getPosition()).moveTo(2).queue();
                                for (UnoHand hand : hands) {
                                    category.createTextChannel(String.format("%s-casino.uno", hand.getPlayerName()))
                                            .addMemberPermissionOverride(hand.getPlayerId(), Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList())
                                            .addRolePermissionOverride(589030386726600714L, Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList())
                                            .addRolePermissionOverride(203572340280262657L, Collections.emptyList(), Collections.singletonList(Permission.VIEW_CHANNEL)).queue(channel -> {
                                        channel.sendFile(ImageHandler.getCardsImage(hand.getCards()), "hand.png").embed(unoGame.createEmbed(hand.getPlayerId()).setColor(guild.getSelfMember().getColor()).build()).queue(mes -> {
                                            hand.setChannelId(channel.getIdLong());
                                            hand.setMessageId(mes.getIdLong());
                                        });
                                    });
                                }
                            });
                        } else {
                            message.removeReaction((Emote) emoji, member.getUser()).queue();
                        }
                    }
                    break;
                case "❌":
                    if (unoGame.getStarter() == member.getIdLong()) {
                        for (long channelId : unoGame.getHands().stream().map(UnoHand::getChannelId).collect(Collectors.toList())) {
                            if (channelId != -1) guild.getTextChannelById(channelId).delete().queue();
                        }
                        if (unoGame.getTurn() != -1) {
                            guild.getCategoryById(unoGame.getCategory()).delete().queue();
                        }
                        MessageEmbed me = message.getEmbeds().get(0);
                        EmbedBuilder eb = new EmbedBuilder(me);
                        eb.setTitle("The game of casino.uno has been canceled");
                        message.editMessage(eb.build()).queue();
                        gameHandler.removeUnoGame();
                    }
                    break;
                case "\uD83D\uDD90️":
                    if (unoGame.getTurn() == -1 && !hands.stream().map(UnoHand::getPlayerId).collect(Collectors.toList()).contains(member.getIdLong())) {
                        unoGame.addPlayer(member.getIdLong(), member.getEffectiveName());
                        MessageEmbed me = message.getEmbeds().get(0);
                        EmbedBuilder eb = new EmbedBuilder(me);
                        eb.clearFields();
                        MessageEmbed.Field f = me.getFields().get(0);
                        StringBuilder sb = new StringBuilder();
                        for (String name : hands.stream().map(UnoHand::getPlayerName).collect(Collectors.toList())) {
                            sb.append(name);
                            sb.append("\n");
                        }
                        eb.addField(f.getName(), sb.toString().trim(), false);
                        message.editMessage(eb.build()).queue();
                    }
                    break;
            }
        }
    }
}


