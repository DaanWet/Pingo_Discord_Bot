package me.damascus2000.pingo.repositories;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import me.damascus2000.pingo.companions.Achievement;

/*
    This is currently the same as @Embbeded String
 */
@Converter(autoApply = true)
public class AchievementConverter implements AttributeConverter<Achievement, String> {

    @Override
    public String convertToDatabaseColumn(Achievement ach){
        if (ach == null){
            return null;
        }
        return ach.name();
    }

    @Override
    public Achievement convertToEntityAttribute(String s){
        if (s == null){
            return null;
        }
        return Achievement.valueOf(s);
    }
}
