package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageException;
import utils.MyResourceBundle;
import utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class TeamPicker extends Command {

    public TeamPicker(){
        this.name = "teams";
        this.aliases = new String[]{"t"};
        this.arguments = "<t|p> <number> ";
        this.description = "team.description";
    }


    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        int ateams = 2;
        int playerspt = 0;
        if (args.length < 2)
            throw new MessageException(getUsage(e.getGuild().getIdLong()));


        int offset = 0;
        if (args[0].equalsIgnoreCase("t")){
            ateams = Utils.getInt(args[1]);
            offset = 2;
        } else if (args[0].equalsIgnoreCase("p")){
            playerspt = Utils.getInt(args[1]);
            offset = 2;
        }
        if (playerspt != 0)
            ateams = (int) Math.ceil((args.length - offset) / (double) playerspt);
        playerspt = (int) Math.ceil((args.length - offset) / (double) ateams);
        ArrayList<String> players = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, offset, args.length)));
        Collections.shuffle(players);
        ArrayList<ArrayList<String>> teams = new ArrayList<>();
        int t = 0;
        while (t < ateams){
            int p = 0;
            ArrayList<String> team = new ArrayList<>();
            while (p < playerspt && teams.size() * playerspt + p < players.size()){
                team.add(players.get(teams.size() * playerspt + p));
                p++;
            }
            teams.add(team);
            t++;
        }

        EmbedBuilder eb = new EmbedBuilder();
        MyResourceBundle language = getLanguage(e);
        for (int i = 0; i < teams.size(); i++){
            String teamString = "```\n" + String.join("\n", teams.get(i)) + "```";
            eb.addField(language.getString("team.title", (i + 1)), teamString, true);
            if (i % 2 == 1 && teams.size() % 3 != 0 && teams.size() % 2 == 0) eb.addBlankField(true);
        }
        e.getChannel().sendMessageEmbeds(eb.build()).queue();

    }
}
