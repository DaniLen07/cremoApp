package com.deli.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataCleanupScheduler {
    private final CremoService cremoService;

    public DataCleanupScheduler(CremoService cremoService) {
        this.cremoService = cremoService;
    }

    @Scheduled(fixedDelayString = "${app.data-reset.interval-ms:259200000}", initialDelayString = "${app.data-reset.interval-ms:259200000}")
    public void resetOperationalData() {
        cremoService.resetOperationalData();
    }
}