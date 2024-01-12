package me.damascus2000.pingo.commands.casino;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.commands.settings.CommandState;
import me.damascus2000.pingo.commands.settings.Setting;
import me.damascus2000.pingo.data.handlers.CreditDataHandler;
import me.damascus2000.pingo.services.MemberService;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Component;

@Component
public class AdminAbuse extends Command {

    private final MemberService memberService;
    public AdminAbuse(MemberService memberService){
        this.memberService = memberService;
        this.name = "adminAbuse";
        this.category = Category.MODERATION;
        this.arguments = new String[]{"[<member>] <amount>"};
        this.description = "give.description";
        this.priveligedGuild = Utils.config.get("special.guild");
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        CreditDataHandler dataHandler = new CreditDataHandler();
        int coins;
        Member target;
        String msg;
        MyResourceBundle language = Utils.getLanguage(e.getGuild().getIdLong());
        if (args.length == 1){
            coins = Utils.getInt(args[0]);
            target = e.getMember();
            msg = language.getString("bet.new", memberService.addCredits(e.getGuild().getIdLong(), target.getIdLong(), coins));
        } else if (args.length == 2 && e.getMessage().mentionsEveryone()){
            // TODO: remove
            coins = Utils.getInt(args[1]);
            for (Long uuid : dataHandler.getAllCredits(e.getGuild().getIdLong()).keySet()){
                memberService.addCredits(e.getGuild().getIdLong(), uuid, coins);
            }
            e.getChannel().sendMessage(language.getString("give.everyone", coins)).queue();
            return;
        } else if (args.length == 2 && e.getMessage().getMentions().size() == 1){
            coins = Utils.getInt(args[1]);
            target = e.getMessage().getMentionedMembers().get(0);
            msg = language.getString("give.success", target.getEffectiveName(), memberService.addCredits(e.getGuild().getIdLong(), target.getIdLong(), coins));
        } else {
            return;
        }
        e.getChannel().sendMessage(msg).queue();
    }
}