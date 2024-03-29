package commands;

import commands.settings.Setting;
import data.handlers.SettingsDataHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.time.LocalDateTime;
import java.util.List;

public class Nickname extends Command {


    public Nickname(){
        this.name = "nickname";
        this.aliases = new String[]{"rename"};
        this.arguments = new String[]{"@Jef \"Bezos\""};
        this.description = "nick.description";

    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        Message m = e.getMessage();
        List<Member> mentionedmembers = m.getMentionedMembers();
        Member target;
        MyResourceBundle language = getLanguage(e);
        try {
            target = mentionedmembers.size() == 1 && args[0].startsWith("<@") ? mentionedmembers.get(0) : e.getGuild().getMemberById(args[0]);
        } catch (Exception exc){
            throw new MessageException(language.getString("nick.error.member"));
        }
        if (args.length >= 2 && (target != null)){

            try {
                String nick = Utils.concat(args, 1).trim();
                if (nick.length() > 32)
                    throw new MessageException(language.getString("nick.error.name"));

                target.modifyNickname(nick).queue();
                new SettingsDataHandler().setCooldown(e.getGuild().getIdLong(), e.getAuthor().getIdLong(), Setting.NICKNAME, LocalDateTime.now());

            } catch (HierarchyException hexc){
                throw new MessageException(language.getString("nick.error.perm"));
            }
        }
    }
}
