package me.damascus2000.pingo.services;

import me.damascus2000.pingo.repositories.AchievementRepository;
import org.springframework.stereotype.Service;

@Service
public class AchievementService {

    private AchievementRepository repository;

    public AchievementService(AchievementRepository repository){
        this.repository = repository;
    }



}
