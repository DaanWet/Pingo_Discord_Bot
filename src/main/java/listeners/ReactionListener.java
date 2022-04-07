package listeners;

import commands.settings.Setting;
import companions.DataCompanion;
import companions.GameCompanion;
import companions.paginators.EmbedPaginator;
import companions.paginators.OpenExplorerData;
import companions.uno.UnoGame;
import companions.uno.UnoHand;
import data.ImageHandler;
import data.handlers.RRDataHandler;
import data.handlers.SettingsDataHandler;
import data.models.RoleAssignRole;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import utils.MyProperties;
import utils.MyResourceBundle;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static listeners.CommandHandler.pathname;

public class ReactionListener extends ListenerAdapter {
    private final CommandHandler commandHandler;
    private final GitHub gitHub;
    private final Random random = new Random();
    private final GameCompanion gameCompanion;
    private final DataCompanion dataCompanion;

    public ReactionListener(CommandHandler commandHandler, GitHub gitHub, GameCompanion gameCompanion, DataCompanion dataCompanion){
        this.commandHandler = commandHandler;
        this.gitHub = gitHub;
        this.gameCompanion = gameCompanion;
        this.dataCompanion = dataCompanion;
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e){
        User user = e.getUser();
        if (user.isBot()) return;
        MyProperties config = Utils.config;
        if (e.getChannel().getIdLong() == config.get("special.codex")){
            handleSuggestionReaction(e);
            return;
        } else if (e.getChannel().getIdLong() == config.get("special.suggestion")){
            handleBotSuggestion(e);
            return;
        }
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        String roleCat = new RRDataHandler().getCategory(e.getGuild().getIdLong(), e.getChannel().getIdLong(), e.getMessageIdLong());
        if (roleCat != null){
            try {
                handleRoleReaction(e.getReactionEmote().getAsReactionCode(), e.getGuild(), roleCat, e.getMember(), true);
            } catch (HierarchyException exc){
                if (e.getGuild().getDefaultChannel() != null)
                    e.getGuild().getDefaultChannel().sendMessage(language.getString("roleassign.error.perms.short")).queue();
            }
            return;
        } else if (dataCompanion.getEmbedPaginatorMap(e.getGuild().getIdLong()).contains(e.getMessageIdLong())){
            handlePaginatorReaction(e);
            return;
        }

        Optional<OpenExplorerData> explorerData = dataCompanion.getExplorerData(e.getGuild().getIdLong(), e.getChannel().getIdLong(), e.getMessageIdLong());
        if (explorerData.isPresent()){
            handleExplorerReaction(e, explorerData.get());
            return;
        }
        UnoGame unoGame = gameCompanion.getUnoGame(e.getGuild().getIdLong());
        if (unoGame != null && unoGame.getMessageID() == e.getMessageIdLong() && unoGame.getChannelID() == e.getChannel().getIdLong()){
            e.retrieveMessage().queue(m -> handleUnoReaction(e.getMember(), m, unoGame, e.getReactionEmote()));
        }
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent e){
        e.retrieveMember().queue(u -> {
            String roleCat = new RRDataHandler().getCategory(e.getGuild().getIdLong(), e.getChannel().getIdLong(), e.getMessageIdLong());
            if (roleCat != null){
                try {
                    handleRoleReaction(e.getReactionEmote().getAsReactionCode(), e.getGuild(), roleCat, e.getMember(), false);
                } catch (HierarchyException exc){
                    if (e.getGuild().getDefaultChannel() != null)
                        e.getGuild().getDefaultChannel().sendMessage(Utils.getLanguage(e.getGuild().getIdLong()).getString("roleassign.error.perms.short")).queue();
                }
            }
        });

    }

