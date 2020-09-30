package commands.casino;

import commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;
import utils.Utils;

public class AdminAbuse extends Command {

    private DataHandler dataHandler;


    public AdminAbuse(){
        this.name = "AdminAbuse";
        this.category = "Moderation";
        dataHandler = new DataHandler();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        int coins = 0;
        Member target;
        String msg;
        if (args.length == 1){
            coins = Utils.getInt(args[0]);
            target = e.getMember();
            msg = "You now have **%d** credits";
        }  else if (args.length == 2 && e.getMessage().mentionsEveryone()){
            coins = Utils.getInt(args[1]);
            for (String uuid : dataHandler.getAllCredits().keySet()){
                dataHandler.addCredits(uuid, coins);
            }
            e.getChannel().sendMessage(String.format("Everyone have been given **%d** credits", coins)).queue();
            return;
        } else if (args.length == 2 && e.getMessage().getMentions().size() == 1){
            coins = Utils.getInt(args[1]);
            target = e.getMessage().getMentionedMembers().get(0);
            msg = String.format("%s now has **%s** credits", target.getEffectiveName(), "%d");
        } else {
            return;
        }
        int value = dataHandler.addCredits(target.getId(), coins);
        e.getChannel().sendMessage(String.format(msg, value)).queue();
    }

    @Override
    public String getDescription() {
        return "Give coins to someone";
    }
}
