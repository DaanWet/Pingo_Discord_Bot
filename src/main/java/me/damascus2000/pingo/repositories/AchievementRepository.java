package me.damascus2000.pingo.repositories;

import me.damascus2000.pingo.models.AchievementCountDTO;
import me.damascus2000.pingo.models.AchievementId;
import me.damascus2000.pingo.models.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<UserAchievement, AchievementId> {

    Optional<UserAchievement> findById(AchievementId achievementId);

    @Query("SELECT new me.damascus2000.pingo.models.AchievementCountDTO(COUNT(a), a.achievement) FROM UserAchievement a WHERE a.achieved = true GROUP BY a.achievement")
    List<AchievementCountDTO> getAchievementsCount();

    @Query("SELECT new me.damascus2000.pingo.models.AchievementCountDTO(COUNT(a), a.achievement) FROM UserAchievement a WHERE a.achieved = true AND a.guildId = ?1 GROUP BY a.achievement")
    List<AchievementCountDTO> getAchievementsCount(long guildId);
}