    public void handlePaginatorReaction(GuildMessageReactionAddEvent e){
        EmbedPaginator paginator = dataCompanion.getEmbedPaginatorMap(e.getGuild().getIdLong()).get(e.getMessageIdLong());
        MyProperties config = Utils.config;
        String emoji = e.getReactionEmote().getEmoji();

        HashMap<String, Runnable> map = new HashMap<>() {{
            put(config.getProperty("emoji.first"), paginator::firstPage);
            put(config.getProperty("emoji.previous"), paginator::previousPage);
            put(config.getProperty("emoji.next"), paginator::nextPage);
            put(config.getProperty("emoji.last"), paginator::lastPage);
        }};
        if (!map.containsKey(emoji))
            return;
        map.get(emoji).run();

        e.retrieveMessage().queue(m -> {
            m.editMessageEmbeds(paginator.createEmbed(e.getGuild().getIdLong())).queue();
            m.removeReaction(e.getReactionEmote().getEmoji(), e.getUser()).queue();
        });
    }


    public void handleBotSuggestion(GuildMessageReactionAddEvent e){
        MyProperties config = Utils.config;
        if (e.getMember().getIdLong() == config.get("special.owner") && e.getReactionEmote().isEmoji() && e.getReactionEmote().getEmoji().equals(config.getProperty("emoji.checkmark"))){
            e.retrieveMessage().queue(m -> {
                if (m.getEmbeds().size() == 1){
                    e.getReaction().retrieveUsers().queue(users -> {
                        boolean added = users.stream().anyMatch(u -> u.isBot() && u.getIdLong() == config.get("special.bot"));
                        if (!added){
                            try {
                                MessageEmbed me = m.getEmbeds().get(0);
                                if (me.getFooter() != null && me.getFooter().getText() != null){
                                    GHRepository repo = gitHub.getRepository(me.getFooter().getText().split(" ")[1]);
                                    GHIssueBuilder issue = repo.createIssue(me.getTitle()).body(me.getDescription());
                                    String title = Utils.getLanguage(e.getGuild().getIdLong()).getString("suggestion.labels");
                                    for (MessageEmbed.Field f : me.getFields()){
                                        if (f.getName().equalsIgnoreCase(title)){
                                            for (String label : me.getFields().get(0).getValue().split(", ")){
                                                issue = issue.label(label);
                                            }
                                        }
                                    }
                                    issue.create();
                                    m.addReaction(config.getProperty("emoji.checkmark")).queue();
                                }
                            } catch (IOException | NullPointerException ioException){
                                e.getChannel().sendMessage(String.format("Oops, something went wrong: %s", ioException.getMessage())).queue();
                                ioException.printStackTrace();
                            }
                        }
                    });

                }
            });
        }
    }

    public void handleSuggestionReaction(GuildMessageReactionAddEvent e){
        User user = e.getUser();
        e.getChannel().retrieveMessageById(e.getMessageId()).queue(m -> {
            MyProperties config = Utils.config;
            HashMap<String, Runnable> map = new HashMap<>() {{
                put(config.getProperty("emoji.green_tick"), () -> {
                    m.removeReaction(config.getProperty("emoji.indifferent_tick"), user).queue();
                    m.removeReaction(config.getProperty("emoji.red_tick"), user).queue();
                });
                put(config.getProperty("emoji.indifferent_tick"), () -> {
                    m.removeReaction(config.getProperty("emoji.green_tick"), user).queue();
                    m.removeReaction(config.getProperty("emoji.red_tick"), user).queue();
                });
                put(config.getProperty("emoji.red_tick"), () -> {
                    m.removeReaction(config.getProperty("emoji.green_tick"), user).queue();
                    m.removeReaction(config.getProperty("emoji.indifferent_tick"), user).queue();
                });
            }};

            map.get(e.getReactionEmote().getEmote().getAsMention()).run();


            int pro = 0;
            int con = 0;
            int indif = 0;

            for (MessageReaction reaction : m.getReactions()){
                String emoji = reaction.getReactionEmote().getEmote().getAsMention();
                if (config.getProperty("emoji.green_tick").equals(emoji)){
                    pro = reaction.getCount();
                } else if (config.getProperty("emoji.indifferent_tick").equals(emoji)){
                    indif = reaction.getCount();
                } else if (config.getProperty("emoji.red_tick").equals(emoji)){
                    con = reaction.getCount();
                }
            }
            List<MessageEmbed> embedList = m.getEmbeds();
            int threshold = (int) Math.ceil((e.getChannel().getMembers().size() - 1) * (double) config.get("special.threshold"));
            if ((pro >= threshold || con >= threshold) && !embedList.isEmpty()){
                MessageEmbed embed = embedList.get(0);
                EmbedBuilder eb = new EmbedBuilder(embed);
                if (embed.getFooter() == null || (embed.getFooter().getText().contains("Approved") && con >= threshold) || (embed.getFooter().getText().contains("Rejected") && pro >= threshold)){
                    eb.setFooter(String.format("Pro: %d, Con: %d => %s", pro, con, pro >= threshold ? "Approved" : "Rejected"));
                    eb.setTimestamp(LocalDateTime.now(ZoneId.systemDefault()));
                    m.editMessageEmbeds(eb.build()).queue();
                }
            }
        });
    }

