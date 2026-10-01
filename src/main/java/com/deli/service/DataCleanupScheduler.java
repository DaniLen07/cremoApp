package com.deli.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataCleanupScheduler {
    private final CremoService cremoService;

    public DataCleanupScheduler(CremoService cremoService) {
        this.cremoService = cremoService;
    }

    @Scheduled(cron = "${app.daily-inventory.cron:0 0 0 * * *}", zone = "America/Bogota")
    public void initializeDailyInventory() {
        cremoService.getTodayInventory();
    }
}