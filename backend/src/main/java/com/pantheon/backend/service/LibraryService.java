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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
public class LibraryService {

    private final PlatformRepository platformRepository;
    private final GameRepository gameRepository;
    private final LibraryEntryRepository libraryEntryRepository;
    private final GameMapper gameMapper;
    private final PlatformClientMapperService platformClientMapperService;
    private final LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService;

    @Autowired

    public LibraryService(PlatformRepository platformRepository,
                          GameRepository gameRepository,
                          LibraryEntryRepository libraryEntryRepository,
                          PlatformClientMapperService platformClientMapperService,
                          GameMapper gameMapper,
                          LocalScanNotificationOrchestrationService localScanNotificationOrchestrationService) {

        this.platformRepository = platformRepository;
        this.gameRepository = gameRepository;
        this.libraryEntryRepository = libraryEntryRepository;
        this.gameMapper = gameMapper;
        this.platformClientMapperService = platformClientMapperService;
        this.localScanNotificationOrchestrationService = localScanNotificationOrchestrationService;

    }

    /**
     * @param platformName The name of the platform to scan
     * @throws IllegalArgumentException for incorrect platform name
     * @throws IllegalStateException    for correct platform name but no Scanner defined for it.
     */
    @Async
    @Transactional
    public void scanPlatform(String platformName) {
        log.info("Requesting scan for platform {}", platformName);

        try {
            Platform platform = platformRepository.findByName(platformName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown platform: " + platformName));

            LocalGameLibraryClient client = platformClientMapperService.getScanner(platform);

            // 1. Notify Start (We guess size is 0 initially, or you could estimate based on path count)
            localScanNotificationOrchestrationService.notifyStart(platformName);

            int totalGamesFound = 0;

            for (String pathStr : platform.getLibraryPaths()) {

                List<ScannedLocalGameDTO> foundGames = client.scan(Path.of(pathStr));

                processScannedGames(foundGames, platform);

                localScanNotificationOrchestrationService.notifyBatch(platformName, foundGames);

                totalGamesFound += foundGames.size();
            }

            // 2. Notify Complete
            localScanNotificationOrchestrationService.notifyComplete(platformName, totalGamesFound);

        } catch (Exception e) {
            log.error("Scan failed for {}", platformName, e);
            localScanNotificationOrchestrationService.notifyError(platformName);
        }
    }


    private void processScannedGames(List<ScannedLocalGameDTO> scannedGames, Platform platform) {
        for (ScannedLocalGameDTO dto : scannedGames) {
            Game game = findOrCreateGame(dto);
            createOrUpdateLibraryEntry(game, platform, dto);
        }
        log.info("{}: Processed {} games ", platform.getName(), scannedGames.size());
    }

    private Game findOrCreateGame(ScannedLocalGameDTO dto) {

        return gameRepository.findByTitle(dto.title()).orElseGet(() -> {
            Game newGame = gameMapper.toEntity(dto);
            return gameRepository.save(newGame);
        });
    }

    private void createOrUpdateLibraryEntry(Game game, Platform platform, ScannedLocalGameDTO dto) {

        LibraryEntry entry = libraryEntryRepository.findByGameIdAndPlatformId(game.getId(), platform.getId()).orElseGet(() -> {
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
