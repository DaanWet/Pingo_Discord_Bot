package me.damascus2000.pingo.services;

import me.damascus2000.pingo.commands.Command;
import me.damascus2000.pingo.companions.Record;
import me.damascus2000.pingo.companions.paginators.RecordPaginator;
import me.damascus2000.pingo.models.*;
import me.damascus2000.pingo.repositories.AchievementRepository;
import me.damascus2000.pingo.repositories.MemberRepository;
import me.damascus2000.pingo.repositories.RecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MemberService {


    private final MemberRepository repository;
    private final RecordRepository recordRepository;

    public MemberService(MemberRepository memberRepository, RecordRepository recordRepository){
        this.repository = memberRepository;
        this.recordRepository = recordRepository;
    }

    private Member getMember(long guildId, long userId){
        return repository.findById(new GuildUserId(guildId, userId)).orElse(new Member(guildId, userId));
    }

    public int getCredits(long guildId, long userId){
        return getMember(guildId, userId).getCredits();
    }

    public Page<Member> getMembers(long guildId, Pageable page){
        return repository.findByGuildIdOrderByCreditsDesc(guildId, page);
    }

    // TODO: This does not have the same functionality as before, now players can appear twice in the global overview,
    // TODO: this has to be fixed, if needed, in the repository
    public Page<Member> getMembers(Pageable page){
        return repository.getAllByOrderByCreditsDesc(page);
    }

    public int addCredits(long guildId, long userId, int credits){
        Optional<Member> optional = repository.findById(new GuildUserId(guildId, userId));
        Member m = getMember(guildId, userId);
        m.setCredits(m.getCredits() + credits);

        repository.save(m);
        int newCredits = m.getCredits();
        Optional<UserRecord> optRecord = recordRepository.findById(new RecordId(guildId, userId, Record.CREDITS));
        UserRecord record = new UserRecord(guildId, userId, Record.CREDITS);
        if (optRecord.isPresent()){
            record = optRecord.get();
        }
        if (newCredits > record.getValue()){
            record.setValue(newCredits);
        }
        recordRepository.save(record);
        return newCredits;
    }

    public LocalDateTime getLastDaily(long guildId, long userId){
        return getMember(guildId, userId).getLastDaily();
    }

    public void setLastDaily(long guildId, long userId, LocalDateTime time){
            Member m = getMember(guildId, userId);
            m.setLastDaily(time);
            repository.save(m);
    }

    public LocalDateTime getLastWeekly(long guildId, long userId){
        return getMember(guildId, userId).getLastWeekly();
    }

    public void setLastWeekly(long guildId, long userId, LocalDateTime time){
        Member m = getMember(guildId, userId);
        m.setLastWeekly(time);
        repository.save(m);
    }

    public int getXP(long guildId, long userId){
        Optional<Member> optional = repository.findById(new GuildUserId(guildId, userId));
        return optional.map(Member::getExperience).orElse(0);
    }

    public int addXP(long guildId, long userId, int experience){
        if (!Command.betaGuilds.contains(guildId))
            return -1;
        Member member = getMember(guildId, userId);
        member.setExperience(member.getExperience() + experience);
        repository.save(member);
        return member.getExperience();
    }

}