    public void handleExplorerReaction(GuildMessageReactionAddEvent e, OpenExplorerData explorerData){
        e.retrieveMessage().queue(m -> {
            MessageEmbed me = m.getEmbeds().get(0);
            User user = e.getUser();
            EmbedBuilder eb = new EmbedBuilder(me);
            int n = explorerData.getPage();
            String emoji = e.getReactionEmote().getEmoji();
            String command = explorerData.getCommand();

            MyProperties config = Utils.config;
            if (explorerData.getPlayerId() == e.getUserIdLong() || emoji.equals(config.getProperty("emoji.cancel"))){
                File dir = new File(String.format("%s/%s", pathname, explorerData));
                int max = dir.listFiles().length;


                if (config.getProperty("emoji.previous").equals(emoji)){
                    m.removeReaction(emoji, user).queue();
                    if (n != 0){
                        n--;
                        eb.setImage(String.format("%s/%s/%d&%d=%d", config.getProperty("pictures.url"), command, n, random.nextInt(), random.nextInt()));
                        eb.setDescription(String.format("%d.jpg", n));
                        m.editMessageEmbeds(eb.build()).queue();
                    }
                } else if (config.getProperty("emoji.trash").equals(emoji)){//remove image

                    File photo = new File(String.format("%s/%s/%d.jpg", pathname, command, n));
                    photo.delete();
                    for (int i = n + 1; i < max; i++){
                        photo = new File(String.format("%s/%s/%d.jpg", pathname, command, i));
                        photo.renameTo(new File(String.format("%s/%s/%d.jpg", pathname, command, i - 1)));
                    }
                    eb.setImage(String.format("%s/%s/%d&%d=%d", config.getProperty("pictures.url"), command, n, random.nextInt(), random.nextInt()));
                    m.editMessageEmbeds(eb.build()).queue();
                    if (dir.listFiles().length == 0){
                        dir.delete();
                        dataCompanion.closeExplorer(command, m);
                    } else {
                        m.removeReaction(emoji, user).queue();
                    }
                } else if (config.getProperty("emoji.next").equals(emoji)){
                    m.removeReaction(emoji, user).queue();
                    if (n != max - 1){
                        n++;
                        eb.setImage(String.format("%S/%s/%d&%d=%d", config.getProperty("pictures.url"), command, n, random.nextInt(), random.nextInt()));
                        eb.setDescription(String.format("%d.jpg", n));
                        m.editMessageEmbeds(eb.build()).queue();

                    }
                } else if (config.getProperty("emoji.cancel").equals(emoji)){
                    dataCompanion.closeExplorer(command, m);
                }
            }
        });

    }

    public void handleRoleReaction(String emote, Guild g, String category, Member m, boolean add){
        ArrayList<RoleAssignRole> gameroles = new RRDataHandler().getRoles(g.getIdLong(), category);
        for (RoleAssignRole obj : gameroles){
            if (emote.equals(obj.getEmoji().replaceFirst("<:", "").replaceFirst(">$", ""))){
                if (add){
                    g.addRoleToMember(m, Objects.requireNonNull(g.getRoleById(obj.getRoleId()))).queue();
                } else {
                    g.removeRoleFromMember(m, Objects.requireNonNull(g.getRoleById(obj.getRoleId()))).queue();
                }
            }
        }
    }

