package com.pantheon.backend.core.inventory.local.processor;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.inventory.mapper.GameMapper;
import com.pantheon.backend.core.inventory.model.Game;
import com.pantheon.backend.core.inventory.model.LocalInstallation;
import com.pantheon.backend.core.inventory.repository.GameRepository;
import com.pantheon.backend.core.inventory.repository.LibraryEntryRepository;
import com.pantheon.backend.core.platform.model.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalGamesProcessorTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private LibraryEntryRepository libraryEntryRepository;

    @InjectMocks
    private LocalGamesProcessor localGamesProcessor;

    private Platform platform;
    private ScannedLocalGameDTO scannedGameDTO;
    private Game game;

    @BeforeEach
    void setUp() {
        platform = Platform.builder()
                .id(1)
                .name("Steam")
                .build();

        scannedGameDTO = ScannedLocalGameDTO.builder()
                .title("Test Game")
                .platformGameId("12345")
                .installPath("/path/to/game")
                .isInstalled(true)
                .playtimeMinutes(120)
                .downloadSize(1024L)
                .lastPlayed(LocalDateTime.now())
                .build();

        game = new Game();
        game.setId(100);
        game.setTitle("Test Game");
    }

    @Test
    void processScannedGames_NewGame_CreatesGameAndLibraryEntry() {
        when(gameRepository.findByTitle("Test Game")).thenReturn(Optional.empty());
        when(gameMapper.toEntity(scannedGameDTO)).thenReturn(game);
        when(gameRepository.save(game)).thenReturn(game);
        when(libraryEntryRepository.findByGameIdAndPlatformId(100, 1)).thenReturn(Optional.empty());

        localGamesProcessor.processScannedGames(List.of(scannedGameDTO), platform);

        verify(gameRepository).save(game);
        verify(libraryEntryRepository).save(any(LocalInstallation.class));
    }

    @Test
    void processScannedGames_ExistingGame_UpdatesLibraryEntry() {
        when(gameRepository.findByTitle("Test Game")).thenReturn(Optional.of(game));
        
        LocalInstallation existingEntry = new LocalInstallation();
        existingEntry.setGame(game);
        existingEntry.setPlatform(platform);
        
        when(libraryEntryRepository.findByGameIdAndPlatformId(100, 1)).thenReturn(Optional.of(existingEntry));

        localGamesProcessor.processScannedGames(List.of(scannedGameDTO), platform);

        verify(gameRepository, org.mockito.Mockito.never()).save(any(Game.class));
        verify(libraryEntryRepository).save(existingEntry);
    }
}
