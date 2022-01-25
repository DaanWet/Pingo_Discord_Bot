package commands.casino;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import data.handlers.CreditDataHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MyResourceBundle;
import utils.Utils;

public class AdminAbuse extends Command {


    public AdminAbuse(){
        this.name = "AdminAbuse";
        this.category = Category.MODERATION;
        this.arguments = "[<member>] <amount>";
        this.description = "give.description";
        this.priveligedGuild = 203572340280262657L;
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
            msg = language.getString("bet.new", dataHandler.addCredits(e.getGuild().getIdLong(), target.getIdLong(), coins));
        } else if (args.length == 2 && e.getMessage().mentionsEveryone()){
            coins = Utils.getInt(args[1]);
            for (Long uuid : dataHandler.getAllCredits(e.getGuild().getIdLong()).keySet()){
                dataHandler.addCredits(e.getGuild().getIdLong(), uuid, coins);
            }
            e.getChannel().sendMessage(language.getString("give.everyone", coins)).queue();
            return;
        } else if (args.length == 2 && e.getMessage().getMentions().size() == 1){
            coins = Utils.getInt(args[1]);
            target = e.getMessage().getMentionedMembers().get(0);
            msg = language.getString("give.success", target.getEffectiveName(), dataHandler.addCredits(e.getGuild().getIdLong(), target.getIdLong(), coins));
        } else {
            return;
        }
        e.getChannel().sendMessage(msg).queue();
    }
}
