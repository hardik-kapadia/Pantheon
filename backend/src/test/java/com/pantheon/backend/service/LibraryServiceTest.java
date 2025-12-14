package com.pantheon.backend.service;

import com.pantheon.backend.client.LocalGameLibraryClient;
import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.mapper.GameMapper;
import com.pantheon.backend.model.Game;
import com.pantheon.backend.model.LibraryEntry;
import com.pantheon.backend.model.Platform;
import com.pantheon.backend.repository.GameRepository;
import com.pantheon.backend.repository.LibraryEntryRepository;
import com.pantheon.backend.repository.PlatformRepository;
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
import java.util.Optional;


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
public class LibraryServiceTest {

    // TODO: Add test for: Partial Success due to failure in notifying
    // TODO: Add test for: Complete Success with multiple library paths

    private static final Logger log = LoggerFactory.getLogger(LibraryServiceTest.class);

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private LibraryEntryRepository libraryEntryRepository;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private PlatformClientMapperService platformClientMapperService;

    @Mock
    private LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;

    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        libraryService = new LibraryService(platformRepository, gameRepository, libraryEntryRepository, platformClientMapperService, gameMapper, localScanNotificationOrchestrationService);
    }

    @Test
    @DisplayName("scanPlatform should throw an Illegal Argument exception if invalid name is provided")
    void testInvalidPlatformName() {

        String invalidName = "XYZ";

        when(platformRepository.findByName("XYZ")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> libraryService.scanPlatform(invalidName));

    }

    @Test
    @DisplayName("scanPlatform should throw IllegalStateException if the platform name is correct but no LocalLibraryClient has been mapped to it.")
    void testNoLocalLibraryClient() {

        String platformName = "XYZ";

        Platform platformMock = mock(Platform.class);

        when(platformRepository.findByName("XYZ")).thenReturn(Optional.of(platformMock));
        when(platformClientMapperService.getScanner(platformMock)).thenThrow(IllegalStateException.class);

        assertThrows(IllegalStateException.class, () -> libraryService.scanPlatform(platformName));

    }

    @Test
    @DisplayName("scanPlatform should successfully scan games and notify the event listeners")
    void testCompleteSuccess() throws IOException {

        String platformName = "Steam";

        Platform platform = Platform.builder()
                .name(platformName)
                .libraryPaths(List.of("C:/Games/Steam/common"))
                .id(1)
                .build();

        ScannedLocalGameDTO game1DTO = ScannedLocalGameDTO.builder()
                .title("Elden Ring")
                .platformGameId("12")
                .platformName(platformName)
                .isInstalled(true)
                .installPath("C:/Games/Steam/common/Elden Ring")
                .playtimeMinutes(1200)
                .lastPlayed(LocalDateTime.of(2025, 10, 15, 20, 30))
                .build();

        ScannedLocalGameDTO game2DTO = ScannedLocalGameDTO.builder()
                .title("Dark Souls 3")
                .platformName(platformName)
                .platformGameId("32")
                .isInstalled(true)
                .installPath("C:/Games/Steam/common/Elden Ring")
                .build();

        List<ScannedLocalGameDTO> foundGames = Arrays.asList(game1DTO, game2DTO);

        LibraryEntry game1Entry = mock(LibraryEntry.class);

        Game game1Mock = mock(Game.class);
        when(game1Mock.getId()).thenReturn(1);
        Game game2Mock = mock(Game.class);
        when(game2Mock.getId()).thenReturn(2);

        LocalGameLibraryClient clientMock = mock(LocalGameLibraryClient.class);

        when(clientMock.scan(Path.of(platform.getLibraryPaths().getFirst()))).thenReturn(foundGames);

        when(platformRepository.findByName(platformName)).thenReturn(Optional.of(platform));

        when(platformClientMapperService.getScanner(platform)).thenReturn(clientMock);

        when(gameRepository.findByTitle(game2DTO.title())).thenReturn(Optional.empty());
        when(gameRepository.findByTitle(game1DTO.title())).thenReturn(Optional.of(game1Mock));

        when(gameRepository.save(game2Mock)).thenReturn(game2Mock);

        when(gameMapper.toEntity(game2DTO)).thenReturn(game2Mock);

        when(libraryEntryRepository.findByGameIdAndPlatformId(anyInt(), anyInt())).thenReturn(Optional.empty());
        when(libraryEntryRepository.findByGameIdAndPlatformId(1, 1)).thenReturn(Optional.of(game1Entry));

        when(libraryEntryRepository.save(any(LibraryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        try (var mockedConstruction = Mockito.mockConstruction(LibraryEntry.class)) {

            libraryService.scanPlatform(platformName);

            verify(localScanNotificationOrchestrationService, times(1)).notifyStart(platformName);
            verify(clientMock, times(1)).scan(Path.of(platform.getLibraryPaths().getFirst()));
            verify(gameMapper, times(1)).toEntity(game2DTO);
            verify(gameRepository, times(2)).findByTitle(anyString());
            verify(gameRepository, times(1)).save(game2Mock);
            verify(libraryEntryRepository, times(2)).findByGameIdAndPlatformId(anyInt(), anyInt());
            assertEquals(1, mockedConstruction.constructed().size());
            verify(game1Entry, never()).setGame(any(Game.class));
            verify(game1Entry, never()).setPlatform(any(Platform.class));
            verify(libraryEntryRepository, times(2)).save(any(LibraryEntry.class));
            verify(localScanNotificationOrchestrationService, times(1)).notifyBatch(platformName, foundGames);
            verify(localScanNotificationOrchestrationService, times(1)).notifyComplete(platformName, 2);
            verify(localScanNotificationOrchestrationService, never()).notifyError(anyString(), anyInt(), anyList());

        }

    }

    @Test
    @DisplayName("scanPlatform should successfully scan games in some, not all libraries and notify the event listeners")
    void testPartialSuccessDueToScanFailure() throws IOException {

        String platformName = "Steam";

        List<String> libraryPaths = List.of("C:/Games/Steam", "D:/My Games/Steam");

        Platform platform = Platform.builder()
                .name(platformName)
                .libraryPaths(libraryPaths)
                .id(1)
                .build();

        ScannedLocalGameDTO game1DTO = ScannedLocalGameDTO.builder()
                .title("Elden Ring")
                .platformGameId("12")
                .platformName(platformName)
                .isInstalled(true)
                .installPath("C:/Games/Steam/common/Elden Ring")
                .playtimeMinutes(1200)
                .lastPlayed(LocalDateTime.of(2025, 10, 15, 20, 30))
                .build();

        ScannedLocalGameDTO game2DTO = ScannedLocalGameDTO.builder()
                .title("Dark Souls 3")
                .platformName(platformName)
                .platformGameId("32")
                .isInstalled(true)
                .installPath("D:/My Games/Steam/common/Elden Ring")
                .build();

        List<ScannedLocalGameDTO> gamesInLibrary1 = List.of(game1DTO);
        List<ScannedLocalGameDTO> gamesInLibrary2 = List.of(game2DTO);

        LibraryEntry game1Entry = mock(LibraryEntry.class);

        Game game1Mock = mock(Game.class);
        when(game1Mock.getId()).thenReturn(1);

        LocalGameLibraryClient clientMock = mock(LocalGameLibraryClient.class);

        when(clientMock.scan(Path.of(platform.getLibraryPaths().getFirst()))).thenReturn(gamesInLibrary1);
        when(clientMock.scan(Path.of(platform.getLibraryPaths().getLast()))).thenThrow(IOException.class);

        when(platformRepository.findByName(platformName)).thenReturn(Optional.of(platform));

        when(platformClientMapperService.getScanner(platform)).thenReturn(clientMock);

        when(gameRepository.findByTitle(game1DTO.title())).thenReturn(Optional.of(game1Mock));

        when(libraryEntryRepository.findByGameIdAndPlatformId(1, 1)).thenReturn(Optional.of(game1Entry));

        when(libraryEntryRepository.save(any(LibraryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (var mockedConstruction = Mockito.mockConstruction(LibraryEntry.class)) {

            libraryService.scanPlatform(platformName);

            verify(localScanNotificationOrchestrationService, times(1)).notifyStart(platformName);
            verify(clientMock, times(1)).scan(Path.of(platform.getLibraryPaths().getFirst()));
            verify(clientMock, times(1)).scan(Path.of(platform.getLibraryPaths().getLast()));
            verify(gameMapper, never()).toEntity(game2DTO);
            verify(gameRepository, times(1)).findByTitle(anyString());
            verify(libraryEntryRepository, times(1)).findByGameIdAndPlatformId(1, 1);
            assertEquals(0, mockedConstruction.constructed().size());
            verify(game1Entry, never()).setGame(any(Game.class));
            verify(game1Entry, never()).setPlatform(any(Platform.class));
            verify(libraryEntryRepository, times(1)).save(any(LibraryEntry.class));
            verify(localScanNotificationOrchestrationService, times(1)).notifyBatch(platformName, gamesInLibrary1);
            verify(localScanNotificationOrchestrationService, never()).notifyBatch(platformName, gamesInLibrary2);
            verify(localScanNotificationOrchestrationService, times(1)).notifyComplete(eq(platformName), eq(1), eq(1), eq(List.of("D:/My Games/Steam")));
            verify(localScanNotificationOrchestrationService, never()).notifyError(anyString(), anyInt(), anyList());

        }

    }

    @Test
    @DisplayName("scanPlatform should catch the IO Exception and notify an error to via the OrchestrationService")
    void testScanFailure() throws IOException {

        String platformName = "Steam";
        List<String> libraryPaths = List.of("C:/Games/Steam/common");

        Platform platform = Platform.builder()
                .name(platformName)
                .libraryPaths(libraryPaths)
                .id(1)
                .build();

        LocalGameLibraryClient clientMock = mock(LocalGameLibraryClient.class);

        when(clientMock.scan(any(Path.class))).thenThrow(IOException.class);

        when(platformRepository.findByName(platformName)).thenReturn(Optional.of(platform));

        when(platformClientMapperService.getScanner(platform)).thenReturn(clientMock);

        try (var mockedConstruction = Mockito.mockConstruction(LibraryEntry.class)) {

            libraryService.scanPlatform(platformName);

            verify(localScanNotificationOrchestrationService, times(1)).notifyStart(platformName);
            verify(clientMock, times(1)).scan(Path.of(platform.getLibraryPaths().getFirst()));
            verify(gameMapper, never()).toEntity(any(ScannedLocalGameDTO.class));
            verify(gameRepository, never()).findByTitle(anyString());
            verify(gameRepository, never()).save(any(Game.class));
            verify(libraryEntryRepository, never()).findByGameIdAndPlatformId(anyInt(), anyInt());
            assertEquals(0, mockedConstruction.constructed().size());
            verify(libraryEntryRepository, never()).save(any(LibraryEntry.class));
            verify(localScanNotificationOrchestrationService, never()).notifyBatch(anyString(), anyList());
            verify(localScanNotificationOrchestrationService, never()).notifyComplete(anyString(), anyInt());
            verify(localScanNotificationOrchestrationService, times(1)).notifyError(eq(platformName), eq(1), eq(libraryPaths));

        }

    }

    @Test
    @DisplayName("scanPlatform should catch the IO Exception and notify an error to via the OrchestrationService")
    void testNotificationFailure() throws IOException {

        String platformName = "Steam";
        List<String> libraryPaths = List.of("C:/Games/Steam/common");

        Platform platform = Platform.builder()
                .name(platformName)
                .libraryPaths(libraryPaths)
                .id(1)
                .build();

        LocalGameLibraryClient clientMock = mock(LocalGameLibraryClient.class);

        when(clientMock.scan(any(Path.class))).thenThrow(IOException.class);

        when(platformRepository.findByName(platformName)).thenReturn(Optional.of(platform));

        when(platformClientMapperService.getScanner(platform)).thenReturn(clientMock);

        try (var mockedConstruction = Mockito.mockConstruction(LibraryEntry.class)) {

            libraryService.scanPlatform(platformName);

            verify(localScanNotificationOrchestrationService, times(1)).notifyStart(platformName);
            verify(clientMock, times(1)).scan(Path.of(platform.getLibraryPaths().getFirst()));
            verify(gameMapper, never()).toEntity(any(ScannedLocalGameDTO.class));
            verify(gameRepository, never()).findByTitle(anyString());
            verify(gameRepository, never()).save(any(Game.class));
            verify(libraryEntryRepository, never()).findByGameIdAndPlatformId(anyInt(), anyInt());
            assertEquals(0, mockedConstruction.constructed().size());
            verify(libraryEntryRepository, never()).save(any(LibraryEntry.class));
            verify(localScanNotificationOrchestrationService, never()).notifyBatch(anyString(), anyList());
            verify(localScanNotificationOrchestrationService, never()).notifyComplete(anyString(), anyInt());
            verify(localScanNotificationOrchestrationService, times(1)).notifyError(eq(platformName), eq(1), eq(libraryPaths));

        }

    }


}
