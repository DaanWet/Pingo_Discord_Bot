package me.damascus2000.pingo.services;

import me.damascus2000.pingo.companions.Record;
import me.damascus2000.pingo.models.RecordId;
import me.damascus2000.pingo.models.UserRecord;
import me.damascus2000.pingo.repositories.RecordRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecordService {

    private final RecordRepository recordRepository;

    public RecordService(RecordRepository recordRepository){
        this.recordRepository = recordRepository;
    }


    public UserRecord getRecord(long guildId, long userId, Record record){
        return recordRepository.findById(new RecordId(guildId, userId, record)).orElse(new UserRecord(guildId, userId, record));

    }


    public double getRecordValue(long guildId, long userId, Record record){
        return getRecord(guildId, userId, record).getValue();
    }

    public void setRecord(long guildId, long userId, Record record, double value, String link, boolean force){
        UserRecord userRecord = getRecord(guildId, userId, record);
        if (force || userRecord.getValue() > value){
            userRecord.setValue(value);
            userRecord.setLink(link);
        }
        recordRepository.save(userRecord);
    }

    public void setRecord(long guildId, long userId, Record record, double value, boolean force){
        setRecord(guildId, userId, record, value, null, force);
    }

    public List<UserRecord> getRecords(long guildId, long userId){
        return recordRepository.findAllByGuildIdAndUserId(guildId, userId).stream().sorted(Comparator.comparingInt(r -> r.getRecord().getPlace())).collect(Collectors.toList());
    }

    public List<UserRecord> getRecords(long guildId){
        return null;
    }

    public List<UserRecord> getRecords(){
        return null;
    }

    public List<UserRecord> getRecords(long guildId, Record record, Pageable pageable){
        return recordRepository.findAllByGuildIdAndRecordOrderByValueDesc(guildId, record, pageable);
    }

    public List<UserRecord> getRecords(Record record, Pageable pageable){
        return recordRepository.findAllByRecordOrderByValueDesc(record, pageable);
    }


}
