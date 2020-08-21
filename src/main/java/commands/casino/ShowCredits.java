package commands.casino;

import commands.Command;
import utils.DataHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ShowCredits extends Command {

    private DataHandler dataHandler;

    public ShowCredits(){
        this.name = "ShowCredits";
        this.aliases = new String[]{"bal", "credits", "balance"};
        this.category = "Casino";
        this.dataHandler = new DataHandler();
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) {
        if (args.length == 0){
            e.getChannel().sendMessage(String.format("Your current balance is **%d**", dataHandler.getCredits(e.getAuthor().getId()))).queue();
        } else {
            e.getChannel().sendMessage(this.getUsage()).queue();
        }
    }

    @Override
    public String getDescription() {
        return "Show your current credit balance";
    }
}
