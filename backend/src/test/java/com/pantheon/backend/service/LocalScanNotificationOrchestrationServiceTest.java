package com.pantheon.backend.service;

import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.event.localscan.LocalScanBatchEvent;
import com.pantheon.backend.event.localscan.LocalScanCompletedEvent;

import org.springframework.context.ApplicationEventPublisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LocalScanNotificationOrchestrationServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Random random;

    private LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;

    @BeforeEach
    void setUp() {
        localScanNotificationOrchestrationService = new LocalScanNotificationOrchestrationService(eventPublisher, 50);
        random = new Random(51);
    }

    @Test
    @DisplayName("Should successfully emit the start notification")
    void testStart() {

    }

    @Test
    @DisplayName("Should emit one batch event if games count is less than batch size")
    void testSmallBatch() {

        List<ScannedLocalGameDTO> games = createDummyGames(10);

        localScanNotificationOrchestrationService.notifyBatch("Steam", games);

        verify(eventPublisher, times(1)).publishEvent(any(LocalScanBatchEvent.class));
    }

    @Test
    @DisplayName("Should split 105 games into 3 batches (50, 50, 5)")
    void testLargeBatchSplitting() {

        List<ScannedLocalGameDTO> games = createDummyGames(105);

        localScanNotificationOrchestrationService.notifyBatch("Steam", games);

        ArgumentCaptor<LocalScanBatchEvent> captor = ArgumentCaptor.forClass(LocalScanBatchEvent.class);

        verify(eventPublisher, times(3)).publishEvent(captor.capture());

        List<LocalScanBatchEvent> capturedEvents = captor.getAllValues();

        assertEquals(50, capturedEvents.get(0).games().size());
        assertEquals(50, capturedEvents.get(1).games().size());
        assertEquals(5, capturedEvents.get(2).games().size());
    }

    @Test
    @DisplayName("Should notify completion with correct count")
    void testCompletion() {
        localScanNotificationOrchestrationService.notifyComplete("GOG", 500);

        ArgumentCaptor<LocalScanCompletedEvent> captor = ArgumentCaptor.forClass(LocalScanCompletedEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        LocalScanCompletedEvent event = captor.getValue();

        assertEquals("GOG", event.platformName());
        assertEquals(500, event.finalCount());
        assertTrue(event.success());
    }

    private List<ScannedLocalGameDTO> createDummyGames(int count) {
        List<ScannedLocalGameDTO> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(ScannedLocalGameDTO.builder()
                    .title("Game " + random.nextInt(count * 100))
                    .isInstalled(random.nextBoolean())
                    .build());
        }
        return list;
    }

}
