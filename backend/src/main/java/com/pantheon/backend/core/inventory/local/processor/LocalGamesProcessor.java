package com.pantheon.backend.core.inventory.local.processor;

import com.pantheon.backend.core.inventory.local.dto.ScannedLocalGameDTO;
import com.pantheon.backend.core.inventory.mapper.GameMapper;
import com.pantheon.backend.core.inventory.model.Game;
import com.pantheon.backend.core.inventory.model.RemoteEntitlement;
import com.pantheon.backend.core.inventory.repository.GameRepository;
import com.pantheon.backend.core.platform.model.Platform;
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

    @Autowired
    public LocalGamesProcessor(GameRepository gameRepository, GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.gameMapper = gameMapper;
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
            // TODO: generate the remoteEntitlement first and then then the localInstallation
            RemoteEntitlement remoteEntitlement = findOrCreateRemoteEntitlement(game, platform, dto);
            createOrUpdateLocalInstallation(remoteEntitlement, dto);
//            createOrUpdateLibraryEntry(game, platform, dto);
        }
        log.info("{}: Processed {} games ", platform.getName(), scannedGames.size());
    }

    private Game findOrCreateGame(ScannedLocalGameDTO dto) {
        return gameRepository.findByTitle(dto.title()).orElseGet(() -> gameRepository.save(gameMapper.toEntity(dto)));
    }

    // Need to check whether the user has logged in or not, and if they have then just verify and return, if they haven't then create the RemoteEntitlement and return it.
    private RemoteEntitlement findOrCreateRemoteEntitlement(Game game, Platform platform, ScannedLocalGameDTO dto) {
        // TODO: Implement
        return null;
    }

    private void createOrUpdateLocalInstallation(RemoteEntitlement remoteEntitlement, ScannedLocalGameDTO dto) {
        // TODO: Implement
    }

    // TODO: delete and write new method accounting for split entities
//     private void createOrUpdateLibraryEntry(Game game, Platform platform, ScannedLocalGameDTO dto) {
//
//     log.info("{}: Creating/Updating library entry {}", platform.getName(), dto.toString());
//
//     LocalInstallation entry = libraryEntryRepository.findByGameIdAndPlatformId(game.getId(), platform.getId())
//     .orElseGet(() -> {
//     LocalInstallation newEntry = new LocalInstallation();
//     newEntry.setGame(game);
//     newEntry.setPlatform(platform);
//     return newEntry;
//     });
//
//     entry.setInstalled(dto.isInstalled());
//     entry.setInstallPath(dto.installPath());
//     entry.setPlatformGameId(dto.platformGameId());
//
//     if (dto.playtimeMinutes() != null && dto.playtimeMinutes() > 0) {
//     entry.setPlaytimeMinutes(dto.playtimeMinutes());
//     }
//
//     if (dto.downloadSize() != null && dto.downloadSize() > 0) {
//     entry.setGameSize(dto.downloadSize());
//     }
//
//     if (dto.lastPlayed() != null) {
//     entry.setLastPlayed(dto.lastPlayed());
//     }
//
//
//     libraryEntryRepository.save(entry);
//     }

}