    public void handleUnoReaction(Member member, Message message, UnoGame unoGame, MessageReaction.ReactionEmote emoji){
        Guild guild = message.getGuild();
        if (emoji.isEmoji() && unoGame != null && message.getIdLong() == unoGame.getMessageID()){
            ArrayList<UnoHand> hands = unoGame.getHands();
            MyResourceBundle language = Utils.getLanguage(guild.getIdLong());
            String emojiEmoji = emoji.getEmoji();
            MyProperties config = Utils.config;
            if (config.getProperty("emoji.uno.start").equals(emojiEmoji)){
                if (unoGame.getStarter() == member.getIdLong() && unoGame.getTurn() == -1){
                    int turn = unoGame.start();
                    if (turn != -1){
                        guild.createCategory("Uno")
                                .addMemberPermissionOverride(guild.getSelfMember().getIdLong(), Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList())
                                .addRolePermissionOverride(guild.getIdLong(), Collections.emptyList(), Collections.singletonList(Permission.VIEW_CHANNEL)).queue(category -> {
                                    unoGame.setCategory(category.getIdLong());
                                    guild.modifyCategoryPositions().selectPosition(category.getPosition()).moveTo(Math.min(guild.getCategories().size() - 1, 2)).queue();

                                    String prefix = new SettingsDataHandler().getStringSetting(guild.getIdLong(), Setting.PREFIX).get(0);
                                    String help = language.getString("uno.help", prefix);
                                    for (UnoHand hand : hands){
                                        category.createTextChannel(String.format("%s-uno", hand.getPlayerName()))
                                                .addMemberPermissionOverride(hand.getPlayerId(), Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList())
                                                .addMemberPermissionOverride(guild.getSelfMember().getIdLong(), Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList())
                                                .addRolePermissionOverride(guild.getIdLong(), Collections.emptyList(), Collections.singletonList(Permission.VIEW_CHANNEL)).setTopic(help).queue(channel ->
                                                                                                                                                                                                        channel.sendFile(ImageHandler.getCardsImage(hand.getCards()), "hand.png").setEmbeds(unoGame.createEmbed(hand.getPlayerId(), language).setColor(guild.getSelfMember().getColor()).build()).queue(mes -> {
                                                                                                                                                                                                            hand.setChannelId(channel.getIdLong());
                                                                                                                                                                                                            hand.setMessageId(mes.getIdLong());
                                                                                                                                                                                                        })
                                                );
                                    }
                                });
                    } else {
                        message.removeReaction((Emote) emoji, member.getUser()).queue();
                    }
                }
            } else if (config.getProperty("emoji.cancel").equals(emojiEmoji)){
                if (unoGame.getStarter() == member.getIdLong()){
                    for (long channelId : unoGame.getHands().stream().map(UnoHand::getChannelId).collect(Collectors.toList())){
                        if (channelId != -1) guild.getTextChannelById(channelId).delete().queue();
                    }
                    if (unoGame.getTurn() != -1){
                        guild.getCategoryById(unoGame.getCategory()).delete().queue();
                    }
                    MessageEmbed me = message.getEmbeds().get(0);
                    EmbedBuilder eb = new EmbedBuilder(me);
                    eb.setTitle(language.getString("uno.embed.cancelled"));
                    message.editMessageEmbeds(eb.build()).queue();
                    gameCompanion.removeUnoGame(guild.getIdLong());
                }
            } else if (config.getProperty("emoji.uno.join").equals(emojiEmoji)){
                if (unoGame.getTurn() == -1 && !hands.stream().map(UnoHand::getPlayerId).collect(Collectors.toList()).contains(member.getIdLong())){
                    unoGame.addPlayer(member.getIdLong(), member.getEffectiveName());
                    MessageEmbed me = message.getEmbeds().get(0);
                    EmbedBuilder eb = new EmbedBuilder(me);
                    eb.clearFields();
                    MessageEmbed.Field f = me.getFields().get(0);
                    StringBuilder sb = new StringBuilder();
                    for (String name : hands.stream().map(UnoHand::getPlayerName).collect(Collectors.toList())){
                        sb.append(name);
                        sb.append("\n");
                    }
                    eb.addField(f.getName(), sb.toString().trim(), false);
                    message.editMessageEmbeds(eb.build()).queue();
                }
            }
        }
    }
}


