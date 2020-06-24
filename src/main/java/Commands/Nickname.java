package Commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.exceptions.HierarchyException;

import java.net.Socket;
import java.util.List;

public class Nickname extends Command{


    public Nickname(){
        name = "nickname";
        aliases = new String[]{"rename", "bijnaam"};
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        Message m = e.getMessage();
        List<Member> mentionedmembers = m.getMentionedMembers();
        System.out.println(args[0]);
        Member target;
        try {
            target = mentionedmembers.size() == 1 && args[0].startsWith("<@") ? mentionedmembers.get(0) : e.getGuild().getMemberById(args[0]);
        } catch (Exception exc){
            target = null;
            e.getChannel().sendMessage("Usage: !nickname <Member> <Nickname>").queue();
        }
        if ( args.length >= 2 && (target != null )){
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++){
                    System.out.println(i);
                    System.out.println(args[i]);
                    sb.append(args[i]).append(" ");
                }
                if(sb.toString().trim().length() <= 32){
                    target.modifyNickname(sb.toString().trim()).queue();
                } else {
                    e.getChannel().sendMessage("Nickname too long").queue();
                }
            } catch (HierarchyException hexc){
                e.getChannel().sendMessage("Sorry, I can't change the nickname of my master").queue();
            }

        }
    }

    @Override
    public String getDescription() {
        return "Edits the nickname of a given person";
    }
}
