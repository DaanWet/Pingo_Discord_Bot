package commands;

import commands.settings.Setting;
import companions.GameCompanion;
import data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.*;

import java.io.File;
import java.util.*;

import static listeners.CommandHandler.pathname;

public class Help extends Command {

    private final GameCompanion gameCompanion;
    private Collection<Command> commands;

    public Help(GameCompanion gameCompanion){
        this.name = "help";
        this.aliases = new String[]{"commands", "command", "h"};
        this.arguments = new String[]{"[command]"};
        this.description = "help.description";
        this.example = "poll";
        this.gameCompanion = gameCompanion;
    }

    public void setCommands(Map<String, Command> comm){
        commands = comm.values();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(e.getGuild().getSelfMember().getColor());
        long guildId = e.getGuild().getIdLong();
        String prefix = new SettingsDataHandler().getStringSetting(guildId, Setting.PREFIX).get(0);
        MyResourceBundle language = Utils.getLanguage(guildId);
        if (args.length == 1){
            if (args[0].equalsIgnoreCase("moderation")){
                if (!e.getMember().hasPermission(Permission.ADMINISTRATOR))
                    throw new EmbedException(language.getString("help.error.perm"));

                eb.setTitle(language.getString("help.moderation"));
                fillCommands(eb, true, guildId, prefix, language);

            } else if (args[0].equalsIgnoreCase("pictures") && guildId == Utils.config.get("special.guild")){
                File dir = new File(pathname);
                eb.setTitle(language.getString("help.pictures"));
                StringBuilder sb = new StringBuilder();
                for (File directory : dir.listFiles()){
                    sb.append("\n").append(language.getString("help.pictures.desc", prefix, directory.getName(), directory.getName()));
                }
                eb.setDescription(sb.toString());
            } else {
                boolean found = false;
                Iterator<Command> iterator = commands.iterator();
                while (!found && iterator.hasNext()){
                    Command command = iterator.next();
                    if (command.isCommandFor(args[0])){
                        eb = command.getHelp(eb, language, prefix);
                        found = true;
                    }
                }
                if (!found){
                    throw new MessageException(language.getString("help.error.command", args[0]));
                }
            }
        } else if (gameCompanion.isUnoChannel(guildId, e.getChannel().getIdLong())){
            getUnoHelp(eb, language, prefix);
        } else {
            eb.setTitle(language.getString("help.commands"));
            fillCommands(eb, false, guildId, prefix, language);
            if (e.getMember().hasPermission(Permission.MANAGE_SERVER))
                eb.appendDescription(language.getString("help.embed.moderation", prefix));
        }
        e.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    public void fillCommands(EmbedBuilder eb, boolean moderation, long guildId, String prefix, MyResourceBundle language){
        eb.setDescription(language.getString("help.embed.description", prefix));
        Map<Category, StringBuilder> sbs = new HashMap<>();
        MyProperties config = Utils.config;
        for (Command c : commands){
            Category cat = c.getCategory();
            if ((cat == null || ((!c.isHidden() && cat == Category.MODERATION == moderation))) && (c.getPriveligedGuild() == -1 || c.getPriveligedGuild() == guildId)){
                if (!sbs.containsKey(cat)){
                    sbs.put(cat, new StringBuilder());
                }
                StringBuilder sb = sbs.get(cat);
                sb.append(String.format("• %s%s\n", c.getName(), c.getAliases().length > 0 ? " / " + c.getAliases()[0] : ""));
            }
        }
        if (guildId == config.get("special.guild") && !moderation)
            sbs.get(Category.PICTURES).append("\n").append(language.getString("help.pictures.list", prefix + "help"));
        Arrays.stream(Category.values()).forEachOrdered(c -> {
            if (sbs.containsKey(c))
                eb.addField(c.getDisplay(), sbs.get(c).toString().trim(), true);
        });
        eb.addField(language.getString("help.embed.links"), language.getString("help.embed.link.2", config.getProperty("github"), config.getProperty("bot.invite"), config.getProperty("server.invite")), false);
        eb.setFooter(language.getString("help.embed.footer"));
    }


    public EmbedBuilder getUnoHelp(EmbedBuilder eb, MyResourceBundle language, String prefix){
        eb.setTitle(language.getString("help.uno"));
        StringBuilder sb = new StringBuilder();
        for (Command c : commands){
            if (c.getCategory() != null && c.getCategory() == Category.UNO){
                sb.append(String.format("\n%s%s %s: *%s*", prefix, c.getName(), c.getArguments().length > 0 ? c.getArguments()[0] : "", language.getString(c.getDescription() == null ? "help.error" : c.getDescription().trim())));
            }
        }
        eb.setDescription(sb.toString());
        return eb;
    }
}
