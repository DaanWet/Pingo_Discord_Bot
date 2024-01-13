package me.damascus2000.pingo.repositories;

import me.damascus2000.pingo.models.GuildUserId;
import me.damascus2000.pingo.models.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, GuildUserId> {

    Optional<Member> findById(GuildUserId guildUserId);

    Optional<Member> findByGuildIdAndUserId(long guildId, long userId);

    List<Member> findByGuildId(long guildId);

    Page<Member> getAllByOrderByCreditsDesc(Pageable pageable);

    Page<Member> findByGuildIdOrderByCreditsDesc(long guildId, Pageable pageable);


}
