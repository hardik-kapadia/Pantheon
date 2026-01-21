package com.pantheon.backend.service.localdiscovery.helper;

import com.pantheon.backend.core.localscanner.LocalGameLibraryScanner;
import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.exception.ScanFailureException;
import com.pantheon.backend.model.Game;
import com.pantheon.backend.model.LibraryEntry;
import com.pantheon.backend.model.Platform;
import com.pantheon.backend.service.librarydiscovery.local.notification.LocalScanNotificationOrchestrationService;
import com.pantheon.backend.service.librarydiscovery.local.processor.PlatformLocalGamesProcessor;
import com.pantheon.backend.service.librarydiscovery.local.processor.PlatformLocalScanService;
import com.pantheon.backend.service.utils.ScannerUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlatformLocalScanServiceTest {

    // TODO: fix tests for new ScannerUtil Logic

    private static final Logger log = LoggerFactory.getLogger(PlatformLocalScanServiceTest.class);

    @Mock
    private PlatformLocalGamesProcessor platformLocalGamesProcessor;
    @Mock
    private LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;
    @Mock
    private LocalGameLibraryScanner mockSteamScanner;
    @Mock
    private LocalGameLibraryScanner mockEpicScanner;
    @Mock
    private ScannerUtil mockScannerUtil;

    private PlatformLocalScanService platformLocalScanService;

    @BeforeEach
    void setUp() {

        platformLocalScanService = new PlatformLocalScanService(
                localScanNotificationOrchestrationService,
                mockScannerUtil,
                platformLocalGamesProcessor
        );
    }

    @Test
    @DisplayName("No LocalLibraryClient setup: scanPlatform should throw IllegalStateException if the platform name is correct but no LocalLibraryClient has been mapped to it.")
    void testNoLocalLibraryClient() {

        Platform platformMock = mock(Platform.class);

        assertThrows(IllegalStateException.class, () -> platformLocalScanService.scanPlatformPaths(platformMock));

    }

    @Test
    @DisplayName("Complete Success with one library path: scanPlatform should successfully scan games and notify the event listeners")
    void testCompleteSuccessWithOneLibrary() throws IOException {

        String platformName = "Steam";
        Platform platform = getDummyPlatform(platformName, List.of("C:/Games/Steam/common"));

        ScannedLocalGameDTO game1DTO = getDummyGame1DTO(platformName);
        ScannedLocalGameDTO game2DTO = getDummyGame2DTO(platformName, "C:/Games/Steam/common/Dark Souls 3");

        List<ScannedLocalGameDTO> foundGames = Arrays.asList(game1DTO, game2DTO);

        LibraryEntry game1Entry = mock(LibraryEntry.class);

        Game game1Mock = mock(Game.class);
        when(game1Mock.getId()).thenReturn(1);

        Game game2Mock = mock(Game.class);
        when(game2Mock.getId()).thenReturn(2);

        when(mockSteamScanner.scan(Path.of(platform.getLibraryPaths().getFirst()))).thenReturn(foundGames);


        try (var mockedConstruction = Mockito.mockConstruction(LibraryEntry.class)) {

            platformLocalScanService.scanPlatformPaths(platform);

            verify(localScanNotificationOrchestrationService, times(1)).notifyStart(platformName);
            verify(mockSteamScanner, times(1)).scan(Path.of(platform.getLibraryPaths().getFirst()));
            assertEquals(1, mockedConstruction.constructed().size());
            verify(game1Entry, never()).setGame(any(Game.class));
            verify(game1Entry, never()).setPlatform(any(Platform.class));
            verify(localScanNotificationOrchestrationService, times(1)).notifyBatch(platformName, foundGames);
            verify(localScanNotificationOrchestrationService, times(1)).notifyComplete(platformName, 2);
            verify(localScanNotificationOrchestrationService, never()).notifyError(anyString(), anyInt(), anyList());

        }

    }

    @Test
    @DisplayName("Partial Success due to Scan Failures: scanPlatform should successfully scan games in some, but not all libraries and notify the event listeners accordingly")
    void testPartialSuccessDueToScanFailure() throws IOException {

        String platformName = "Steam";

        List<String> libraryPaths = List.of("C:/Games/Steam", "D:/My Games/Steam");

        Platform platform = getDummyPlatform(platformName, libraryPaths);

        ScannedLocalGameDTO game1DTO = getDummyGame1DTO(platformName);
        ScannedLocalGameDTO game2DTO = getDummyGame2DTO(platformName, "D:/My Games/Steam/common/Dark Souls 3");

        List<ScannedLocalGameDTO> gamesInLibrary1 = List.of(game1DTO);
        List<ScannedLocalGameDTO> gamesInLibrary2 = List.of(game2DTO);

        LibraryEntry game1Entry = mock(LibraryEntry.class);

        Game game1Mock = mock(Game.class);
        when(game1Mock.getId()).thenReturn(1);

        when(mockSteamScanner.scan(Path.of(platform.getLibraryPaths().getFirst()))).thenReturn(gamesInLibrary1);
        when(mockSteamScanner.scan(Path.of(platform.getLibraryPaths().getLast()))).thenThrow(ScanFailureException.class);

        try (var mockedConstruction = Mockito.mockConstruction(LibraryEntry.class)) {

            platformLocalScanService.scanPlatformPaths(platform);

            verify(localScanNotificationOrchestrationService, times(1)).notifyStart(platformName);
            verify(mockSteamScanner, times(1)).scan(Path.of(platform.getLibraryPaths().getFirst()));
            verify(mockSteamScanner, times(1)).scan(Path.of(platform.getLibraryPaths().getLast()));
            assertEquals(0, mockedConstruction.constructed().size());
            verify(game1Entry, never()).setGame(any(Game.class));
            verify(game1Entry, never()).setPlatform(any(Platform.class));
            verify(localScanNotificationOrchestrationService, times(1)).notifyBatch(platformName, gamesInLibrary1);
            verify(localScanNotificationOrchestrationService, never()).notifyBatch(platformName, gamesInLibrary2);
            verify(localScanNotificationOrchestrationService, times(1)).notifyComplete(eq(platformName), eq(1), eq(1), eq(List.of("D:/My Games/Steam")));
            verify(localScanNotificationOrchestrationService, never()).notifyError(anyString(), anyInt(), anyList());

        }

    }

    @Test
    @DisplayName("Scan Failure with only one library path: scanPlatform should catch the Scan Exception and notify an error to via the OrchestrationService")
    void testCompleteFailureForSingleLibraryPathDueToScanFailure() throws IOException {

        String platformName = "Steam";
        List<String> libraryPaths = List.of("C:/Games/Steam/common");

        Platform platform = Platform.builder()
                .name(platformName)
                .libraryPaths(libraryPaths)
                .id(1)
                .build();

        when(mockSteamScanner.scan(any(Path.class))).thenThrow(ScanFailureException.class);

        try (var mockedConstruction = Mockito.mockConstruction(LibraryEntry.class)) {

            platformLocalScanService.scanPlatformPaths(platform);

            verify(localScanNotificationOrchestrationService, times(1)).notifyStart(platformName);
            verify(mockSteamScanner, times(1)).scan(Path.of(platform.getLibraryPaths().getFirst()));
            assertEquals(0, mockedConstruction.constructed().size());
            verify(localScanNotificationOrchestrationService, never()).notifyBatch(anyString(), anyList());
            verify(localScanNotificationOrchestrationService, never()).notifyComplete(anyString(), anyInt());
            verify(localScanNotificationOrchestrationService, times(1)).notifyError(eq(platformName), eq(1), eq(libraryPaths));

        }

    }

    @Test
    @DisplayName("Notification Failure with only one library path: scanPlatform should catch the IO Exception and notify an error to via the OrchestrationService")
    void tesNotificationFailureForSingleLibraryPath() throws IOException {

        String platformName = "Steam";
        List<String> libraryPaths = List.of("C:/Games/Steam/common");

        Platform platform = Platform.builder()
                .name(platformName)
                .libraryPaths(libraryPaths)
                .id(1)
                .build();

        when(mockSteamScanner.scan(any(Path.class))).thenThrow(ScanFailureException.class);

        try (var mockedConstruction = Mockito.mockConstruction(LibraryEntry.class)) {

            platformLocalScanService.scanPlatformPaths(platform);

            verify(localScanNotificationOrchestrationService, times(1)).notifyStart(platformName);
            verify(mockSteamScanner, times(1)).scan(Path.of(platform.getLibraryPaths().getFirst()));
            assertEquals(0, mockedConstruction.constructed().size());
            verify(localScanNotificationOrchestrationService, never()).notifyBatch(anyString(), anyList());
            verify(localScanNotificationOrchestrationService, never()).notifyComplete(anyString(), anyInt());
            verify(localScanNotificationOrchestrationService, times(1)).notifyError(eq(platformName), eq(1), eq(libraryPaths));

        }

    }

    private ScannedLocalGameDTO getDummyGame1DTO(String platformName) {
        return ScannedLocalGameDTO.builder()
                .title("Elden Ring")
                .platformGameId("12")
                .platformName(platformName)
                .isInstalled(true)
                .installPath("C:/Games/Steam/common/Elden Ring")
                .playtimeMinutes(1200)
                .lastPlayed(LocalDateTime.of(2025, 10, 15, 20, 30))
                .build();
    }

    private ScannedLocalGameDTO getDummyGame2DTO(String platformName, String installPath) {
        return ScannedLocalGameDTO.builder()
                .title("Dark Souls 3")
                .platformName(platformName)
                .platformGameId("32")
                .isInstalled(true)
                .installPath(installPath)
                .build();
    }

    private Platform getDummyPlatform(String platformName, List<String> libraryPaths) {

        return Platform.builder()
                .name(platformName)
                .libraryPaths(libraryPaths)
                .id(1)
                .build();
    }


}
