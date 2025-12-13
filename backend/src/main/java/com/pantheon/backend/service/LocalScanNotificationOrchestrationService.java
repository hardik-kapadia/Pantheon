package com.pantheon.backend.service;

import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.event.localscan.LocalScanBatchEvent;
import com.pantheon.backend.event.localscan.LocalScanCompletedEvent;
import com.pantheon.backend.event.localscan.LocalScanStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalScanNotificationOrchestrationService {

    private final ApplicationEventPublisher eventPublisher;

    @Value("app.notification.batch.size")
    private static final int BATCH_SIZE = 100;

    public void notifyStart(String platformName) {
        notifyStart(platformName, 0);
    }

    public void notifyStart(String platformName, int totalExpected) {
        log.info("Starting scan notification for {} (Expect ~{} games)", platformName, totalExpected);
        eventPublisher.publishEvent(new LocalScanStartedEvent(platformName, totalExpected));
    }

    public void notifyBatch(String platformName, List<ScannedLocalGameDTO> batch) {

        if (batch.isEmpty()) return;

        if (batch.size() <= BATCH_SIZE) {
            eventPublisher.publishEvent(new LocalScanBatchEvent(platformName, batch));
            return;
        }

        int total = batch.size();
        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(total, i + BATCH_SIZE);
            List<ScannedLocalGameDTO> subList = batch.subList(i, end);
            eventPublisher.publishEvent(new LocalScanBatchEvent(platformName, subList));
        }
    }

    public void notifyComplete(String platformName, int finalCount) {
        log.info("Scan complete for {}. Total processed: {}", platformName, finalCount);
        eventPublisher.publishEvent(new LocalScanCompletedEvent(platformName, finalCount, true));
    }

    public void notifyError(String platformName) {
        log.error("Scan failed for {}", platformName);
        eventPublisher.publishEvent(new LocalScanCompletedEvent(platformName, 0, false));
    }
}
