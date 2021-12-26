package commands.casino;

import commands.Command;
import commands.settings.CommandState;
import commands.settings.Setting;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;
import utils.Utils;

public class AdminAbuse extends Command {


    public AdminAbuse(){
        this.name = "AdminAbuse";
        this.category = "Moderation";
        this.arguments = "[<member>] <amount>";
        this.description = "Give coins to someone";
        this.priveligedGuild = 203572340280262657L;
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        return canBeExecuted(guildId, channelId, member, Setting.BETTING);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        DataHandler dataHandler = new DataHandler();
        int coins = 0;
        Member target;
        String msg;
        if (args.length == 1){
            coins = Utils.getInt(args[0]);
            target = e.getMember();
            msg = "You now have **%d** credits";
        } else if (args.length == 2 && e.getMessage().mentionsEveryone()){
            coins = Utils.getInt(args[1]);
            for (Long uuid : dataHandler.getAllCredits(e.getGuild().getIdLong()).keySet()){
                dataHandler.addCredits(e.getGuild().getIdLong(), uuid, coins);
            }
            e.getChannel().sendMessage(String.format("Everyone received **%d** credits", coins)).queue();
            return;
        } else if (args.length == 2 && e.getMessage().getMentions().size() == 1){
            coins = Utils.getInt(args[1]);
            target = e.getMessage().getMentionedMembers().get(0);
            msg = String.format("%s now has **%s** credits", target.getEffectiveName(), "%d");
        } else {
            return;
        }
        if (target == null) return;
        int value = dataHandler.addCredits(e.getGuild().getIdLong(), target.getIdLong(), coins);
        e.getChannel().sendMessage(String.format(msg, value)).queue();
    }
}
