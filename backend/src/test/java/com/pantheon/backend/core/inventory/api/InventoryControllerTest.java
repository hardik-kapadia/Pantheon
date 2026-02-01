package com.pantheon.backend.core.inventory.api;

import com.pantheon.backend.core.inventory.local.InventoryLocalDiscoveryService;
import com.pantheon.backend.core.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryLocalDiscoveryService inventoryLocalDiscoveryService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InventoryController inventoryController;

    @Test
    void scanPlatform_InitiatesScanAndReturnsAccepted() {
        String platform = "Steam";

        ResponseEntity<String> response = inventoryController.scanPlatform(platform);

        verify(inventoryLocalDiscoveryService).scanPlatform(platform);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Scan initiated for Steam", response.getBody());
    }

    @Test
    void scanPlatforms_NoArgs_InitiatesScanForAllAndReturnsAccepted() {
        ResponseEntity<String> response = inventoryController.scanPlatforms(null);

        verify(inventoryLocalDiscoveryService).scanPlatforms(null);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Scan Initiated for all platforms", response.getBody());
    }

    @Test
    void scanPlatforms_WithArgs_InitiatesScanForSpecificPlatformsAndReturnsAccepted() {
        String[] platforms = {"Steam", "Epic"};

        ResponseEntity<String> response = inventoryController.scanPlatforms(platforms);

        verify(inventoryLocalDiscoveryService).scanPlatforms(platforms);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Scan Initiated for [Steam, Epic]", response.getBody());
    }

    @Test
    void streamEvents_SubscribesToNotificationService() {
        SseEmitter emitter = new SseEmitter();
        when(notificationService.subscribe()).thenReturn(emitter);

        SseEmitter result = inventoryController.streamEvents();

        verify(notificationService).subscribe();
        assertNotNull(result);
        assertEquals(emitter, result);
    }
}
