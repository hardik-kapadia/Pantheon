package com.pantheon.backend.core.inventory.local;

import com.pantheon.backend.core.inventory.local.processor.InventoryLocalScanService;
import com.pantheon.backend.core.notification.LocalScanNotificationOrchestrationService;
import com.pantheon.backend.core.platform.model.Platform;
import com.pantheon.backend.core.platform.repository.PlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryLocalDiscoveryServiceTest {

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;

    @Mock
    private InventoryLocalScanService inventoryLocalScanService;

    @InjectMocks
    private InventoryLocalDiscoveryService inventoryLocalDiscoveryService;

    private Platform steamPlatform;
    private Platform epicPlatform;

    @BeforeEach
    void setUp() {
        steamPlatform = Platform.builder().name("Steam").build();
        epicPlatform = Platform.builder().name("Epic").build();
    }

    @Test
    void scanPlatforms_NoArgs_ScansAllPlatforms() {
        when(platformRepository.findAll()).thenReturn(Arrays.asList(steamPlatform, epicPlatform));

        inventoryLocalDiscoveryService.scanPlatforms();

        verify(platformRepository).findAll();
        verify(inventoryLocalScanService).scanPlatformPaths(steamPlatform);
        verify(inventoryLocalScanService).scanPlatformPaths(epicPlatform);
    }

    @Test
    void scanPlatforms_WithArgs_ScansSpecificPlatforms() {
        when(platformRepository.findByName("Steam")).thenReturn(Optional.of(steamPlatform));

        inventoryLocalDiscoveryService.scanPlatforms(new String[]{"Steam"});

        verify(platformRepository, never()).findAll();
        verify(platformRepository).findByName("Steam");
        verify(inventoryLocalScanService).scanPlatformPaths(steamPlatform);
        verify(inventoryLocalScanService, never()).scanPlatformPaths(epicPlatform);
    }

    @Test
    void scanPlatforms_WithInvalidPlatform_LogsErrorAndSkips() {
        when(platformRepository.findByName("Unknown")).thenReturn(Optional.empty());

        inventoryLocalDiscoveryService.scanPlatforms(new String[]{"Unknown"});

        verify(localScanNotificationOrchestrationService).notifyError(anyString(), anyString());
        verify(inventoryLocalScanService, never()).scanPlatformPaths(any());
    }

    @Test
    void scanPlatform_ValidPlatform_ScansPlatform() {
        when(platformRepository.findByName("Steam")).thenReturn(Optional.of(steamPlatform));

        inventoryLocalDiscoveryService.scanPlatform("Steam");

        verify(inventoryLocalScanService).scanPlatformPaths(steamPlatform);
    }

    @Test
    void scanPlatform_InvalidPlatform_ThrowsException() {
        when(platformRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> inventoryLocalDiscoveryService.scanPlatform("Unknown"));

        verify(localScanNotificationOrchestrationService).notifyError(anyString(), anyString());
    }
}
