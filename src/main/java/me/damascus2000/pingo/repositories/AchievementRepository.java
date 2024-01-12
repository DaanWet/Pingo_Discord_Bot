package me.damascus2000.pingo.repositories;

import me.damascus2000.pingo.models.AchievementId;
import me.damascus2000.pingo.models.GuildUserId;
import me.damascus2000.pingo.models.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AchievementRepository extends JpaRepository<UserAchievement, AchievementId> {

    Optional<UserAchievement> findById(AchievementId achievementId);
}
