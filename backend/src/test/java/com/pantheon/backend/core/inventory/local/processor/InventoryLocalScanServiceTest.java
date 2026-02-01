package com.pantheon.backend.core.inventory.local.processor;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.library.exception.ScanFailureException;
import com.pantheon.backend.core.library.local.LocalGameLibraryScanner;
import com.pantheon.backend.core.library.utils.ScannerUtil;
import com.pantheon.backend.core.notification.LocalScanNotificationOrchestrationService;
import com.pantheon.backend.core.platform.model.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryLocalScanServiceTest {

    @Mock
    private LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;

    @Mock
    private LocalGamesProcessor localGamesProcessor;

    @Mock
    private ScannerUtil scannerUtil;

    @Mock
    private LocalGameLibraryScanner scanner;

    @InjectMocks
    private InventoryLocalScanService inventoryLocalScanService;

    private Platform platform;

    @BeforeEach
    void setUp() {
        platform = Platform.builder()
                .name("Steam")
                .libraryPaths(List.of("/path/to/library"))
                .build();
    }

    @Test
    void scanPlatformPaths_SuccessfulScan() throws ScanFailureException {
        when(scannerUtil.getScannerForPlatform(platform)).thenReturn(scanner);
        when(scanner.getConfiguredLibraryPaths()).thenReturn(List.of("/path/to/library"));
        List<ScannedLocalGameDTO> games = List.of(ScannedLocalGameDTO.builder().title("Game 1").build());
        when(scanner.scan(any(Path.class))).thenReturn(games);

        inventoryLocalScanService.scanPlatformPaths(platform);

        verify(localScanNotificationOrchestrationService).notifyStart("Steam");
        verify(localGamesProcessor).processScannedGames(games, platform);
        verify(localScanNotificationOrchestrationService).notifyBatch("Steam", games);
        verify(localScanNotificationOrchestrationService).notifyComplete("Steam", 1);
    }

    @Test
    void scanPlatformPaths_NoScanner_ThrowsException() {
        when(scannerUtil.getScannerForPlatform(platform)).thenThrow(new IllegalStateException("No scanner"));

        assertThrows(IllegalStateException.class, () -> inventoryLocalScanService.scanPlatformPaths(platform));

        verify(localScanNotificationOrchestrationService).notifyError(eq("Steam"), anyString());
    }

    @Test
    void scanPlatformPaths_NoLibraryPaths_ThrowsException() {
        when(scannerUtil.getScannerForPlatform(platform)).thenReturn(scanner);
        when(scanner.getConfiguredLibraryPaths()).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> inventoryLocalScanService.scanPlatformPaths(platform));

        verify(localScanNotificationOrchestrationService).notifyError(eq("Steam"), anyString());
    }

    @Test
    void scanPlatformPaths_ScanFailure_LogsErrorAndContinues() throws ScanFailureException {
        when(scannerUtil.getScannerForPlatform(platform)).thenReturn(scanner);
        when(scanner.getConfiguredLibraryPaths()).thenReturn(List.of("/path/to/library"));
        doThrow(new ScanFailureException("Scan failed")).when(scanner).scan(any(Path.class));

        inventoryLocalScanService.scanPlatformPaths(platform);

        verify(localScanNotificationOrchestrationService).notifyStart("Steam");
        verify(localGamesProcessor, never()).processScannedGames(anyList(), any(Platform.class));
        verify(localScanNotificationOrchestrationService).notifyError(eq("Steam"), eq(1), anyList());
    }

    @Test
    void scanPlatformPaths_PartialFailure() throws ScanFailureException {
        platform.setLibraryPaths(List.of("/path/1", "/path/2"));
        when(scannerUtil.getScannerForPlatform(platform)).thenReturn(scanner);
        when(scanner.getConfiguredLibraryPaths()).thenReturn(List.of("/path/1", "/path/2"));
        
        // First path fails
        doThrow(new ScanFailureException("Scan failed")).when(scanner).scan(Path.of("/path/1"));
        // Second path succeeds
        List<ScannedLocalGameDTO> games = List.of(ScannedLocalGameDTO.builder().title("Game 1").build());
        when(scanner.scan(Path.of("/path/2"))).thenReturn(games);

        inventoryLocalScanService.scanPlatformPaths(platform);

        verify(localScanNotificationOrchestrationService).notifyStart("Steam");
        verify(localGamesProcessor).processScannedGames(games, platform);
        verify(localScanNotificationOrchestrationService).notifyComplete(eq("Steam"), eq(1), eq(1), anyList());
    }
}
