package com.pantheon.backend.core.notification;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.notification.event.localscan.LocalScanBatchEvent;
import com.pantheon.backend.core.notification.event.localscan.LocalScanCompletedEvent;
import com.pantheon.backend.core.notification.event.localscan.LocalScanErrorEvent;
import com.pantheon.backend.core.notification.event.localscan.LocalScanStartedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalScanNotificationOrchestrationServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private LocalScanNotificationOrchestrationService service;

    private final int BATCH_SIZE = 10;

    @BeforeEach
    void setUp() {
        service = new LocalScanNotificationOrchestrationService(eventPublisher, BATCH_SIZE);
    }

    @Test
    void notifyStart_PublishesStartedEvent() {
        service.notifyStart("Steam", 100);

        ArgumentCaptor<LocalScanStartedEvent> captor = ArgumentCaptor.forClass(LocalScanStartedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        LocalScanStartedEvent event = captor.getValue();
        assertEquals("Steam", event.platformName());
        assertEquals(100, event.totalGames());
    }

    @Test
    void notifyBatch_SmallBatch_PublishesSingleEvent() {
        List<ScannedLocalGameDTO> batch = createDummyGames(5);

        service.notifyBatch("Steam", batch);

        ArgumentCaptor<LocalScanBatchEvent> captor = ArgumentCaptor.forClass(LocalScanBatchEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        LocalScanBatchEvent event = captor.getValue();
        assertEquals("Steam", event.platformName());
        assertEquals(5, event.games().size());
    }

    @Test
    void notifyBatch_LargeBatch_SplitsEvents() {
        List<ScannedLocalGameDTO> batch = createDummyGames(25); // Batch size is 10

        service.notifyBatch("Steam", batch);

        ArgumentCaptor<LocalScanBatchEvent> captor = ArgumentCaptor.forClass(LocalScanBatchEvent.class);
        verify(eventPublisher, times(3)).publishEvent(captor.capture());

        List<LocalScanBatchEvent> events = captor.getAllValues();
        assertEquals(3, events.size());
        assertEquals(10, events.get(0).games().size());
        assertEquals(10, events.get(1).games().size());
        assertEquals(5, events.get(2).games().size());
    }

    @Test
    void notifyComplete_Success_PublishesCompletedEvent() {
        service.notifyComplete("Steam", 50);

        ArgumentCaptor<LocalScanCompletedEvent> captor = ArgumentCaptor.forClass(LocalScanCompletedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        LocalScanCompletedEvent event = captor.getValue();
        assertEquals("Steam", event.platformName());
        assertEquals(50, event.finalCount());
        assertTrue(event.success());
        assertEquals(0, event.failedPathsCount());
    }

    @Test
    void notifyComplete_WithFailures_PublishesCompletedEventWithFailures() {
        List<String> failedPaths = List.of("/path/1", "/path/2");
        service.notifyComplete("Steam", 50, 2, failedPaths);

        ArgumentCaptor<LocalScanCompletedEvent> captor = ArgumentCaptor.forClass(LocalScanCompletedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        LocalScanCompletedEvent event = captor.getValue();
        assertEquals("Steam", event.platformName());
        assertEquals(50, event.finalCount());
        assertTrue(event.success());
        assertEquals(2, event.failedPathsCount());
        assertEquals(failedPaths, event.failedPaths());
    }

    @Test
    void notifyError_WithPaths_PublishesFailedEvent() {
        List<String> failedPaths = List.of("/path/1");
        service.notifyError("Steam", failedPaths);

        ArgumentCaptor<LocalScanCompletedEvent> captor = ArgumentCaptor.forClass(LocalScanCompletedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        LocalScanCompletedEvent event = captor.getValue();
        assertEquals("Steam", event.platformName());
        assertFalse(event.success());
        assertEquals(1, event.failedPathsCount());
        assertEquals(failedPaths, event.failedPaths());
    }

    @Test
    void notifyError_WithMessage_PublishesErrorEvent() {
        service.notifyError("Steam", "Something went wrong");

        ArgumentCaptor<LocalScanErrorEvent> captor = ArgumentCaptor.forClass(LocalScanErrorEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        LocalScanErrorEvent event = captor.getValue();
        assertEquals("Steam", event.platformName());
        assertEquals("Something went wrong", event.errorMessage());
    }

    private List<ScannedLocalGameDTO> createDummyGames(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> ScannedLocalGameDTO.builder().title("Game " + i).build())
                .collect(Collectors.toList());
    }
}
