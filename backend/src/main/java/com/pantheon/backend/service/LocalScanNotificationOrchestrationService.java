package com.pantheon.backend.service;

import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.event.localscan.LocalScanErrorEvent;
import com.pantheon.backend.event.localscan.LocalScanBatchEvent;
import com.pantheon.backend.event.localscan.LocalScanCompletedEvent;
import com.pantheon.backend.event.localscan.LocalScanStartedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LocalScanNotificationOrchestrationService {

    private final ApplicationEventPublisher eventPublisher;
    private final int batchSize;

    public LocalScanNotificationOrchestrationService(
            ApplicationEventPublisher eventPublisher,
            @Qualifier("sseBatchSize") int batchSize
    ) {
        this.eventPublisher = eventPublisher;
        this.batchSize = batchSize;
    }

    public void notifyStart(String platformName) {
        notifyStart(platformName, 0);
    }

    public void notifyStart(String platformName, int totalExpected) {
        log.info("{}: Starting scan notification (Expect ~{} games)", platformName, totalExpected);
        eventPublisher.publishEvent(new LocalScanStartedEvent(platformName, totalExpected));
    }

    public void notifyBatch(String platformName, List<ScannedLocalGameDTO> batch) {

        if (batch.isEmpty()) return;

        log.info("{}: Starting batch event for {} games", platformName, batch.size());

        if (batch.size() <= batchSize) {
            log.info("{}: Publishing all {} games", platformName, batch.size());
            eventPublisher.publishEvent(new LocalScanBatchEvent(platformName, batch));
        } else {

            int total = batch.size();

            for (int i = 0, batchCount = 1; i < total; i += batchSize, batchCount++) {
                int end = Math.min(total, i + batchSize);
                List<ScannedLocalGameDTO> subList = batch.subList(i, end);
                log.info("{}: Publishing Batch {} - {} games", platformName, batchCount, subList.size());
                eventPublisher.publishEvent(new LocalScanBatchEvent(platformName, subList));
            }
        }
    }

    public void notifyComplete(String platformName, int finalCount) {
        notifyComplete(platformName, finalCount, 0, new ArrayList<>());
    }

    public void notifyComplete(String platformName, int finalCount, int failedPathsCount, List<String> failedPaths) {
        log.info("{}: Scan complete. Total processed: {}", platformName, finalCount);
        eventPublisher.publishEvent(new LocalScanCompletedEvent(platformName, finalCount, true, failedPathsCount, failedPaths));
    }

    public void notifyError(String platformName, int failedPathsCount, List<String> failedPaths) {
        log.error("{}: Scan failed", platformName);
        eventPublisher.publishEvent(new LocalScanCompletedEvent(platformName, 0, false, failedPathsCount, failedPaths));
    }

    public void notifyError(String platformName, List<String> failedPaths) {
        notifyError(platformName, 0, failedPaths);
    }

    public void notifyError(String platformName, String errorMessage) {
        eventPublisher.publishEvent(new LocalScanErrorEvent(platformName, errorMessage));
    }
}
