package commands.casino.blackjack;

import casino.BlackJackGame;
import casino.GameHandler;
import commands.settings.CommandState;
import commands.settings.Setting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DataHandler;
import utils.Utils;
import java.time.LocalDateTime;


public class BlackJack extends BCommand {


    public BlackJack(GameHandler gameHandler) {
        super(gameHandler);
        this.name = "blackjack";
        this.aliases = new String[]{"bj", "21"};
        this.arguments = "<bet>";
        this.description = "Start a blackjack game";
        this.hidden = false;
    }

    @Override
    public CommandState canBeExecuted(long guildId, long channelId, Member member){
        CommandState betting = canBeExecuted(guildId, channelId, member, Setting.BETTING);
        CommandState blackjack = canBeExecuted(guildId, channelId, member, Setting.BLACKJACK);
        return betting.worst(blackjack);
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception {
        User author = e.getAuthor();
        long guildId = e.getGuild().getIdLong();

        if (gameHandler.isUnoChannel(guildId, e.getChannel().getIdLong())) {
            e.getChannel().sendMessage("You can't start a game in a uno channel").queue();
            return;
        }
        long playerId = author.getIdLong();
        int bet = args.length == 0 ? 0 : Utils.getInt(args[0]);
        DataHandler dataHandler = new DataHandler();
        if (args.length != 0 && args[0].matches("(?i)all(-?in)?")) {
            bet = dataHandler.getCredits(guildId, playerId);
        }
        if (bet < 10) {
            e.getChannel().sendMessage("You need to place a bet for at least 10 credits").queue();
            return;
        }
        if (dataHandler.getCredits(guildId, playerId) < bet ) {
            e.getChannel().sendMessage(String.format("You don't have enough credits to make a %d credits bet", bet)).queue();
            return;
        }
        BlackJackGame objg = gameHandler.getBlackJackGame(guildId, playerId);
        if (objg != null) {
            e.getChannel().sendMessage("You're already playing a game").queue();
            return;
        }
        BlackJackGame bjg = new BlackJackGame(bet);
        dataHandler.setCooldown(guildId, playerId, Setting.BLACKJACK, LocalDateTime.now());
        EmbedBuilder eb = bjg.buildEmbed(author.getName());
        if (!bjg.hasEnded()) {
            gameHandler.putBlackJackGame(guildId, playerId, bjg);
        } else {
            int credits = dataHandler.addCredits(guildId, playerId, bjg.getWonCreds());
            eb.addField("Credits", String.format("You now have %d credits", credits), false);
        }
        e.getChannel().sendMessage(eb.build()).queue(m -> {
            if (!bjg.hasEnded()) bjg.setMessageId(m.getIdLong());
            else
                updateRecords(guildId, playerId, dataHandler, bjg.getWonCreds(), m.getJumpUrl());
        });
    }
}
