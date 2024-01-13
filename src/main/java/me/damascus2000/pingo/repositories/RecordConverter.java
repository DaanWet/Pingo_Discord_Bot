package me.damascus2000.pingo.repositories;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import me.damascus2000.pingo.companions.Record;

@Converter(autoApply = true)
public class RecordConverter implements AttributeConverter<Record, String> {

    @Override
    public String convertToDatabaseColumn(Record record){
        if (record == null){
            return null;
        }
        return record.getName();
    }

    @Override
    public Record convertToEntityAttribute(String s){
        if (s == null){
            return null;
        }
        return Record.getRecord(s).orElseThrow(IllegalArgumentException::new);
    }
}
