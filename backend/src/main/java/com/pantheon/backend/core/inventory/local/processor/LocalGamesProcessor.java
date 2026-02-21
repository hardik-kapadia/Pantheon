package com.pantheon.backend.core.inventory.local.processor;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.inventory.mapper.GameMapper;
import com.pantheon.backend.core.inventory.model.Game;
import com.pantheon.backend.core.inventory.model.LocalInstallation;
import com.pantheon.backend.core.platform.model.Platform;
import com.pantheon.backend.core.inventory.repository.GameRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This Service processes the scanned games and creates/updates related entries in the DB
 */
@Slf4j
@Service
public class LocalGamesProcessor {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;
    private final LibraryEntryRepository libraryEntryRepository;

    @Autowired
    public LocalGamesProcessor(GameRepository gameRepository, GameMapper gameMapper, LibraryEntryRepository libraryEntryRepository) {
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

        log.info("{}: Creating/Updating library entry {}", platform.getName(), dto.toString());

        LocalInstallation entry = libraryEntryRepository.findByGameIdAndPlatformId(game.getId(), platform.getId())
                .orElseGet(() -> {
                    LocalInstallation newEntry = new LocalInstallation();
                    newEntry.setGame(game);
                    newEntry.setPlatform(platform);
                    return newEntry;
                });

        entry.setInstalled(dto.isInstalled());
        entry.setInstallPath(dto.installPath());
        entry.setPlatformGameId(dto.platformGameId());

        if (dto.playtimeMinutes() != null && dto.playtimeMinutes() > 0) {
            entry.setPlaytimeMinutes(dto.playtimeMinutes());
        }

        if (dto.downloadSize() != null && dto.downloadSize() > 0) {
            entry.setGameSize(dto.downloadSize());
        }

        if (dto.lastPlayed() != null) {
            entry.setLastPlayed(dto.lastPlayed());
        }


        libraryEntryRepository.save(entry);
    }

}
