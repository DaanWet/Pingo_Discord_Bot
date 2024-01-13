package me.damascus2000.pingo.commands.casino.blackjack;

import me.damascus2000.pingo.companions.GameCompanion;
import me.damascus2000.pingo.companions.cardgames.BlackJackGame;
import me.damascus2000.pingo.exceptions.MessageException;
import me.damascus2000.pingo.services.AchievementService;
import me.damascus2000.pingo.services.MemberService;
import me.damascus2000.pingo.services.RecordService;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Stand extends BCommand {

    public Stand(GameCompanion gameCompanion, MemberService memberService, RecordService recordService, AchievementService achievementService){
        super(gameCompanion, memberService, recordService, achievementService);
        this.name = "stand";
    }

    @Override
    public void run(String[] args, GuildMessageReceivedEvent e) throws Exception{
        long id = e.getAuthor().getIdLong();
        long guildId = e.getGuild().getIdLong();
        BlackJackGame bjg = gameCompanion.getBlackJackGame(guildId, id);
        if (args.length == 0 && bjg != null){
            MyResourceBundle language = Utils.getLanguage(guildId);
            if (bjg.getMessageId() == null){
                e.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                throw new MessageException(language.getString("bj.error.fast"), 5);
            }
            bjg.stand();
            updateMessage(e, bjg, language);

        }
    }
}
