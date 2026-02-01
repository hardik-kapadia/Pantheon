package com.pantheon.backend.core.notification.event.listener;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.notification.NotificationService;
import com.pantheon.backend.core.notification.event.localscan.LocalScanBatchEvent;
import com.pantheon.backend.core.notification.event.localscan.LocalScanCompletedEvent;
import com.pantheon.backend.core.notification.event.localscan.LocalScanErrorEvent;
import com.pantheon.backend.core.notification.event.localscan.LocalScanStartedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LibraryScanEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LibraryScanEventListener libraryScanEventListener;

    @Test
    void onScanStarted_BroadcastsEvent() {
        LocalScanStartedEvent event = new LocalScanStartedEvent("Steam", 100);

        libraryScanEventListener.onScanStarted(event);

        verify(notificationService).broadcast("LOCAL_SCAN_STARTED", event);
    }

    @Test
    void onScanBatch_BroadcastsEvent() {
        List<ScannedLocalGameDTO> games = Collections.singletonList(ScannedLocalGameDTO.builder().title("Game 1").build());
        LocalScanBatchEvent event = new LocalScanBatchEvent("Steam", games);

        libraryScanEventListener.onScanBatch(event);

        verify(notificationService).broadcast("LOCAL_SCAN_BATCH", event);
    }

    @Test
    void onScanCompleted_BroadcastsEvent() {
        LocalScanCompletedEvent event = new LocalScanCompletedEvent("Steam", 50, true, 0, Collections.emptyList());

        libraryScanEventListener.onScanCompleted(event);

        verify(notificationService).broadcast("LOCAL_SCAN_COMPLETED", event);
    }

    @Test
    void onScanFailed_BroadcastsEvent() {
        LocalScanErrorEvent event = new LocalScanErrorEvent("Steam", "Error message");

        libraryScanEventListener.onScanFailed(event);

        verify(notificationService).broadcast("LOCAL_SCAN_FAILED", event);
    }
}
