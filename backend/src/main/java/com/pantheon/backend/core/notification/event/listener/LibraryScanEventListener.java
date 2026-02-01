package com.pantheon.backend.core.notification.event.listener;

import com.pantheon.backend.core.notification.NotificationService;
import com.pantheon.backend.core.notification.event.localscan.LocalScanBatchEvent;
import com.pantheon.backend.core.notification.event.localscan.LocalScanCompletedEvent;
import com.pantheon.backend.core.notification.event.localscan.LocalScanErrorEvent;
import com.pantheon.backend.core.notification.event.localscan.LocalScanStartedEvent;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LibraryScanEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void onScanStarted(LocalScanStartedEvent event) {
        notificationService.broadcast("LOCAL_SCAN_STARTED", event);
    }

    @EventListener
    public void onScanBatch(LocalScanBatchEvent event) {
        notificationService.broadcast("LOCAL_SCAN_BATCH", event);
    }

    @EventListener
    public void onScanCompleted(LocalScanCompletedEvent event) {
        notificationService.broadcast("LOCAL_SCAN_COMPLETED", event);
    }

    @EventListener
    public void onScanFailed(LocalScanErrorEvent event) {
        notificationService.broadcast("LOCAL_SCAN_FAILED", event);
    }
}