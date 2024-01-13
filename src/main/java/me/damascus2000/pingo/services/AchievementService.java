package me.damascus2000.pingo.services;

import me.damascus2000.pingo.companions.Achievement;
import me.damascus2000.pingo.models.AchievementCountDTO;
import me.damascus2000.pingo.models.AchievementId;
import me.damascus2000.pingo.models.UserAchievement;
import me.damascus2000.pingo.repositories.AchievementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AchievementService {

    private AchievementRepository repository;

    public AchievementService(AchievementRepository repository){
        this.repository = repository;
    }


    public boolean hasAchieved(long guildId, long userId, Achievement achievement){
        Optional<UserAchievement> optional = repository.findById(new AchievementId(guildId, userId, achievement));
        return optional.map(UserAchievement::isAchieved).orElse(false);
    }

    public void setAchieved(long guildId, long userId, Achievement achievement){
        UserAchievement userAchievement = repository.findById(new AchievementId(guildId, userId, achievement)).orElse(new UserAchievement(guildId, userId, achievement));
        userAchievement.setAchieved(true);
        userAchievement.setTime(LocalDateTime.now());
        repository.save(userAchievement);
    }

    public List<AchievementCountDTO> getAchievementCount(){
        return repository.getAchievementsCount();
    }

    public List<AchievementCountDTO> getAchievementCount(long guildId){
        return repository.getAchievementsCount(guildId);
    }

}
