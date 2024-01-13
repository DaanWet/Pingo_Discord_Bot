package me.damascus2000.pingo.commands.casino.blackjack;

import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.cardgames.BlackJackGame;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.services.MemberService;
import me.damascus2000.pingo.services.RecordService;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Split extends BCommand {

    public Split(GameCompanion gameCompanion, MemberService memberService, RecordService recordService){
        super(gameCompanion, memberService, recordService);
        this.name = "split";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        MyResourceBundle language = Utils.getLanguage(guildId);
        BlackJackGame bjg = gameCompanion.getBlackJackGame(guildId, id);
        if (args.length == 0 && bjg != null){
            if (!bjg.canSplit())
                throw new MessageException("You can't split your cards");
            if (memberService.getCredits(guildId, id) < 2 * bjg.getBet())
                throw new MessageException(language.getString("credit.error.not_enough.short"));
            if (bjg.getMessageId() == null){
                e.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                throw new MessageException(language.getString("bj.error.fast"), 5);
            }
            bjg.split();
            updateMessage(e, bjg, language);

        }
    }
}
