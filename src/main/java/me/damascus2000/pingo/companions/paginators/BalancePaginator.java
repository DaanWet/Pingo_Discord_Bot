package me.damascus2000.pingo.companions.paginators;

import me.damascus2000.pingo.models.Member;
import me.damascus2000.pingo.services.MemberService;
import me.damascus2000.pingo.utils.MyResourceBundle;
import me.damascus2000.pingo.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public class BalancePaginator extends EmbedPaginator {

    private final boolean global;
    private final long guildId;

    private final MemberService memberService;

    public BalancePaginator(boolean global, long guildId, MemberService memberService){
        this.global = global;
        this.guildId = guildId;
        this.memberService = memberService;
    }


    @Override
    public MessageEmbed createEmbed(long guild){
        Page<Member> memberPage = global ? memberService.getMembers(PageRequest.of(page, 10)) : memberService.getMembers(guildId, PageRequest.of(page, 10));
        maxPage = memberPage.getTotalPages() - 1;
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder sb = new StringBuilder();


        MyResourceBundle language = Utils.getLanguage(guild);
        eb.setTitle(language.getString(global ? "leaderboard.credits.global" : "leaderboard.credits.title"));
        int i = 10 * page + 1;
        for (Member m : memberPage){
            sb.append("`").append(i).append(i >= 10 ? ".`  " : ". `  ")
                    .append("<@!")
                    .append(m.getUserId())
                    .append(">  **: ").append(m.getCredits()).append(" **\n");
            i++;
        }
        eb.setDescription(sb.toString());
        if (memberPage.getTotalElements() == 0)
            eb.setDescription(language.getString("leaderboard.credits.error"));
        else
            eb.setFooter(language.getString("paginator.footer", page + 1, memberPage.getTotalPages()));
        return eb.build();
    }
}
