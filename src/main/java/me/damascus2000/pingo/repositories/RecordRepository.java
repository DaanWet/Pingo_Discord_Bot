package me.damascus2000.pingo.repositories;

import me.damascus2000.pingo.companions.Record;
import me.damascus2000.pingo.models.RecordId;
import me.damascus2000.pingo.models.UserRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<UserRecord, RecordId> {
    @Override
    Optional<UserRecord> findById(RecordId recordId);

    List<UserRecord> findAllByGuildIdAndUserId(long guildId, long userId);

    List<UserRecord> findAllByGuildId(long guildId);


    List<UserRecord> findAllByGuildIdAndRecordOrderByValueDesc(long guildId, Record record, Pageable pageable);

    List<UserRecord> findAllByRecordOrderByValueDesc(Record record, Pageable pageable);


}
