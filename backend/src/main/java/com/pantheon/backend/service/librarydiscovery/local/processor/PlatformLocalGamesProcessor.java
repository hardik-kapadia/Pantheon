package com.pantheon.backend.service.librarydiscovery.local.processor;

import com.pantheon.backend.dto.ScannedLocalGameDTO;
import com.pantheon.backend.mapper.GameMapper;
import com.pantheon.backend.model.Game;
import com.pantheon.backend.model.LibraryEntry;
import com.pantheon.backend.model.Platform;
import com.pantheon.backend.repository.GameRepository;
import com.pantheon.backend.repository.LibraryEntryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * This Service processes the scanned games and creates/updates related entries in the DB
 */
@Slf4j
@Service
public class PlatformLocalGamesProcessor {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;
    private final LibraryEntryRepository libraryEntryRepository;

    @Autowired
    public PlatformLocalGamesProcessor(GameRepository gameRepository, GameMapper gameMapper, LibraryEntryRepository libraryEntryRepository) {
        this.gameRepository = gameRepository;
        this.gameMapper = gameMapper;
        this.libraryEntryRepository = libraryEntryRepository;
    }

    /**
     *
     * Calls helper methods interacting with the repositories to Create/Update the Scanned Games
     *
     * @param scannedGames The Games identified by scanning the platform
     * @param platform     the platform for which the games were scanned
     */
    @Transactional
    public void processScannedGames(List<ScannedLocalGameDTO> scannedGames, Platform platform) {
        for (ScannedLocalGameDTO dto : scannedGames) {
            Game game = findOrCreateGame(dto);
            createOrUpdateLibraryEntry(game, platform, dto);
        }
        log.info("{}: Processed {} games ", platform.getName(), scannedGames.size());
    }

    private Game findOrCreateGame(ScannedLocalGameDTO dto) {

        return gameRepository.findByTitle(dto.title()).orElseGet(() -> gameRepository.save(gameMapper.toEntity(dto)));
    }

    private void createOrUpdateLibraryEntry(Game game, Platform platform, ScannedLocalGameDTO dto) {

        LibraryEntry entry = libraryEntryRepository.findByGameIdAndPlatformId(game.getId(), platform.getId())
                .orElseGet(() -> {
                    LibraryEntry newEntry = new LibraryEntry();
                    newEntry.setGame(game);
                    newEntry.setPlatform(platform);
                    return newEntry;
                });

        entry.setInstalled(dto.isInstalled());
        entry.setInstallPath(dto.installPath());
        entry.setPlatformGameId(dto.platformGameId());

        if (dto.playtimeMinutes() != null) {
            entry.setPlaytimeMinutes(dto.playtimeMinutes());
        }
        if (dto.lastPlayed() != null) {
            entry.setLastPlayed(dto.lastPlayed());
        }

        libraryEntryRepository.save(entry);
    }

}
