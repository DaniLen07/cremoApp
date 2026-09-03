package com.deli.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.data-reset.enabled", havingValue = "true")
public class DataCleanupScheduler {
    private final CremoService cremoService;

    public DataCleanupScheduler(CremoService cremoService) {
        this.cremoService = cremoService;
    }

    @Scheduled(fixedDelayString = "${app.data-reset.interval-ms:259200000}", initialDelayString = "${app.data-reset.interval-ms:259200000}")
    public void resetSalesData() {
        cremoService.resetSalesData();
    }
}